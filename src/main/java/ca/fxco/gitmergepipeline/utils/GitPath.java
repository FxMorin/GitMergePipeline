package ca.fxco.gitmergepipeline.utils;

import org.eclipse.jgit.revwalk.RevCommit;

import java.nio.file.Path;

/**
 * A class representing a path in a git repo.
 *
 * @author FX
 */
public class GitPath {

    private final RevCommit commit;
    private final Path path;

    /**
     * Creates a new GitFile.
     *
     * @param commit The commit that contains the file
     * @param path The path to the file
     */
    public GitPath(RevCommit commit, Path path) {
        this.commit = commit;
        this.path = path;
    }

    /**
     * Gets the commit that contains the file.
     *
     * @return The commit
     */
    public RevCommit getCommit() {
        return commit;
    }

    /**
     * Gets the path to the file.
     *
     * @return The path
     */
    public Path getPath() {
        return path;
    }
}
