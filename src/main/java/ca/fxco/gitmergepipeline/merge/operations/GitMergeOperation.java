package ca.fxco.gitmergepipeline.merge.operations;

import ca.fxco.gitmergepipeline.merge.MergeContext;
import ca.fxco.gitmergepipeline.merge.MergeOperation;
import ca.fxco.gitmergepipeline.merge.MergeResult;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Stream;

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
        logger.info("Executing Git merge operation");

        String mergeStrategy = DEFAULT_MERGE_STRATEGY;
        if (parameters != null && !parameters.isEmpty()) {
            mergeStrategy = parameters.getFirst();
        }

        Path basePath = context.getBasePath();
        Path currentPath = context.getCurrentPath();
        Path otherPath = context.getOtherPath();

        // Create a temporary directory for the Git repository
        Path tempDir = Files.createTempDirectory("git-merge-");
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
                logger.info("Git merge successful");
                return MergeResult.success("Git merge successful", outputPath);
            } else {
                logger.info("Git merge resulted in conflicts: {}", mergeResult.getMergeStatus());
                return MergeResult.conflict("Git merge resulted in conflicts: " + mergeResult.getMergeStatus());
            }
        } catch (GitAPIException e) {
            logger.error("Error during Git merge", e);
            return MergeResult.error("Error during Git merge: " + e.getMessage(), e);
        } finally {
            // Clean up the temporary directory
            deleteDirectory(tempDir);
        }
    }

    private void deleteDirectory(Path directory) {
        try (Stream<Path> stream = Files.walk(directory)) {
                stream.sorted((a, b) -> -a.compareTo(b))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            logger.warn("Failed to delete {}: {}", path, e.getMessage());
                        }
                    });
        } catch (IOException e) {
            logger.warn("Failed to delete directory {}: {}", directory, e.getMessage());
        }
    }
}
