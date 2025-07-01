package ca.fxco.gitmergepipeline.merge.operations;

import ca.fxco.gitmergepipeline.merge.MergeContext;
import ca.fxco.gitmergepipeline.merge.MergeResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the CommandLineMergeOperation class which performs merges using command line tools.
 * Tests include executing successful commands, commands with conflicts, commands with errors,
 * and invalid parameters.
 *
 * @author FX
 */
public class CommandLineMergeOperationTest {

    @TempDir
    Path tempDir;

    private Path baseFile;
    private Path currentFile;
    private Path otherFile;
    private Path mergedFile;
    private CommandLineMergeOperation operation;

    @BeforeEach
    void setUp() {
        // Create test files
        baseFile = tempDir.resolve("base.txt");
        currentFile = tempDir.resolve("current.txt");
        otherFile = tempDir.resolve("other.txt");
        mergedFile = tempDir.resolve("merged.txt");

        // Create the operation
        operation = new CommandLineMergeOperation();
    }

    @Test
    void getName() {
        assertEquals("command-line-merge", operation.getName());
    }

    @Test
    void getDescription() {
        assertTrue(operation.getDescription().contains("command line"));
    }

    @Test
    void executeWithSuccessfulCommand() throws IOException {
        // Create test files
        Files.writeString(baseFile, "Line 1\nLine 2\nLine 3\n");
        Files.writeString(currentFile, "Line 1\nLine 2 modified by current\nLine 3\n");
        Files.writeString(otherFile, "Line 1\nLine 2\nLine 3 modified by other\n");

        // Create a merged file to use as output
        Path outputFile = tempDir.resolve("output.txt");

        MergeContext context = new MergeContext(baseFile, currentFile, otherFile, "test.txt");
        context.setAttribute("mergedPath", outputFile);

        // Use a simple command that creates a merged file with content from both files
        // Using echo instead of cat to avoid redirection issues
        String command = "echo 'Line 1' > %OUTPUT% && " +
                         "echo 'Line 2 modified by current' >> %OUTPUT% && " +
                         "echo 'Line 3 modified by other' >> %OUTPUT%";

        MergeResult result = operation.execute(context, Collections.singletonList(command));

        // Print the result status for debugging
        System.out.println("[DEBUG_LOG] Result status: " + result.getStatus());
        System.out.println("[DEBUG_LOG] Result message: " + result.getMessage());
        System.out.println("[DEBUG_LOG] Result isSuccess: " + result.isSuccess());

        assertTrue(result.isSuccess());
        assertTrue(Files.exists(outputFile), "Output file should exist");

        // Print the file content for debugging
        String mergedContent = Files.readString(outputFile);
        System.out.println("[DEBUG_LOG] Merged content: " + mergedContent);

        assertTrue(mergedContent.contains("Line 2 modified by current"), "Merged content should contain current line");
        assertTrue(mergedContent.contains("Line 3 modified by other"), "Merged content should contain other line");
    }

    @Test
    void executeWithConflictCommand() throws IOException {
        // Create test files
        Files.writeString(baseFile, "Line 1\nLine 2\nLine 3\n");
        Files.writeString(currentFile, "Line 1\nLine 2 modified by current\nLine 3\n");
        Files.writeString(otherFile, "Line 1\nLine 2 modified by other\nLine 3\n");

        MergeContext context = new MergeContext(baseFile, currentFile, otherFile, "test.txt");

        // Use a command that exits with code 1 to simulate a conflict
        String command = "cat %CURRENT% %OTHER% > %OUTPUT% && exit 1";

        MergeResult result = operation.execute(context, Collections.singletonList(command));

        // Print the result status for debugging
        System.out.println("[DEBUG_LOG] Result status: " + result.getStatus());
        System.out.println("[DEBUG_LOG] Result message: " + result.getMessage());
        System.out.println("[DEBUG_LOG] Result isConflict: " + result.isConflict());

        assertTrue(result.isConflict());
        assertTrue(Files.exists(currentFile));
    }

    @Test
    void executeWithErrorCommand() throws IOException {
        // Create test files
        Files.writeString(baseFile, "Line 1\nLine 2\nLine 3\n");
        Files.writeString(currentFile, "Line 1\nLine 2 modified by current\nLine 3\n");
        Files.writeString(otherFile, "Line 1\nLine 2 modified by other\nLine 3\n");

        MergeContext context = new MergeContext(baseFile, currentFile, otherFile, "test.txt");

        // Use a command that exits with code 2 to simulate an error
        String command = "cat %CURRENT% %OTHER% > %OUTPUT% && exit 2";

        MergeResult result = operation.execute(context, Collections.singletonList(command));

        // Print the result status for debugging
        System.out.println("[DEBUG_LOG] Result status: " + result.getStatus());
        System.out.println("[DEBUG_LOG] Result message: " + result.getMessage());
        System.out.println("[DEBUG_LOG] Result isError: " + result.isError());

        assertTrue(result.isError());
        assertTrue(Files.exists(currentFile));
    }

    @Test
    void executeWithInvalidCommand() throws IOException {
        // Create test files
        Files.writeString(baseFile, "Line 1\nLine 2\nLine 3\n");
        Files.writeString(currentFile, "Line 1\nLine 2 modified by current\nLine 3\n");
        Files.writeString(otherFile, "Line 1\nLine 2 modified by other\nLine 3\n");

        MergeContext context = new MergeContext(baseFile, currentFile, otherFile, "test.txt");

        // Use a non-existent command
        String command = "non_existent_command %CURRENT% %OTHER% %OUTPUT%";

        MergeResult result = operation.execute(context, Collections.singletonList(command));

        // Print the result status for debugging
        System.out.println("[DEBUG_LOG] Result status: " + result.getStatus());
        System.out.println("[DEBUG_LOG] Result message: " + result.getMessage());
        System.out.println("[DEBUG_LOG] Result isError: " + result.isError());

        assertTrue(result.isError());
    }

    @Test
    void executeWithNoCommand() throws IOException {
        // Create test files
        Files.writeString(baseFile, "Line 1\nLine 2\nLine 3\n");
        Files.writeString(currentFile, "Line 1\nLine 2 modified by current\nLine 3\n");
        Files.writeString(otherFile, "Line 1\nLine 2 modified by other\nLine 3\n");

        MergeContext context = new MergeContext(baseFile, currentFile, otherFile, "test.txt");

        // No command provided
        MergeResult result = operation.execute(context, null);

        // Print the result status for debugging
        System.out.println("[DEBUG_LOG] Result status: " + result.getStatus());
        System.out.println("[DEBUG_LOG] Result message: " + result.getMessage());
        System.out.println("[DEBUG_LOG] Result isError: " + result.isError());

        assertTrue(result.isError());
        assertTrue(result.getMessage().contains("No command specified"));
    }

    @Test
    void executeWithMergeTool() throws IOException {
        // Create test files
        Files.writeString(currentFile, "Line 1\nLine 2 modified by current\nLine 3\n");
        Files.writeString(otherFile, "Line 1\nLine 2\nLine 3 modified by other\n");

        MergeContext context = MergeContext.forMergeTool(currentFile, otherFile, mergedFile);

        // Use a simple command that concatenates the current and other files
        String command = "cat %CURRENT% %OTHER% > %OUTPUT%";

        MergeResult result = operation.execute(context, Collections.singletonList(command));

        // Print the result status for debugging
        System.out.println("[DEBUG_LOG] Result status: " + result.getStatus());
        System.out.println("[DEBUG_LOG] Result message: " + result.getMessage());
        System.out.println("[DEBUG_LOG] Result isSuccess: " + result.isSuccess());

        assertTrue(result.isSuccess());
        assertTrue(Files.exists(mergedFile));

        String mergedContent = Files.readString(mergedFile);
        assertTrue(mergedContent.contains("Line 2 modified by current"));
        assertTrue(mergedContent.contains("Line 3 modified by other"));
    }

    @Test
    void executeWithTimeout() throws IOException {
        // Skip this test if we're on a system where sleep might not work as expected
        org.junit.jupiter.api.Assumptions.assumeTrue(
            !System.getProperty("os.name").toLowerCase().contains("windows"),
            "Skipping timeout test on Windows where sleep behavior may be inconsistent"
        );

        // Create test files
        Files.writeString(baseFile, "Line 1\nLine 2\nLine 3\n");
        Files.writeString(currentFile, "Line 1\nLine 2 modified by current\nLine 3\n");
        Files.writeString(otherFile, "Line 1\nLine 2 modified by other\nLine 3\n");

        // Create a merged file to use as output
        Path outputFile = tempDir.resolve("timeout_output.txt");

        MergeContext context = new MergeContext(baseFile, currentFile, otherFile, "test.txt");
        context.setAttribute("mergedPath", outputFile);

        // Use a command that will run for a very long time
        // The 'yes' command outputs 'y' continuously until killed
        String command = "yes";
        List<String> parameters = Arrays.asList(command, "1"); // 1 second timeout

        System.out.println("[DEBUG_LOG] Executing command with timeout: " + command);
        System.out.println("[DEBUG_LOG] Timeout: 1 second");

        MergeResult result = operation.execute(context, parameters);

        // Print the result status for debugging
        System.out.println("[DEBUG_LOG] Result status: " + result.getStatus());
        System.out.println("[DEBUG_LOG] Result message: " + result.getMessage());
        System.out.println("[DEBUG_LOG] Result isError: " + result.isError());

        assertTrue(result.isError(), "Result should be an error due to timeout");
        assertTrue(result.getMessage().contains("timed out"), "Error message should mention timeout");
    }
}
