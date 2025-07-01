package ca.fxco.gitmergepipeline.merge;

import ca.fxco.gitmergepipeline.config.PipelineConfiguration;
import ca.fxco.gitmergepipeline.pipeline.Pipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Implementation of a Git merge driver using the GitMergePipeline system.
 * This class is used when the system is used as a Git merge driver.
 *
 * @author FX
 */
public class MergeDriver {
    private static final Logger logger = LoggerFactory.getLogger(MergeDriver.class);
    
    private final PipelineConfiguration configuration;
    
    /**
     * Creates a new merge driver with the specified configuration.
     * 
     * @param configuration The pipeline configuration to use
     */
    public MergeDriver(PipelineConfiguration configuration) {
        this.configuration = configuration;
    }
    
    /**
     * Merges the specified files.
     * 
     * @param basePath Path to the base version of the file
     * @param currentPath Path to the current version of the file
     * @param otherPath Path to the other version of the file
     * @param filePath Path to the file relative to the working directory
     * @return true if the merge was successful, false otherwise
     * @throws IOException If there's an error during the merge
     */
    public boolean merge(Path basePath, Path currentPath, Path otherPath, String filePath) throws IOException {
        logger.info("Merging file: {}", filePath);
        
        // Create the merge context
        MergeContext context = new MergeContext(basePath, currentPath, otherPath, filePath);
        
        // Find a pipeline that applies to this file
        Pipeline pipeline = MergeUtil.findPipeline(configuration, context);
        if (pipeline == null) {
            logger.error("No pipeline found for file: {}", filePath);
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