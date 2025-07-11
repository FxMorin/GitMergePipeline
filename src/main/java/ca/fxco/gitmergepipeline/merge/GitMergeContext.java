package ca.fxco.gitmergepipeline.merge;

import ca.fxco.gitmergepipeline.utils.GitPath;
import org.apache.commons.io.FilenameUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Git Context for a batched merge operation, containing information about the git files being merged
 * and any additional metadata needed for the merge process.
 *
 * @see MergeContext
 * @author FX
 */
public class GitMergeContext {
    private final GitPath basePath;
    private final GitPath currentPath;
    private final GitPath otherPath;
    private final String filePath;
    private final Map<String, Object> attributes;

    /**
     * Creates a new git merge context for a merge operation.
     *
     * @param basePath    Path to the base version of the file
     * @param currentPath Path to the current version of the file
     * @param otherPath   Path to the other version of the file
     * @param filePath    Path to the file relative to the working directory
     */
    public GitMergeContext(GitPath basePath, GitPath currentPath, GitPath otherPath, String filePath) {
        this.basePath = basePath;
        this.currentPath = currentPath;
        this.otherPath = otherPath;
        this.filePath = filePath;
        this.attributes = new HashMap<>();
    }

    /**
     * Gets the path to the base version of the file.
     *
     * @return Path to the base version
     */
    public GitPath getBasePath() {
        return basePath;
    }

    /**
     * Gets the path to the current version of the file.
     *
     * @return Path to the current version
     */
    public GitPath getCurrentPath() {
        return currentPath;
    }

    /**
     * Gets the path to the other version of the file.
     *
     * @return Path to the other version
     */
    public GitPath getOtherPath() {
        return otherPath;
    }

    /**
     * Gets the path to the file relative to the working directory.
     *
     * @return Relative path to the file
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * Gets the filename of the file being merged.
     *
     * @return Filename of the file
     */
    public String getFileName() {
        return FilenameUtils.getName(filePath);
    }

    /**
     * Gets the file extension of the file being merged.
     *
     * @return File extension of the file
     */
    public String getFileExtension() {
        return FilenameUtils.getExtension(filePath);
    }

    /**
     * Sets an attribute in the merge context.
     *
     * @param key Attribute key
     * @param value Attribute value
     */
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    /**
     * Gets an attribute from the merge context.
     *
     * @param key Attribute key
     * @return Attribute value, or null if the attribute doesn't exist
     */
    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    /**
     * Gets an attribute from the merge context, with a default value if the attribute doesn't exist.
     *
     * @param key Attribute key
     * @param defaultValue Default value to return if the attribute doesn't exist
     * @return Attribute value, or the default value if the attribute doesn't exist
     */
    public Object getAttribute(String key, Object defaultValue) {
        return attributes.getOrDefault(key, defaultValue);
    }

    /**
     * Gets all attributes in the merge context.
     *
     * @return Map of attribute keys to values
     */
    public Map<String, Object> getAttributes() {
        return new HashMap<>(attributes);
    }
}
