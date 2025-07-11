package ca.fxco.gitmergepipeline.merge;

import ca.fxco.gitmergepipeline.utils.GitPath;
import org.eclipse.jgit.revwalk.RevCommit;

import java.nio.file.Path;

/**
 * Represents the result of a git merge operation.
 *
 * @author FX
 */
public class GitMergeResult extends MergeResult {
    private final RevCommit commit;

    /**
     * Creates a new merge result.
     *
     * @param status     The status of the merge
     * @param message    A message describing the result
     * @param outputPath The path to the output file, if any
     * @param error      The error that occurred, if any
     * @param commit     The commit associated with the merge result
     */
    public GitMergeResult(Status status, String message, Path outputPath, Exception error, RevCommit commit) {
        super(status, message, outputPath, error);
        this.commit = commit;
    }

    /**
     * The commit associated with the merge result.
     *
     * @return The commit
     */
    public RevCommit getCommit() {
        return commit;
    }
    
    /**
     * Creates a successful merge result.
     * 
     * @param message    A message describing the result
     * @param outputPath The path to the output file
     * @param commit     The commit associated with the merge result
     * @return A new merge result
     */
    public static GitMergeResult success(String message, Path outputPath, RevCommit commit) {
        return new GitMergeResult(Status.SUCCESS, message, outputPath, null, commit);
    }

    /**
     * Creates a successful merge result.
     *
     * @param message    A message describing the result
     * @param outputPath The git path to the output file
     * @return A new merge result
     */
    public static GitMergeResult success(String message, GitPath outputPath) {
        return new GitMergeResult(Status.SUCCESS, message, outputPath.getPath(), null, outputPath.getCommit());
    }
}