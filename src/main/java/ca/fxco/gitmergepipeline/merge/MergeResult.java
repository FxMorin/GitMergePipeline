package ca.fxco.gitmergepipeline.merge;

import java.nio.file.Path;

/**
 * Represents the result of a merge operation.
 *
 * @author FX
 */
public class MergeResult {
    private final Status status;
    private final String message;
    private final Path outputPath;
    private final Exception error;
    
    /**
     * Creates a new merge result.
     * 
     * @param status The status of the merge
     * @param message A message describing the result
     * @param outputPath The path to the output file, if any
     * @param error The error that occurred, if any
     */
    public MergeResult(Status status, String message, Path outputPath, Exception error) {
        this.status = status;
        this.message = message;
        this.outputPath = outputPath;
        this.error = error;
    }
    
    /**
     * Creates a successful merge result.
     * 
     * @param message A message describing the result
     * @param outputPath The path to the output file
     * @return A new merge result
     */
    public static MergeResult success(String message, Path outputPath) {
        return new MergeResult(Status.SUCCESS, message, outputPath, null);
    }
    
    /**
     * Creates a conflict merge result.
     * 
     * @param message A message describing the conflict
     * @return A new merge result
     */
    public static MergeResult conflict(String message) {
        return new MergeResult(Status.CONFLICT, message, null, null);
    }
    
    /**
     * Creates an error merge result.
     * 
     * @param message A message describing the error
     * @param error The error that occurred
     * @return A new merge result
     */
    public static MergeResult error(String message, Exception error) {
        return new MergeResult(Status.ERROR, message, null, error);
    }
    
    /**
     * Gets the status of the merge.
     * 
     * @return The status
     */
    public Status getStatus() {
        return status;
    }
    
    /**
     * Gets the message describing the result.
     * 
     * @return The message
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * Gets the path to the output file, if any.
     * 
     * @return The output path, or null if there is no output file
     */
    public Path getOutputPath() {
        return outputPath;
    }
    
    /**
     * Gets the error that occurred, if any.
     * 
     * @return The error, or null if no error occurred
     */
    public Exception getError() {
        return error;
    }
    
    /**
     * Checks whether the merge was successful.
     * 
     * @return true if the merge was successful, false otherwise
     */
    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }
    
    /**
     * Checks whether the merge resulted in a conflict.
     * 
     * @return true if the merge resulted in a conflict, false otherwise
     */
    public boolean isConflict() {
        return status == Status.CONFLICT;
    }
    
    /**
     * Checks whether the merge resulted in an error.
     * 
     * @return true if the merge resulted in an error, false otherwise
     */
    public boolean isError() {
        return status == Status.ERROR;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("MergeResult{status=").append(status);
        
        if (message != null) {
            sb.append(", message='").append(message).append('\'');
        }
        
        if (outputPath != null) {
            sb.append(", outputPath=").append(outputPath);
        }
        
        if (error != null) {
            sb.append(", error=").append(error.getMessage());
        }
        
        sb.append('}');
        return sb.toString();
    }

    /// The status of the merge operation.
    public enum Status {
        /// The merge was successful
        SUCCESS,

        /// The merge failed due to conflicts
        CONFLICT,

        /// The merge failed due to an error
        ERROR
    }
}