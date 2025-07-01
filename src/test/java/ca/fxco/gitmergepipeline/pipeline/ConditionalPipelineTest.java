package ca.fxco.gitmergepipeline.pipeline;

import ca.fxco.gitmergepipeline.merge.MergeContext;
import ca.fxco.gitmergepipeline.merge.MergeResult;
import ca.fxco.gitmergepipeline.rule.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the ConditionalPipeline class which executes different pipelines based on rules.
 * Tests include executing with matching branches, non-matching branches, multiple branches,
 * and with/without default pipelines.
 *
 * @author FX
 */
class ConditionalPipelineTest {

    private MergeContext context;
    private Rule alwaysAppliesRule;
    private Rule neverAppliesRule;
    private Pipeline successPipeline;
    private Pipeline failurePipeline;

    @BeforeEach
    void setUp() {
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
            public String getDescription() {
                return "Never applies";
            }
        };

        // Create mock pipelines
        successPipeline = new Pipeline() {
            @Override
            public MergeResult execute(MergeContext context) {
                return MergeResult.success("Success from mock pipeline", null);
            }

            @Override
            public String getDescription() {
                return "Success Pipeline";
            }
        };

        failurePipeline = new Pipeline() {
            @Override
            public MergeResult execute(MergeContext context) {
                return MergeResult.error("Failure from mock pipeline", new RuntimeException("Test failure"));
            }

            @Override
            public String getDescription() {
                return "Failure Pipeline";
            }
        };
    }

    @Test
    void executeWithMatchingBranch() throws IOException {
        ConditionalPipeline.Branch branch = new ConditionalPipeline.Branch(alwaysAppliesRule, successPipeline);
        ConditionalPipeline pipeline = new ConditionalPipeline("Test Pipeline", Collections.singletonList(branch), null);

        MergeResult result = pipeline.execute(context);

        assertTrue(result.isSuccess());
        assertEquals("Success from mock pipeline", result.getMessage());
    }

    @Test
    void executeWithNonMatchingBranch() throws IOException {
        ConditionalPipeline.Branch branch = new ConditionalPipeline.Branch(neverAppliesRule, failurePipeline);
        ConditionalPipeline pipeline = new ConditionalPipeline("Test Pipeline", Collections.singletonList(branch), successPipeline);

        MergeResult result = pipeline.execute(context);

        assertTrue(result.isSuccess());
        assertEquals("Success from mock pipeline", result.getMessage());
    }

    @Test
    void executeWithMultipleBranches() throws IOException {
        ConditionalPipeline.Branch branch1 = new ConditionalPipeline.Branch(neverAppliesRule, failurePipeline);
        ConditionalPipeline.Branch branch2 = new ConditionalPipeline.Branch(alwaysAppliesRule, successPipeline);
        ConditionalPipeline pipeline = new ConditionalPipeline("Test Pipeline", Arrays.asList(branch1, branch2), failurePipeline);

        MergeResult result = pipeline.execute(context);

        assertTrue(result.isSuccess());
        assertEquals("Success from mock pipeline", result.getMessage());
    }

    @Test
    void executeWithNoMatchingBranchAndNoDefaultPipeline() throws IOException {
        ConditionalPipeline.Branch branch = new ConditionalPipeline.Branch(neverAppliesRule, failurePipeline);
        ConditionalPipeline pipeline = new ConditionalPipeline("Test Pipeline", Collections.singletonList(branch), null);

        MergeResult result = pipeline.execute(context);

        assertTrue(result.isSuccess());
        assertEquals("No branch rule applies and no default pipeline", result.getMessage());
    }

    @Test
    void executeWithNoMatchingBranchAndDefaultPipeline() throws IOException {
        ConditionalPipeline.Branch branch = new ConditionalPipeline.Branch(neverAppliesRule, failurePipeline);
        ConditionalPipeline pipeline = new ConditionalPipeline("Test Pipeline", Collections.singletonList(branch), successPipeline);

        MergeResult result = pipeline.execute(context);

        assertTrue(result.isSuccess());
        assertEquals("Success from mock pipeline", result.getMessage());
    }

    @Test
    void getDescription() {
        ConditionalPipeline pipeline = new ConditionalPipeline("Test Pipeline", Collections.emptyList(), null);

        assertEquals("Conditional pipeline: Test Pipeline", pipeline.getDescription());
    }

    @Test
    void getBranches() {
        ConditionalPipeline.Branch branch = new ConditionalPipeline.Branch(alwaysAppliesRule, successPipeline);
        ConditionalPipeline pipeline = new ConditionalPipeline("Test Pipeline", Collections.singletonList(branch), null);

        assertEquals(1, pipeline.getBranches().size());
        assertSame(alwaysAppliesRule, pipeline.getBranches().get(0).getRule());
        assertSame(successPipeline, pipeline.getBranches().get(0).getPipeline());
    }

    @Test
    void getDefaultPipeline() {
        ConditionalPipeline pipeline = new ConditionalPipeline("Test Pipeline", Collections.emptyList(), successPipeline);

        assertSame(successPipeline, pipeline.getDefaultPipeline());
    }
}
