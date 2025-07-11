package ca.fxco.gitmergepipeline.merge.operations;

import ca.fxco.gitmergepipeline.merge.*;
import ca.fxco.gitmergepipeline.utils.GitPath;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.merge.Merger;
import org.eclipse.jgit.merge.ThreeWayMerger;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

/**
 * A merge operation that uses JGit to perform a merge.
 * This operation uses the Git merge algorithm to merge files.
 *
 * @author FX
 */
public class GitMergeOperation implements MergeOperation {
    private static final Logger logger = LoggerFactory.getLogger(GitMergeOperation.class);

    private static final String DEFAULT_MERGE_STRATEGY = "recursive";

    @Override
    public String getName() {
        return "git-merge";
    }

    @Override
    public String getDescription() {
        return "Merges files using the Git merge algorithm";
    }

    @Override
    public MergeResult execute(MergeContext context, List<String> parameters)
            throws IOException {
        logger.debug("Executing Git merge operation");

        String mergeStrategy = DEFAULT_MERGE_STRATEGY;
        if (parameters != null && !parameters.isEmpty()) {
            mergeStrategy = parameters.getFirst();
        }

        Path basePath = context.getBasePath();
        Path currentPath = context.getCurrentPath();
        Path otherPath = context.getOtherPath();

        // Create a temporary directory for the Git repository
        Path tempDir = Files.createTempDirectory("git-merge-");
        tempDir.toFile().deleteOnExit();
        try {
            // Initialize a Git repository
            org.eclipse.jgit.api.MergeResult mergeResult;
            try (Git git = Git.init().setDirectory(tempDir.toFile()).call()) {

                // Create the initial commit with the base content
                Path baseFile = tempDir.resolve("base");
                if (basePath != null) {
                    Files.copy(basePath, baseFile, StandardCopyOption.REPLACE_EXISTING);
                } else {
                    Files.createFile(baseFile);
                }

                git.add().addFilepattern("base").call();
                RevCommit baseCommit = git.commit().setMessage("Base").call();

                // Create a branch for the current content
                git.checkout().setCreateBranch(true).setName("current").call();
                Files.copy(currentPath, baseFile, StandardCopyOption.REPLACE_EXISTING);
                git.add().addFilepattern("base").call();
                git.commit().setMessage("Current").call();

                // Create a branch for the other content
                git.checkout().setStartPoint(baseCommit).setCreateBranch(true).setName("other").call();
                Files.copy(otherPath, baseFile, StandardCopyOption.REPLACE_EXISTING);
                git.add().addFilepattern("base").call();
                RevCommit otherCommit = git.commit().setMessage("Other").call();

                // Merge the current and other branches
                git.checkout().setName("current").call();

                MergeStrategy strategy = MergeStrategy.get(mergeStrategy);
                if (strategy == null) {
                    logger.error("Unknown merge strategy: {}", mergeStrategy);
                    return MergeResult.error("Unknown merge strategy: " + mergeStrategy, null);
                }

                mergeResult = git.merge()
                        .include(otherCommit)
                        .setStrategy(strategy)
                        .setCommit(false)
                        .setFastForward(MergeCommand.FastForwardMode.NO_FF)
                        .call();
            }

            // Get the merged content
            Path mergedFile = tempDir.resolve("base");
            Path outputPath = context.getCurrentPath();

            // If this is a merge tool operation, use the merged path
            Object mergedPathObj = context.getAttribute("mergedPath");
            if (mergedPathObj instanceof Path path) {
                outputPath = path;
            }

            // Copy the file regardless of whether the merge was successful or resulted in conflicts
            Files.copy(mergedFile, outputPath, StandardCopyOption.REPLACE_EXISTING);

            if (mergeResult.getMergeStatus().isSuccessful()) {
                logger.debug("Git merge successful");
                return MergeResult.success("Git merge successful", outputPath);
            } else {
                logger.debug("Git merge resulted in conflicts: {}", mergeResult.getMergeStatus());
                return MergeResult.conflict("Git merge resulted in conflicts: " + mergeResult.getMergeStatus());
            }
        } catch (GitAPIException e) {
            logger.error("Error during Git merge", e);
            return MergeResult.error("Error during Git merge: " + e.getMessage(), e);
        }
    }

    @Override
    public MergeResult executeBatched(Git git, GitMergeContext context, List<String> parameters) throws IOException {
        logger.debug("Executing batched Git merge operation");

        String mergeStrategy = DEFAULT_MERGE_STRATEGY;
        if (parameters != null && !parameters.isEmpty()) {
            mergeStrategy = parameters.getFirst();
        }

        GitPath base = context.getBasePath();
        GitPath current = context.getCurrentPath();
        GitPath other = context.getOtherPath();
        String filePath = context.getFilePath();

        MergeStrategy strategy = MergeStrategy.get(mergeStrategy);
        if (strategy == null) {
            logger.error("Unknown merge strategy: {}", mergeStrategy);
            return MergeResult.error("Unknown merge strategy: " + mergeStrategy, null);
        }

        try (ObjectReader reader = git.getRepository().newObjectReader()) {
            Merger rawMerger = strategy.newMerger(git.getRepository(), true);

            ObjectId mergedTree;

            if (rawMerger instanceof ThreeWayMerger merger) {
                merger.setBase(base.getCommit());
                boolean success = merger.merge(current.getCommit(), other.getCommit());
                mergedTree = merger.getResultTreeId();

                if (mergedTree == null) {
                    return MergeResult.error("Three-way merge failed with null result tree", null);
                }

                return extractMergedFileFromTree(
                        git, reader, mergedTree, filePath, current, context.getAttributes(), success
                );
            } else {
                logger.warn("Strategy did not return a ThreeWayMerger, falling back to two sequential TwoWayMergers");

                // First merge base + current
                Merger firstMerger = strategy.newMerger(git.getRepository(), true);
                if (!firstMerger.merge(base.getCommit(), current.getCommit())) {
                    return MergeResult.error("Two-way merge (base -> current) failed", null);
                }
                ObjectId intermediateTree = firstMerger.getResultTreeId();
                if (intermediateTree == null) {
                    return MergeResult.error("Intermediate merge produced null tree", null);
                }

                // Second merge intermediate + other
                Merger secondMerger = strategy.newMerger(git.getRepository(), true);
                boolean success = secondMerger.merge(intermediateTree, other.getCommit());
                mergedTree = secondMerger.getResultTreeId();

                if (mergedTree == null) {
                    return MergeResult.error("Final merge (intermediate -> other) produced null tree", null);
                }

                return extractMergedFileFromTree(
                        git, reader, mergedTree, filePath, current, context.getAttributes(), success
                );
            }

        } catch (Exception e) {
            logger.error("Error during Git merge", e);
            return MergeResult.error("Error during Git merge: " + e.getMessage(), e);
        }
    }

    private MergeResult extractMergedFileFromTree(
            Git git,
            ObjectReader reader,
            ObjectId treeId,
            String filePath,
            GitPath defaultOutputPath,
            Map<String, Object> attributes,
            boolean success
    ) throws IOException {
        try (TreeWalk treeWalk = new TreeWalk(git.getRepository())) {
            treeWalk.addTree(treeId);
            treeWalk.setRecursive(true);
            treeWalk.setFilter(PathFilter.create(filePath));

            GitPath outputPath = defaultOutputPath;
            Object mergedPathObj = attributes.get("mergedPath");
            if (mergedPathObj instanceof Path path) {
                outputPath = new GitPath(outputPath.getCommit(), path);
            }

            if (!treeWalk.next()) {
                Files.deleteIfExists(outputPath.getPath()); // TODO: I don't believe this is correct
                if (success) {
                    logger.debug("Git merge successful");
                    return GitMergeResult.success("Git merge successful", outputPath);
                } else {
                    logger.debug("Git merge resulted in conflicts");
                    return MergeResult.conflict("Git merge resulted in conflicts - " + outputPath);
                }
            }

            ObjectId blobId = treeWalk.getObjectId(0);
            ObjectLoader loader = reader.open(blobId);
            byte[] mergedBytes = loader.getBytes();

            Files.write(outputPath.getPath(), mergedBytes);

            if (success) {
                logger.debug("Git merge successful");
                return GitMergeResult.success("Git merge successful", outputPath);
            } else {
                logger.debug("Git merge resulted in conflicts");
                return MergeResult.conflict("Git merge resulted in conflicts - " + outputPath);
            }
        }
    }
}
