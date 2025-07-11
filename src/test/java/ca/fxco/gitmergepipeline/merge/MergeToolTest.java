package ca.fxco.gitmergepipeline.merge;

import ca.fxco.gitmergepipeline.config.PipelineConfiguration;
import ca.fxco.gitmergepipeline.pipeline.Pipeline;
import ca.fxco.gitmergepipeline.pipeline.StandardPipeline;
import ca.fxco.gitmergepipeline.rule.FilePatternRule;
import org.eclipse.jgit.api.Git;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the MergeTool class which handles merging local and remote files.
 * Tests include successful merges, failing merges, merges with no pipelines,
 * and merges with file pattern rules.
 *
 * @author FX
 */
class MergeToolTest {

    @TempDir
    Path tempDir;

    private Path localFile;
    private Path remoteFile;
    private Path mergedFile;
    private PipelineConfiguration configuration;
    private MergeTool mergeTool;

    @BeforeEach
    void setUp() throws IOException {
        // Create test files
        localFile = tempDir.resolve("local.txt");
        remoteFile = tempDir.resolve("remote.txt");
        mergedFile = tempDir.resolve("merged.txt");

        Files.writeString(localFile, "Local content");
        Files.writeString(remoteFile, "Remote content");

        // Create a mock pipeline that always succeeds
        Pipeline successPipeline = new Pipeline() {
            @Override
            public MergeResult execute(MergeContext context) {
                try {
                    // Write to the merged file to simulate a successful merge
                    Path mergedPath = (Path) context.getAttribute("mergedPath");
                    Files.writeString(mergedPath, "Merged content");
                    return MergeResult.success("Success from mock pipeline", mergedPath);
                } catch (IOException e) {
                    return MergeResult.error("Error writing to merged file", e);
                }
            }

            @Override
            public MergeResult executeBatched(Git git, GitMergeContext context) {
                try {
                    // Write to the merged file to simulate a successful merge
                    Path mergedPath = (Path) context.getAttribute("mergedPath");
                    Files.writeString(mergedPath, "Merged content");
                    return MergeResult.success("Success from mock pipeline", mergedPath);
                } catch (IOException e) {
                    return MergeResult.error("Error writing to merged file", e);
                }
            }

            @Override
            public String getDescription() {
                return "Success Pipeline";
            }
        };

        // Create a mock pipeline that always fails
        Pipeline failurePipeline = new Pipeline() {
            @Override
            public MergeResult execute(MergeContext context) {
                return MergeResult.error("Failure from mock pipeline", new RuntimeException("Test failure"));
            }

            @Override
            public MergeResult executeBatched(Git git, GitMergeContext context) {
                return MergeResult.error("Failure from mock pipeline", new RuntimeException("Test failure"));
            }

            @Override
            public String getDescription() {
                return "Failure Pipeline";
            }
        };

        // Create a configuration with the mock pipelines
        configuration = PipelineConfiguration.onlyPipelines(successPipeline, failurePipeline);

        // Create the merge tool
        mergeTool = new MergeTool(configuration);
    }

    @Test
    void mergeWithSuccessfulPipeline() throws IOException {
        // The first pipeline in the configuration is the success pipeline
        boolean result = mergeTool.merge(localFile, remoteFile, mergedFile);

        assertTrue(result);
        assertTrue(Files.exists(mergedFile));
        assertEquals("Merged content", Files.readString(mergedFile));
    }

    @Test
    void mergeWithFailingPipeline() throws IOException {
        // Create a configuration with only the failure pipeline
        PipelineConfiguration failureConfig = PipelineConfiguration.onlyPipelines(configuration.getPipelines().get(1));

        MergeTool failureTool = new MergeTool(failureConfig);

        boolean result = failureTool.merge(localFile, remoteFile, mergedFile);

        assertFalse(result);
    }

    @Test
    void mergeWithNoPipelines() throws IOException {
        // Create a configuration with no pipelines
        PipelineConfiguration emptyConfig = new PipelineConfiguration();

        MergeTool emptyTool = new MergeTool(emptyConfig);

        boolean result = emptyTool.merge(localFile, remoteFile, mergedFile);

        assertFalse(result);
    }

    @Test
    void mergeWithFilePatternRule() throws IOException {
        // Create a pipeline with a file pattern rule
        FilePatternRule javaRule = new FilePatternRule("*.java", false, false);
        FilePatternRule txtRule = new FilePatternRule("*.txt", false, false);

        Pipeline.Step javaStep = new Pipeline.Step(javaRule, "success", Collections.emptyList());
        Pipeline.Step txtStep = new Pipeline.Step(txtRule, "success", Collections.emptyList());

        StandardPipeline javaPipeline = new StandardPipeline("Java Pipeline", Collections.singletonList(javaStep), null);
        StandardPipeline txtPipeline = new StandardPipeline("Txt Pipeline", Collections.singletonList(txtStep), null) {
            @Override
            public MergeResult execute(MergeContext context) {
                try {
                    // Write to the merged file to simulate a successful merge
                    Path mergedPath = (Path) context.getAttribute("mergedPath");
                    Files.writeString(mergedPath, "Merged content from txt pipeline");
                    return MergeResult.success("Success from txt pipeline", mergedPath);
                } catch (IOException e) {
                    return MergeResult.error("Error writing to merged file", e);
                }
            }
        };

        // Create a configuration with the pipelines
        PipelineConfiguration ruleConfig = PipelineConfiguration.onlyPipelines(javaPipeline, txtPipeline);

        MergeTool ruleTool = new MergeTool(ruleConfig);

        // The txt pipeline should be selected for a .txt file
        boolean result = ruleTool.merge(localFile, remoteFile, mergedFile);

        assertTrue(result);
        assertTrue(Files.exists(mergedFile));
        assertEquals("Merged content from txt pipeline", Files.readString(mergedFile));
    }
}
