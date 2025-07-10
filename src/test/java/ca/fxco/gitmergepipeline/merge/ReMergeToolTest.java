package ca.fxco.gitmergepipeline.merge;

import ca.fxco.gitmergepipeline.config.PipelineConfiguration;
import ca.fxco.gitmergepipeline.pipeline.Pipeline;
import ca.fxco.gitmergepipeline.pipeline.StandardPipeline;
import ca.fxco.gitmergepipeline.rule.FilePatternRule;
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
 * Tests for the ReMergeTool class which handles re-merging files with conflicts.
 * Tests include successful re-merges, failing re-merges, re-merges with no pipelines,
 * and re-merges with file pattern rules.
 *
 * @author FX
 */
class ReMergeToolTest {

    @TempDir
    Path tempDir;

    private Path baseFile;
    private Path currentFile;
    private Path otherFile;
    private PipelineConfiguration configuration;
    private ReMergeTool reMergeTool;

    @BeforeEach
    void setUp() throws IOException {
        // Create test files
        baseFile = tempDir.resolve("base.txt");
        currentFile = tempDir.resolve("current.txt");
        otherFile = tempDir.resolve("other.txt");

        Files.writeString(baseFile, "Base content");
        Files.writeString(currentFile, "Current content with conflicts");
        Files.writeString(otherFile, "Other content with different changes");

        // Create a mock pipeline that always succeeds
        Pipeline successPipeline = new Pipeline() {
            @Override
            public MergeResult execute(MergeContext context) {
                try {
                    // Write to the current file to simulate a successful re-merge
                    Files.writeString(context.getCurrentPath(), "Re-merged content");
                    return MergeResult.success("Success from mock pipeline", context.getCurrentPath());
                } catch (IOException e) {
                    return MergeResult.error("Error writing to current file", e);
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
            public String getDescription() {
                return "Failure Pipeline";
            }
        };

        // Create a configuration with the mock pipelines
        configuration = PipelineConfiguration.onlyPipelines(successPipeline, failurePipeline);

        // Create the re-merge tool
        reMergeTool = new ReMergeTool(configuration);
    }

    @Test
    void remergeWithSuccessfulPipeline() throws IOException {
        // The first pipeline in the configuration is the success pipeline
        boolean result = reMergeTool.remerge(baseFile, currentFile, otherFile);

        assertTrue(result);
        assertEquals("Re-merged content", Files.readString(currentFile));
    }

    @Test
    void remergeWithFailingPipeline() throws IOException {
        // Create a configuration with only the failure pipeline
        PipelineConfiguration failureConfig = PipelineConfiguration.onlyPipelines(configuration.getPipelines().get(1));

        ReMergeTool failureTool = new ReMergeTool(failureConfig);

        boolean result = failureTool.remerge(baseFile, currentFile, otherFile);

        assertFalse(result);
        assertEquals("Current content with conflicts", Files.readString(currentFile));
    }

    @Test
    void remergeWithNoPipelines() throws IOException {
        // Create a configuration with no pipelines
        PipelineConfiguration emptyConfig = new PipelineConfiguration();

        ReMergeTool emptyTool = new ReMergeTool(emptyConfig);

        boolean result = emptyTool.remerge(baseFile, currentFile, otherFile);

        assertFalse(result);
        assertEquals("Current content with conflicts", Files.readString(currentFile));
    }

    @Test
    void remergeWithFilePatternRule() throws IOException {
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
                    // Write to the current file to simulate a successful re-merge
                    Files.writeString(context.getCurrentPath(), "Re-merged content from txt pipeline");
                    return MergeResult.success("Success from txt pipeline", context.getCurrentPath());
                } catch (IOException e) {
                    return MergeResult.error("Error writing to current file", e);
                }
            }
        };

        // Create a configuration with the pipelines
        PipelineConfiguration ruleConfig = PipelineConfiguration.onlyPipelines(javaPipeline, txtPipeline);

        ReMergeTool ruleTool = new ReMergeTool(ruleConfig);

        // The txt pipeline should be selected for a .txt file
        boolean result = ruleTool.remerge(baseFile, currentFile, otherFile);

        assertTrue(result);
        assertEquals("Re-merged content from txt pipeline", Files.readString(currentFile));
    }
}
