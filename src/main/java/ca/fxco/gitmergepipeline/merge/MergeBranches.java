package ca.fxco.gitmergepipeline.merge;

import ca.fxco.gitmergepipeline.config.PipelineConfiguration;
import ca.fxco.gitmergepipeline.pipeline.Pipeline;
import ca.fxco.gitmergepipeline.utils.GitPath;
import ca.fxco.gitmergepipeline.utils.GitUtils;
import org.eclipse.jgit.annotations.Nullable;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Re-Implementation of `git merge` using the GitMergePipeline system.
 * This allows you to merge branches using the GitMergePipeline system, while not having to rely on `git merge`.
 *
 * @author FX
 */
public class MergeBranches extends Merger {
    private static final Logger logger = LoggerFactory.getLogger(MergeBranches.class);

    /**
     * Creates a new merge with the specified configuration.
     *
     * @param configuration The pipeline configuration to use
     */
    public MergeBranches(PipelineConfiguration configuration) {
        super(configuration);
    }

    /**
     * Merges the specified branches.
     * Uses Octopus strategy.
     *
     * @param baseBranch The base branch to merge from, or null if the base branch should be determined automatically
     * @param repoDir    The repo directory to use, or null if the current directory should be used
     * @param branches   The branches to merge
     * @return {@code true} if the merge was successful, otherwise {@code false}
     */
    public boolean merge(@Nullable String baseBranch, @Nullable File repoDir, Collection<String> branches) {
        if (repoDir == null) {
            repoDir = new File(".");
        }
        try (Git git = Git.open(repoDir)) {
            Repository repo = git.getRepository();

            // Get commits
            List<RevCommit> branchCommits = new ArrayList<>();
            try (RevWalk revWalk = new RevWalk(repo)) {
                revWalk.setRetainBody(false);
                for (String branch : branches) {
                    ObjectId branchId = repo.resolve(branch);
                    if (branchId == null) {
                        logger.error("Branch not found: " + branch);
                        return false;
                    }
                    branchCommits.add(revWalk.parseCommit(branchId));
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            RevCommit baseCommit;
            if (baseBranch != null) {
                ObjectId baseId = repo.resolve(baseBranch);
                if (baseId == null) {
                    logger.error("Base branch not found: " + baseBranch);
                    return false;
                }
                try (RevWalk revWalk = new RevWalk(repo)) {
                    revWalk.setRetainBody(false);
                    baseCommit = revWalk.parseCommit(baseId);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    return false;
                }
            } else {
                baseCommit = GitUtils.findCommonAncestor(repo, branchCommits);
                if (baseCommit == null) {
                    logger.error("Could not determine common ancestor.");
                    return false;
                }
            }

            PipelineConfiguration configuration = getConfiguration();
            List<DiffEntry> changedFiles = GitUtils.getChangedFiles(configuration, repo, baseCommit, branchCommits);
            logger.info("Merging {} files across {} branches.", changedFiles.size(), branches.size());

            for (DiffEntry diff : changedFiles) {
                String filePath = diff.getNewPath();
                Path target = Path.of(repoDir.getPath() + "/" + filePath);
                GitPath basePath = new GitPath(baseCommit, target);

                GitPath currentPath = basePath;
                for (RevCommit commit : branchCommits) {
                    GitPath otherPath = new GitPath(commit, target);
                    GitMergeContext context = new GitMergeContext(basePath, currentPath, otherPath, filePath);
                    Pipeline pipeline = configuration.findPipeline(context);

                    if (pipeline == null) {
                        logger.error("No pipeline found for file: " + filePath);
                        return false;
                    }

                    MergeResult result = pipeline.executeBatched(git, context);
                    if (!result.isSuccess()) {
                        logger.error("Merge conflict in file: " + filePath);
                        return false;
                    }

                    if (result.getOutputPath() != null) {
                        if (result instanceof GitMergeResult gitMergeResult) {
                            currentPath = new GitPath(gitMergeResult.getCommit(), result.getOutputPath());
                        } else {
                            currentPath = new GitPath(currentPath.getCommit(), result.getOutputPath());
                        }
                    }
                }

                // Final merged result is in currentPath
                if (diff.getChangeType() != DiffEntry.ChangeType.DELETE) { // Target = /dev/null
                    if (Files.exists(currentPath.getPath())) { // TODO: Figure out why this happens
                        GitUtils.copyFileToWorkingDirectory(currentPath.getPath(), target);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }
}
