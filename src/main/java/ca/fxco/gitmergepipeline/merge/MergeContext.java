package ca.fxco.gitmergepipeline.merge;

import org.apache.commons.io.FilenameUtils;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Context for a merge operation, containing information about the files being merged
 * and any additional metadata needed for the merge process.
 *
 * @author FX
 */
public class MergeContext {
    private final Path basePath;
    private final Path currentPath;
    private final Path otherPath;
    private final String filePath;
    private final Map<String, Object> attributes;
    
    /**
     * Creates a new merge context for a merge driver operation.
     * 
     * @param basePath Path to the base version of the file
     * @param currentPath Path to the current version of the file
     * @param otherPath Path to the other version of the file
     * @param filePath Path to the file relative to the working directory
     */
    public MergeContext(Path basePath, Path currentPath, Path otherPath, String filePath) {
        this.basePath = basePath;
        this.currentPath = currentPath;
        this.otherPath = otherPath;
        this.filePath = filePath;
        this.attributes = new HashMap<>();
    }
    
    /**
     * Creates a new merge context for a merge tool operation.
     * 
     * @param localPath Path to the local version of the file
     * @param remotePath Path to the remote version of the file
     * @param mergedPath Path to the merged version of the file
     */
    public static MergeContext forMergeTool(Path localPath, Path remotePath, Path mergedPath) {
        MergeContext context = new MergeContext(null, localPath, remotePath, mergedPath.toString());
        context.setAttribute("mergedPath", mergedPath);
        return context;
    }
    
    /**
     * Gets the path to the base version of the file.
     * 
     * @return Path to the base version
     */
    public Path getBasePath() {
        return basePath;
    }
    
    /**
     * Gets the path to the current version of the file.
     * 
     * @return Path to the current version
     */
    public Path getCurrentPath() {
        return currentPath;
    }
    
    /**
     * Gets the path to the other version of the file.
     * 
     * @return Path to the other version
     */
    public Path getOtherPath() {
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