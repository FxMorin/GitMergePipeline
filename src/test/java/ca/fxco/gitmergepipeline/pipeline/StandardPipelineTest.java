package ca.fxco.gitmergepipeline.pipeline;

import ca.fxco.gitmergepipeline.merge.*;
import ca.fxco.gitmergepipeline.rule.Rule;
import org.eclipse.jgit.api.Git;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the StandardPipeline class which executes operations in sequence.
 * Tests include executing with no steps, successful steps, failing steps,
 * non-applicable steps, multiple steps, and unknown operations.
 *
 * @author FX
 */
class StandardPipelineTest {

    private MergeOperationRegistry operationRegistry;
    private MergeContext context;
    private Rule alwaysAppliesRule;
    private Rule neverAppliesRule;

    @BeforeEach
    void setUp() {
        // Create a mock operation registry
        operationRegistry = new MergeOperationRegistry();

        // Create a mock context
        context = new MergeContext(
                Paths.get("base.txt"),
                Paths.get("current.txt"),
                Paths.get("other.txt"),
                "test.txt"
        );

        // Create mock rules
        alwaysAppliesRule = new Rule() {
            @Override
            public boolean applies(MergeContext context) {
                return true;
            }

            @Override
            public boolean applies(GitMergeContext context) {
                return true;
            }

            @Override
            public String getDescription() {
                return "Always applies";
            }
        };

        neverAppliesRule = new Rule() {
            @Override
            public boolean applies(MergeContext context) {
                return false;
            }

            @Override
            public boolean applies(GitMergeContext context) {
                return false;
            }

            @Override
            public String getDescription() {
                return "Never applies";
            }
        };

        // Create mock operations
        MergeOperation successOperation = new MergeOperation() {
            @Override
            public String getName() {
                return "success";
            }

            @Override
            public String getDescription() {
                return "Always succeeds";
            }

            @Override
            public MergeResult execute(MergeContext context, List<String> parameters) {
                return MergeResult.success("Success", null);
            }

            @Override
            public MergeResult executeBatched(Git git, GitMergeContext context, List<String> parameters) {
                return MergeResult.success("Success", null);
            }
        };

        MergeOperation failureOperation = new MergeOperation() {
            @Override
            public String getName() {
                return "failure";
            }

            @Override
            public String getDescription() {
                return "Always fails";
            }

            @Override
            public MergeResult execute(MergeContext context, List<String> parameters) {
                return MergeResult.error("Failure", new RuntimeException("Test failure"));
            }

            @Override
            public MergeResult executeBatched(Git git, GitMergeContext context, List<String> parameters) {
                return MergeResult.error("Failure", new RuntimeException("Test failure"));
            }
        };

        // Register the mock operations
        operationRegistry.registerOperation(successOperation);
        operationRegistry.registerOperation(failureOperation);
    }

    @Test
    void executeWithNoSteps() throws IOException {
        StandardPipeline pipeline = new StandardPipeline("Test Pipeline", Collections.emptyList(), operationRegistry);

        MergeResult result = pipeline.execute(context);

        assertTrue(result.isSuccess());
        assertEquals("Pipeline executed successfully", result.getMessage());
    }

    @Test
    void executeWithSuccessfulStep() throws IOException {
        Pipeline.Step step = new Pipeline.Step(alwaysAppliesRule, "success", Collections.emptyList());
        StandardPipeline pipeline = new StandardPipeline("Test Pipeline", Collections.singletonList(step), operationRegistry);

        MergeResult result = pipeline.execute(context);

        assertTrue(result.isSuccess());
        assertEquals("Pipeline executed successfully", result.getMessage());
    }

    @Test
    void executeWithFailingStep() throws IOException {
        Pipeline.Step step = new Pipeline.Step(alwaysAppliesRule, "failure", Collections.emptyList());
        StandardPipeline pipeline = new StandardPipeline("Test Pipeline", Collections.singletonList(step), operationRegistry);

        MergeResult result = pipeline.execute(context);

        assertTrue(result.isError());
        assertEquals("Failure", result.getMessage());
    }

    @Test
    void executeWithNonApplicableStep() throws IOException {
        Pipeline.Step step = new Pipeline.Step(neverAppliesRule, "failure", Collections.emptyList());
        StandardPipeline pipeline = new StandardPipeline("Test Pipeline", Collections.singletonList(step), operationRegistry);

        MergeResult result = pipeline.execute(context);

        assertTrue(result.isSuccess());
        assertEquals("Pipeline executed successfully", result.getMessage());
    }

    @Test
    void executeWithMultipleSteps() throws IOException {
        Pipeline.Step step1 = new Pipeline.Step(alwaysAppliesRule, "success", Collections.emptyList());
        Pipeline.Step step2 = new Pipeline.Step(alwaysAppliesRule, "success", Collections.emptyList());
        StandardPipeline pipeline = new StandardPipeline("Test Pipeline", Arrays.asList(step1, step2), operationRegistry);

        MergeResult result = pipeline.execute(context);

        assertTrue(result.isSuccess());
        assertEquals("Pipeline executed successfully", result.getMessage());
    }

    @Test
    void executeWithUnknownOperation() throws IOException {
        Pipeline.Step step = new Pipeline.Step(alwaysAppliesRule, "unknown", Collections.emptyList());
        StandardPipeline pipeline = new StandardPipeline("Test Pipeline", Collections.singletonList(step), operationRegistry);

        MergeResult result = pipeline.execute(context);

        assertTrue(result.isError());
        assertEquals("Unknown operation: unknown", result.getMessage());
    }

    @Test
    void getDescription() {
        StandardPipeline pipeline = new StandardPipeline("Test Pipeline", Collections.emptyList(), operationRegistry);

        assertEquals("Standard pipeline: Test Pipeline", pipeline.getDescription());
    }
}
