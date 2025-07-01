package ca.fxco.gitmergepipeline;

import ca.fxco.gitmergepipeline.config.ConfigurationLoader;
import ca.fxco.gitmergepipeline.merge.MergeDriver;
import ca.fxco.gitmergepipeline.merge.MergeTool;
import ca.fxco.gitmergepipeline.merge.ReMergeTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Main entry point for the GitMergePipeline application.
 * This application can be used as:
 * 1. A git merge driver
 * 2. A re-merge tool
 * 3. A merge tool
 *
 * @author FX
 */
public class GitMergePipeline {
    private static final Logger logger = LoggerFactory.getLogger(GitMergePipeline.class);

    // Exit codes
    private static final int SUCCESS = 0;
    private static final int ERROR_INVALID_ARGS = 1;
    private static final int ERROR_EXECUTION = 2;

    public static void main(String[] args) {
        if (args.length < 1) {
            printUsage();
            System.exit(ERROR_INVALID_ARGS);
        }

        String mode = args[0].toLowerCase();
        String[] modeArgs = Arrays.copyOfRange(args, 1, args.length);

        try {
            int exitCode;
            switch (mode) {
                case "driver":
                    exitCode = runAsMergeDriver(modeArgs);
                    break;
                case "remerge":
                    exitCode = runAsReMergeTool(modeArgs);
                    break;
                case "tool":
                    exitCode = runAsMergeTool(modeArgs);
                    break;
                case "help":
                    printUsage();
                    exitCode = SUCCESS;
                    break;
                default:
                    System.err.println("Unknown mode: " + mode);
                    printUsage();
                    exitCode = ERROR_INVALID_ARGS;
            }
            System.exit(exitCode);
        } catch (Exception e) {
            logger.error("Error executing in mode: " + mode, e);
            System.err.println("Error: " + e.getMessage());
            System.exit(ERROR_EXECUTION);
        }
    }

    static int runAsMergeDriver(String[] args) throws IOException {
        if (args.length < 4) {
            System.err.println("Insufficient arguments for merge driver mode");
            System.err.println("Usage: driver %O %A %B %P");
            System.err.println("  %O - Base version path");
            System.err.println("  %A - Current version path");
            System.err.println("  %B - Other version path");
            System.err.println("  %P - Path to the file relative to working directory");
            return ERROR_INVALID_ARGS;
        }

        Path basePath = Paths.get(args[0]);
        Path currentPath = Paths.get(args[1]);
        Path otherPath = Paths.get(args[2]);
        String filePath = args[3];

        logger.info("Running as merge driver for file: {}", filePath);

        ConfigurationLoader configLoader = new ConfigurationLoader();
        MergeDriver mergeDriver = new MergeDriver(configLoader.loadConfiguration());

        return mergeDriver.merge(basePath, currentPath, otherPath, filePath) ? SUCCESS : ERROR_EXECUTION;
    }

    static int runAsReMergeTool(String[] args) throws IOException {
        if (args.length < 3) {
            System.err.println("Insufficient arguments for re-merge tool mode");
            System.err.println("Usage: remerge <base> <current> <other>");
            return ERROR_INVALID_ARGS;
        }

        Path basePath = Paths.get(args[0]);
        Path currentPath = Paths.get(args[1]);
        Path otherPath = Paths.get(args[2]);

        logger.info("Running as re-merge tool");

        ConfigurationLoader configLoader = new ConfigurationLoader();
        ReMergeTool reMergeTool = new ReMergeTool(configLoader.loadConfiguration());

        return reMergeTool.remerge(basePath, currentPath, otherPath) ? SUCCESS : ERROR_EXECUTION;
    }

    static int runAsMergeTool(String[] args) throws IOException {
        if (args.length < 3) {
            System.err.println("Insufficient arguments for merge tool mode");
            System.err.println("Usage: tool <local> <remote> <merged>");
            return ERROR_INVALID_ARGS;
        }

        Path localPath = Paths.get(args[0]);
        Path remotePath = Paths.get(args[1]);
        Path mergedPath = Paths.get(args[2]);

        logger.info("Running as merge tool");

        ConfigurationLoader configLoader = new ConfigurationLoader();
        MergeTool mergeTool = new MergeTool(configLoader.loadConfiguration());

        return mergeTool.merge(localPath, remotePath, mergedPath) ? SUCCESS : ERROR_EXECUTION;
    }

    private static void printUsage() {
        System.out.println("GitMergePipeline - A configurable Git merge pipeline system");
        System.out.println();
        System.out.println("Usage:");
        System.out.println("  driver %O %A %B %P   - Run as a Git merge driver");
        System.out.println("  remerge <base> <current> <other> - Run as a re-merge tool");
        System.out.println("  tool <local> <remote> <merged> - Run as a merge tool");
        System.out.println("  help                 - Show this help message");
        System.out.println();
        System.out.println("For more information, see the documentation.");
    }
}
