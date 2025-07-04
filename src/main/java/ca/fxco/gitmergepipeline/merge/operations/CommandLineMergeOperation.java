package ca.fxco.gitmergepipeline.merge.operations;

import ca.fxco.gitmergepipeline.merge.MergeContext;
import ca.fxco.gitmergepipeline.merge.MergeOperation;
import ca.fxco.gitmergepipeline.merge.MergeResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * A merge operation that executes a command line to perform a merge.
 * This operation allows users to specify an external command or script to handle merges.
 *
 * @author FX
 */
public class CommandLineMergeOperation implements MergeOperation {
    private static final Logger logger = LoggerFactory.getLogger(CommandLineMergeOperation.class);

    private static final int DEFAULT_TIMEOUT_SECONDS = 60;

    @Override
    public String getName() {
        return "command-line-merge";
    }

    @Override
    public String getDescription() {
        return "Merges files using a specified command line tool or script";
    }

    @Override
    public MergeResult execute(MergeContext context, List<String> parameters) throws IOException {
        if (parameters == null || parameters.isEmpty()) {
            return MergeResult.error("No command specified for command-line merge", null);
        }

        logger.debug("Executing command-line merge operation");

        // The first parameter is the command to execute
        String commandTemplate = parameters.getFirst();

        // Optional timeout parameter (in seconds)
        int timeoutSeconds = DEFAULT_TIMEOUT_SECONDS;
        if (parameters.size() > 1) {
            try {
                timeoutSeconds = Integer.parseInt(parameters.get(1));
            } catch (NumberFormatException e) {
                logger.warn("Invalid timeout value: {}. Using default: {}", parameters.get(1), DEFAULT_TIMEOUT_SECONDS);
            }
        }

        // Replace placeholders in the command with actual file paths
        String command = replacePathPlaceholders(commandTemplate, context);
        logger.debug("Executing command: {}", command);

        try {
            // Execute the command using a shell to support redirection and other shell features
            String[] shellCommand;
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                shellCommand = new String[]{"cmd.exe", "/c", command};
            } else {
                shellCommand = new String[]{"/bin/sh", "-c", command};
            }

            ProcessBuilder processBuilder = new ProcessBuilder(shellCommand);
            processBuilder.redirectErrorStream(false);

            Process process = processBuilder.start();

            // Create stream consumers that run in separate threads
            // This prevents the process from blocking due to full output buffers
            StringBuilder output = new StringBuilder();
            Thread outputThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append(System.lineSeparator());
                    }
                } catch (IOException e) {
                    // Ignore exceptions when the process is terminated
                }
            });
            outputThread.setDaemon(true);
            outputThread.start();

            StringBuilder errorOutput = new StringBuilder();
            Thread errorThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        errorOutput.append(line).append(System.lineSeparator());
                    }
                } catch (IOException e) {
                    // Ignore exceptions when the process is terminated
                }
            });
            errorThread.setDaemon(true);
            errorThread.start();

            // Wait for the process to complete with timeout
            boolean completed = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!completed) {
                // Force the process to terminate
                process.destroyForcibly();

                // Wait a short time to ensure the process is terminated
                try {
                    process.waitFor(1, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                logger.error("Command timed out after {} seconds", timeoutSeconds);
                return MergeResult.error("Command timed out after " + timeoutSeconds + " seconds", null);
            }

            int exitCode = process.exitValue();

            // Determine the output path
            Path outputPath = context.getCurrentPath();
            Object mergedPathObj = context.getAttribute("mergedPath");
            if (mergedPathObj instanceof Path path) {
                outputPath = path;
            }

            // Return result based on exit code
            if (exitCode == 0) {
                logger.debug("Command-line merge successful");
                return MergeResult.success("Command-line merge successful: " + output, outputPath);
            } else if (exitCode == 1) {
                // By convention, exit code 1 indicates conflicts
                logger.debug("Command-line merge resulted in conflicts");
                return MergeResult.conflict("Command-line merge resulted in conflicts: " + errorOutput);
            } else {
                logger.error("Command-line merge failed with exit code: {}", exitCode);
                return MergeResult.error("Command-line merge failed with exit code " + exitCode + ": " + errorOutput, null);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return MergeResult.error("Command execution was interrupted: " + e.getMessage(), e);
        } catch (IOException e) {
            logger.error("Error executing command", e);
            return MergeResult.error("Error executing command: " + e.getMessage(), e);
        }
    }

    /**
     * Replaces placeholders in the command template with actual file paths.
     * Available placeholders:
     * - %BASE% - Path to the base version of the file
     * - %CURRENT% - Path to the current version of the file
     * - %OTHER% - Path to the other version of the file
     * - %OUTPUT% - Path to the output file (same as %CURRENT% or mergedPath if available)
     * - %FILE% - Relative path to the file being merged
     * 
     * @param commandTemplate The command template with placeholders
     * @param context The merge context containing the file paths
     * @return The command with placeholders replaced
     */
    private String replacePathPlaceholders(String commandTemplate, MergeContext context) {
        String command = commandTemplate;

        // Replace base path placeholder
        if (context.getBasePath() != null) {
            command = command.replace("%BASE%", context.getBasePath().toString());
        }

        // Replace current path placeholder
        if (context.getCurrentPath() != null) {
            command = command.replace("%CURRENT%", context.getCurrentPath().toString());
        }

        // Replace other path placeholder
        if (context.getOtherPath() != null) {
            command = command.replace("%OTHER%", context.getOtherPath().toString());
        }

        // Replace output path placeholder
        Path outputPath = context.getCurrentPath();
        Object mergedPathObj = context.getAttribute("mergedPath");
        if (mergedPathObj instanceof Path path) {
            outputPath = path;
        }
        if (outputPath != null) {
            command = command.replace("%OUTPUT%", outputPath.toString());
        }

        // Replace file path placeholder
        if (context.getFilePath() != null) {
            command = command.replace("%FILE%", context.getFilePath());
        }

        return command;
    }
}
