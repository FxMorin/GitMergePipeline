package ca.fxco.gitmergepipeline.merge;

import ca.fxco.gitmergepipeline.config.PipelineConfiguration;
import ca.fxco.gitmergepipeline.pipeline.Pipeline;
import ca.fxco.gitmergepipeline.pipeline.StandardPipeline;
import ca.fxco.gitmergepipeline.rule.FilePatternRule;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the MergeBranches class which handles merging branches using pipelines.
 * Tests include successful merges, failing merges, merges with no pipelines,
 * and merges with file pattern rules.
 *
 * @author FX
 */
class MergeBranchesTest {

    private static final String BRANCH1 = "branch1";
    private static final String BRANCH2 = "branch2";
    private static final String BRANCH3 = "branch3";
    private static final String BRANCH4 = "branch4";
    private static final String BASE_BRANCH = "main";

    @TempDir
    Path tempDir;

    private PipelineConfiguration configuration;
    private MergeBranches mergeBranches;

    @BeforeEach
    void setUp() throws IOException {
        try (Git git = Git.init().setDirectory(tempDir.toFile()).setInitialBranch("main").call()) {
            Files.writeString(tempDir.resolve("file.txt"), "base\n");
            git.add().addFilepattern("file.txt").call();
            git.commit().setMessage("base commit").call();

            git.branchCreate().setName(BRANCH1).call();
            git.branchCreate().setName(BRANCH2).call();
            git.branchCreate().setName(BRANCH3).call();

            // branch1: modify file
            git.checkout().setName(BRANCH1).call();
            Files.writeString(tempDir.resolve("file.txt"), "branch1 change\n");
            git.add().addFilepattern("file.txt").call();
            git.commit().setMessage("branch1 commit").call();

            // branch2: modify file differently
            git.checkout().setName(BRANCH2).call();
            Files.writeString(tempDir.resolve("file.txt"), "branch2 change\n");
            git.add().addFilepattern("file.txt").call();
            git.commit().setMessage("branch2 commit").call();

            // branch3: modify file differently
            git.checkout().setName(BRANCH3).call();
            Files.writeString(tempDir.resolve("file.txt"), "branch3 change\n");
            git.add().addFilepattern("file.txt").call();
            RevCommit commit = git.commit().setMessage("branch3 commit").call();

            git.branchCreate().setStartPoint(commit).setName(BRANCH4).call();

            // branch4: modify file differently
            git.checkout().setName(BRANCH4).call();
            Files.writeString(tempDir.resolve("file.txt"), "branch4 change\n");
            git.add().addFilepattern("file.txt").call();
            git.commit().setMessage("branch4 commit").call();

            git.checkout().setName(BASE_BRANCH).call();
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }

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
        configuration = new PipelineConfiguration(new HashMap<>(), Arrays.asList(successPipeline, failurePipeline));

        // Create the merge branches
        mergeBranches = new MergeBranches(configuration);
    }

    @Test
    void mergeWithSuccessfulPipeline() {
        // The first pipeline in the configuration is the success pipeline
        boolean result = mergeBranches.merge(BASE_BRANCH, tempDir.toFile(), List.of(BRANCH1, BRANCH2));

        assertTrue(result);

        // Try with more branches
        result = mergeBranches.merge(BASE_BRANCH, tempDir.toFile(), List.of(BRANCH1, BRANCH2, BRANCH3, BRANCH4));

        assertTrue(result);
    }

    @Test
    void mergeWithFailingPipeline() {
        // Create a configuration with only the failure pipeline
        PipelineConfiguration failureConfig = new PipelineConfiguration(
                new HashMap<>(),
                Collections.singletonList(configuration.getPipelines().get(1))
        );

        MergeBranches failureBranches = new MergeBranches(failureConfig);

        boolean result = failureBranches.merge(BASE_BRANCH, tempDir.toFile(), List.of(BRANCH1, BRANCH2));

        assertFalse(result);

        // Try with more branches
        result = failureBranches.merge(BASE_BRANCH, tempDir.toFile(), List.of(BRANCH1, BRANCH2, BRANCH3, BRANCH4));

        assertFalse(result);
    }

    @Test
    void mergeWithNoPipelines() {
        // Create a configuration with no pipelines
        PipelineConfiguration emptyConfig = new PipelineConfiguration();

        MergeBranches emptyBranches = new MergeBranches(emptyConfig);

        boolean result = emptyBranches.merge(BASE_BRANCH, tempDir.toFile(), List.of(BRANCH1, BRANCH2));

        assertFalse(result);

        // Try with more branches
        result = emptyBranches.merge(BASE_BRANCH, tempDir.toFile(), List.of(BRANCH1, BRANCH2, BRANCH3, BRANCH4));

        assertFalse(result);
    }

    @Test
    void mergeWithFilePatternRule() {
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
        PipelineConfiguration ruleConfig = new PipelineConfiguration(
                new HashMap<>(),
                Arrays.asList(javaPipeline, txtPipeline)
        );

        MergeBranches ruleBranches = new MergeBranches(ruleConfig);

        // The txt pipeline should be selected for a .txt file
        boolean result = ruleBranches.merge(BASE_BRANCH, tempDir.toFile(), List.of(BRANCH1, BRANCH2));

        assertTrue(result);

        // Try with more branches
        result = ruleBranches.merge(BASE_BRANCH, tempDir.toFile(), List.of(BRANCH1, BRANCH2, BRANCH3, BRANCH4));

        assertTrue(result);
    }
}
