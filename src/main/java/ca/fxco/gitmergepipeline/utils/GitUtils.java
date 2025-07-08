package ca.fxco.gitmergepipeline.utils;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility class for simplifying common git operations.
 *
 * @author FX
 */
public class GitUtils {
    private static final Logger logger = LoggerFactory.getLogger(GitUtils.class);

    /**
     * Find the common ancestor of the given commits.
     *
     * @param repo    The repository to look in
     * @param commits The commits to find the common ancestor of
     * @return The common ancestor commit, or {@code null} if there is no common ancestor
     * @throws IOException If an I/O error occurs when traversing through the commits
     */
    public static RevCommit findCommonAncestor(Repository repo, List<RevCommit> commits) throws IOException {
        if (commits.isEmpty()) {
            return null;
        }
        if (commits.size() == 1) {
            return commits.getFirst();
        }

        try (RevWalk walk = new RevWalk(repo)) {
            walk.setRetainBody(false);
            walk.setRevFilter(RevFilter.MERGE_BASE);
            for (RevCommit commit : commits) {
                walk.markStart(walk.parseCommit(commit));
            }
            return walk.next();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * Gets the list of changed files between the given commits.
     *
     * @param repo     The repository to look in
     * @param base     The base commit to compare against
     * @param branches The commits to compare against the base commit
     * @return The list of changed files
     * @throws IOException If an I/O error occurs when traversing through the commits
     */
    public static List<DiffEntry> getChangedFiles(Repository repo, RevCommit base, List<RevCommit> branches)
            throws IOException {
        Set<String> seenPaths = new HashSet<>();
        List<DiffEntry> allDiffs = new ArrayList<>();

        try (ObjectReader reader = repo.newObjectReader()) {
            CanonicalTreeParser baseTree = new CanonicalTreeParser();
            baseTree.reset(reader, base.getTree().getId());

            for (RevCommit branch : branches) {
                CanonicalTreeParser branchTree = new CanonicalTreeParser();
                branchTree.reset(reader, branch.getTree().getId());

                try (ByteArrayOutputStream out = new ByteArrayOutputStream();
                     DiffFormatter diffFormatter = new DiffFormatter(out)) {
                    diffFormatter.setRepository(repo);
                    List<DiffEntry> diffs = diffFormatter.scan(baseTree, branchTree);
                    for (DiffEntry diff : diffs) {
                        String path = diff.getNewPath();
                        if (seenPaths.add(path)) {
                            allDiffs.add(diff);
                        }
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return allDiffs;
    }

    /**
     * Checks out a file from a commit.
     *
     * @param repo   The repository to look in
     * @param commit The commit to check out the file from
     * @param path   The path to the file to check out
     * @return The path to the checked-out file
     * @throws IOException If an I/O error occurs when traversing through the repo
     */
    public static Path checkoutFile(Repository repo, RevCommit commit, String path) throws IOException {
        Path tempFile = Files.createTempFile("mergefile-", "-" + path.replace('/', '_'));
        try (TreeWalk treeWalk = TreeWalk.forPath(repo, path, commit.getTree())) {
            if (treeWalk == null) {
                return tempFile; // File doesn't exist in this commit
            }
            ObjectId blobId = treeWalk.getObjectId(0);
            Files.write(tempFile, repo.open(blobId).getBytes());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return tempFile;
    }

    /**
     * Copies a file from source to target within the working directory.
     * Ensures the parent directories exist.
     *
     * @param source the path to the source file
     * @param target the destination path in the working directory
     * @throws IOException if an I/O error occurs
     */
    public static void copyFileToWorkingDirectory(Path source, Path target) throws IOException {
        if (!Files.exists(source)) {
            throw new IOException("Source file does not exist: " + source);
        }

        // Ensure target directory exists
        if (target.getParent() != null) {
            Files.createDirectories(target.getParent());
        }

        // Copy the file, replacing existing if necessary
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
    }
}
