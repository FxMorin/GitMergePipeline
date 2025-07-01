package ca.fxco.gitmergepipeline.merge;

import ca.fxco.gitmergepipeline.config.PipelineConfiguration;
import ca.fxco.gitmergepipeline.pipeline.Pipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Implementation of a merge tool using the GitMergePipeline system.
 * This class is used when the system is used as a merge tool.
 *
 * @author FX
 */
public class MergeTool {
    private static final Logger logger = LoggerFactory.getLogger(MergeTool.class);
    
    private final PipelineConfiguration configuration;
    
    /**
     * Creates a new merge tool with the specified configuration.
     * 
     * @param configuration The pipeline configuration to use
     */
    public MergeTool(PipelineConfiguration configuration) {
        this.configuration = configuration;
    }
    
    /**
     * Merges the specified files.
     * 
     * @param localPath Path to the local version of the file
     * @param remotePath Path to the remote version of the file
     * @param mergedPath Path to the merged version of the file
     * @return true if the merge was successful, false otherwise
     * @throws IOException If there's an error during the merge
     */
    public boolean merge(Path localPath, Path remotePath, Path mergedPath) throws IOException {
        logger.info("Merging files as merge tool");
        
        // Create the merge context
        MergeContext context = MergeContext.forMergeTool(localPath, remotePath, mergedPath);
        
        // Find a pipeline that applies to this file
        Pipeline pipeline = MergeUtil.findPipeline(configuration, context);
        if (pipeline == null) {
            logger.error("No pipeline found for merge tool");
            return false;
        }
        
        // Execute the pipeline
        logger.info("Executing pipeline: {}", pipeline.getDescription());
        MergeResult result = pipeline.execute(context);
        
        if (result.isSuccess()) {
            logger.info("Merge successful: {}", result.getMessage());
            return true;
        } else {
            logger.error("Merge failed: {}", result.getMessage());
            return false;
        }
    }
}