package ca.fxco.gitmergepipeline.merge;

import ca.fxco.gitmergepipeline.config.PipelineConfiguration;
import ca.fxco.gitmergepipeline.utils.GitUtils;
import org.eclipse.jgit.annotations.Nullable;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Re-Implementation of `git merge` using the GitMergePipeline system.
 * This allows you to merge branches using the GitMergePipeline system, while not having to rely on `git merge`.
 *
 * @author FX
 */
public class MergeBranches {
    private static final Logger logger = LoggerFactory.getLogger(MergeBranches.class);

    private final PipelineConfiguration configuration;

    /**
     * Creates a new merge with the specified configuration.
     *
     * @param configuration The pipeline configuration to use
     */
    public MergeBranches(PipelineConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Merges the specified branches.
     *
     * @param branch1    The first branch to merge
     * @param branch2    The second branch to merge
     * @param baseBranch The base branch to merge from, or null if the base branch should be determined automatically
     * @param repoDir    The repo directory to use, or null if the current directory should be used
     * @return {@code true} if the merge was successful, otherwise {@code false}
     */
    public boolean merge(String branch1, String branch2, @Nullable String baseBranch, @Nullable File repoDir) {
        if (repoDir == null) {
            repoDir = new File(".");
        }
        try (Git git = Git.open(repoDir)) {
            Repository repo = git.getRepository();
            MergeDriver driver = new MergeDriver(configuration);

            RevCommit base = GitUtils.getCommonAncestor(repo, branch1, branch2, baseBranch);
            RevCommit commit1 = GitUtils.getCommit(repo, branch1);
            RevCommit commit2 = GitUtils.getCommit(repo, branch2);

            List<DiffEntry> diffs = GitUtils.getChangedFiles(repo, base, commit1, commit2);

            boolean allSuccessful = true;
            for (DiffEntry diff : diffs) {
                String path = diff.getNewPath();

                Path basePath = GitUtils.checkoutFile(repo, base, path);
                Path branch1Path = GitUtils.checkoutFile(repo, commit1, path);
                Path branch2Path = GitUtils.checkoutFile(repo, commit2, path);
                boolean success = driver.merge(basePath, branch1Path, branch2Path, path);

                if (!success) {
                    allSuccessful = false;
                }
            }
            return allSuccessful;
        } catch (IOException | GitAPIException e) {
            logger.error("Error during merge mode", e);
            return false;
        }
    }
}
