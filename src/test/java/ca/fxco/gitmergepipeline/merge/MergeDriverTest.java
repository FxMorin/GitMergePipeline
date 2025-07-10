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
 * Tests for the MergeDriver class which handles merging files using pipelines.
 * Tests include successful merges, failing merges, merges with no pipelines,
 * and merges with file pattern rules.
 *
 * @author FX
 */
class MergeDriverTest {

    @TempDir
    Path tempDir;

    private Path baseFile;
    private Path currentFile;
    private Path otherFile;
    private PipelineConfiguration configuration;
    private MergeDriver mergeDriver;

    @BeforeEach
    void setUp() throws IOException {
        // Create test files
        baseFile = tempDir.resolve("base.txt");
        currentFile = tempDir.resolve("current.txt");
        otherFile = tempDir.resolve("other.txt");

        Files.writeString(baseFile, "Base content");
        Files.writeString(currentFile, "Current content");
        Files.writeString(otherFile, "Other content");

        // Create a mock pipeline that always succeeds
        Pipeline successPipeline = new Pipeline() {
            @Override
            public MergeResult execute(MergeContext context) {
                return MergeResult.success("Success from mock pipeline", null);
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

        // Create the merge driver
        mergeDriver = new MergeDriver(configuration);
    }

    @Test
    void mergeWithSuccessfulPipeline() throws IOException {
        // The first pipeline in the configuration is the success pipeline
        boolean result = mergeDriver.merge(baseFile, currentFile, otherFile, "test.txt");

        assertTrue(result);
    }

    @Test
    void mergeWithFailingPipeline() throws IOException {
        // Create a configuration with only the failure pipeline
        PipelineConfiguration failureConfig = PipelineConfiguration.onlyPipelines(configuration.getPipelines().get(1));

        MergeDriver failureDriver = new MergeDriver(failureConfig);

        boolean result = failureDriver.merge(baseFile, currentFile, otherFile, "test.txt");

        assertFalse(result);
    }

    @Test
    void mergeWithNoPipelines() throws IOException {
        // Create a configuration with no pipelines
        PipelineConfiguration emptyConfig = new PipelineConfiguration();

        MergeDriver emptyDriver = new MergeDriver(emptyConfig);

        boolean result = emptyDriver.merge(baseFile, currentFile, otherFile, "test.txt");

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
                return MergeResult.success("Success from txt pipeline", null);
            }
        };

        // Create a configuration with the pipelines
        PipelineConfiguration ruleConfig = PipelineConfiguration.onlyPipelines(javaPipeline, txtPipeline);

        MergeDriver ruleDriver = new MergeDriver(ruleConfig);

        // The txt pipeline should be selected for a .txt file
        boolean result = ruleDriver.merge(baseFile, currentFile, otherFile, "test.txt");

        assertTrue(result);
    }
}
