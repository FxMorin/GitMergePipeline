package ca.fxco.gitmergepipeline.merge;

import ca.fxco.gitmergepipeline.config.PipelineConfiguration;
import ca.fxco.gitmergepipeline.pipeline.Pipeline;
import ca.fxco.gitmergepipeline.utils.MergeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Implementation of a re-merge tool using the GitMergePipeline system.
 * This class is used when the system is used to re-merge files that have already been merged.
 *
 * @author FX
 */
public class ReMergeTool {
    private static final Logger logger = LoggerFactory.getLogger(ReMergeTool.class);
    
    private final PipelineConfiguration configuration;
    
    /**
     * Creates a new re-merge tool with the specified configuration.
     * 
     * @param configuration The pipeline configuration to use
     */
    public ReMergeTool(PipelineConfiguration configuration) {
        this.configuration = configuration;
    }
    
    /**
     * Re-merges the specified files.
     * 
     * @param basePath Path to the base version of the file
     * @param currentPath Path to the current version of the file
     * @param otherPath Path to the other version of the file
     * @return true if the re-merge was successful, false otherwise
     * @throws IOException If there's an error during the re-merge
     */
    public boolean remerge(Path basePath, Path currentPath, Path otherPath) throws IOException {
        logger.info("Re-merging files");
        
        // Create the merge context
        MergeContext context = new MergeContext(basePath, currentPath, otherPath, currentPath.toString());
        
        // Find a pipeline that applies to this file
        Pipeline pipeline = MergeUtil.findPipeline(configuration, context);
        if (pipeline == null) {
            logger.error("No pipeline found for re-merge");
            return false;
        }
        
        // Execute the pipeline
        logger.info("Executing pipeline: {}", pipeline.getDescription());
        MergeResult result = pipeline.execute(context);
        
        if (result.isSuccess()) {
            logger.info("Re-merge successful: {}", result.getMessage());
            return true;
        } else {
            logger.error("Re-merge failed: {}", result.getMessage());
            return false;
        }
    }
}