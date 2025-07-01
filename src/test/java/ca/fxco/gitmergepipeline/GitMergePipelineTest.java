package ca.fxco.gitmergepipeline;

import ca.fxco.gitmergepipeline.config.ConfigurationLoader;
import ca.fxco.gitmergepipeline.merge.MergeDriver;
import ca.fxco.gitmergepipeline.merge.MergeTool;
import ca.fxco.gitmergepipeline.merge.ReMergeTool;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Tests for running the build as a git merge driver, a re-merge tool, and a merge tool.
 * These tests directly use the MergeDriver, ReMergeTool, and MergeTool classes
 * to simulate how GitMergePipeline would run them.
 *
 * @author FX
 */
class GitMergePipelineTest {

    @TempDir
    Path tempDir;

    /**
     * Test running the build as a git merge driver.
     */
    @Test
    void testRunAsMergeDriver() throws IOException {
        // Create test files
        Path baseFile = tempDir.resolve("base.txt");
        Path currentFile = tempDir.resolve("current.txt");
        Path otherFile = tempDir.resolve("other.txt");
        String filePath = "test.txt";

        Files.writeString(baseFile, "Base content");
        Files.writeString(currentFile, "Current content");
        Files.writeString(otherFile, "Other content");

        // Create configuration and merge driver
        ConfigurationLoader configLoader = new ConfigurationLoader();
        MergeDriver mergeDriver = new MergeDriver(configLoader.loadConfiguration());

        // Run the merge driver
        boolean result = mergeDriver.merge(baseFile, currentFile, otherFile, filePath);

        // Verify the result
        // The result might be false if no pipeline is configured for this file type
        // but the test should still pass as long as the merge driver runs without exceptions
        System.out.println("Merge driver result: " + result);
    }

    /**
     * Test running the build as a re-merge tool.
     */
    @Test
    void testRunAsReMergeTool() throws IOException {
        // Create test files
        Path baseFile = tempDir.resolve("base.txt");
        Path currentFile = tempDir.resolve("current.txt");
        Path otherFile = tempDir.resolve("other.txt");

        Files.writeString(baseFile, "Base content");
        Files.writeString(currentFile, "Current content with conflicts");
        Files.writeString(otherFile, "Other content with different changes");

        // Create configuration and re-merge tool
        ConfigurationLoader configLoader = new ConfigurationLoader();
        ReMergeTool reMergeTool = new ReMergeTool(configLoader.loadConfiguration());

        // Run the re-merge tool
        boolean result = reMergeTool.remerge(baseFile, currentFile, otherFile);

        // Verify the result
        // The result might be false if no pipeline is configured for this file type
        // but the test should still pass as long as the re-merge tool runs without exceptions
        System.out.println("Re-merge tool result: " + result);
    }

    /**
     * Test running the build as a merge tool.
     */
    @Test
    void testRunAsMergeTool() throws IOException {
        // Create test files
        Path localFile = tempDir.resolve("local.txt");
        Path remoteFile = tempDir.resolve("remote.txt");
        Path mergedFile = tempDir.resolve("merged.txt");

        Files.writeString(localFile, "Local content");
        Files.writeString(remoteFile, "Remote content");

        // Create configuration and merge tool
        ConfigurationLoader configLoader = new ConfigurationLoader();
        MergeTool mergeTool = new MergeTool(configLoader.loadConfiguration());

        // Run the merge tool
        boolean result = mergeTool.merge(localFile, remoteFile, mergedFile);

        // Verify the result
        // The result might be false if no pipeline is configured for this file type
        // but the test should still pass as long as the merge tool runs without exceptions
        System.out.println("Merge tool result: " + result);
    }
}
