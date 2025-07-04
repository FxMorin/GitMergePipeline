package ca.fxco.gitmergepipeline.merge.operations;

import ca.fxco.gitmergepipeline.merge.MergeContext;
import ca.fxco.gitmergepipeline.merge.MergeOperation;
import ca.fxco.gitmergepipeline.merge.MergeResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * A merge operation that simply uses the other version of the file.
 * This operation ignores the current version and just takes the other version.
 *
 * @author FX
 */
public class TakeOtherOperation implements MergeOperation {
    private static final Logger logger = LoggerFactory.getLogger(TakeOtherOperation.class);
    
    @Override
    public String getName() {
        return "take-other";
    }
    
    @Override
    public String getDescription() {
        return "Uses the other version of the file, ignoring the current version";
    }
    
    @Override
    public MergeResult execute(MergeContext context, List<String> parameters) throws IOException {
        logger.debug("Executing take-other operation");
        
        Path otherPath = context.getOtherPath();
        if (otherPath == null) {
            logger.error("Other path is null");
            return MergeResult.error("Other path is null", null);
        }
        
        Path outputPath = context.getCurrentPath();
        
        // If this is a merge tool operation, use the merged path
        Object mergedPathObj = context.getAttribute("mergedPath");
        if (mergedPathObj instanceof Path) {
            outputPath = (Path) mergedPathObj;
        }
        
        // Copy the other file to the output path
        Files.copy(otherPath, outputPath, StandardCopyOption.REPLACE_EXISTING);
        
        logger.debug("Take-other operation successful");
        return MergeResult.success("Take-other operation successful", outputPath);
    }
}