package ca.fxco.gitmergepipeline.merge;

import org.eclipse.jgit.api.Git;

import java.io.IOException;
import java.util.List;

/**
 * Interface for merge operations that can be executed in a pipeline.
 * A merge operation performs a specific merge-related task on files.
 *
 * @author FX
 */
public interface MergeOperation {
    
    /**
     * Gets the name of this operation.
     * 
     * @return The operation name
     */
    String getName();
    
    /**
     * Gets a description of this operation.
     * 
     * @return A human-readable description of the operation
     */
    String getDescription();
    
    /**
     * Executes the operation on the given merge context.
     * 
     * @param context    The merge context to execute the operation on
     * @param parameters Parameters for the operation
     * @return The result of the operation
     * @throws IOException If there's an error during the operation
     */
    MergeResult execute(MergeContext context, List<String> parameters) throws IOException;

    /**
     * Executes the operation on the given git merge context.
     *
     * @param git        The git instance to execute the operation on
     * @param context    The git merge context to execute the operation on
     * @param parameters Parameters for the operation
     * @return The result of the operation
     * @throws IOException If there's an error during the operation
     */
    MergeResult executeBatched(Git git, GitMergeContext context, List<String> parameters) throws IOException;
}