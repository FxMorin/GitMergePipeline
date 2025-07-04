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
 * A merge operation that simply uses the current version of the file.
 * This operation ignores the other version and just keeps the current version.
 *
 * @author FX
 */
public class TakeCurrentOperation implements MergeOperation {
    private static final Logger logger = LoggerFactory.getLogger(TakeCurrentOperation.class);
    
    @Override
    public String getName() {
        return "take-current";
    }
    
    @Override
    public String getDescription() {
        return "Uses the current version of the file, ignoring the other version";
    }
    
    @Override
    public MergeResult execute(MergeContext context, List<String> parameters) throws IOException {
        logger.debug("Executing take-current operation");
        
        Path currentPath = context.getCurrentPath();
        if (currentPath == null) {
            logger.error("Current path is null");
            return MergeResult.error("Current path is null", null);
        }
        
        Path outputPath = currentPath;
        
        // If this is a merge tool operation, use the merged path
        Object mergedPathObj = context.getAttribute("mergedPath");
        if (mergedPathObj instanceof Path) {
            outputPath = (Path) mergedPathObj;
            
            // Copy the current file to the output path
            Files.copy(currentPath, outputPath, StandardCopyOption.REPLACE_EXISTING);
        }
        
        logger.debug("Take-current operation successful");
        return MergeResult.success("Take-current operation successful", outputPath);
    }
}