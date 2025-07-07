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
 * A merge operation that simply uses the base version of the file.
 * This operation ignores the other & current version and just keeps the base version.
 *
 * @author FX
 */
public class KeepBaseOperation implements MergeOperation {
    private static final Logger logger = LoggerFactory.getLogger(KeepBaseOperation.class);
    
    @Override
    public String getName() {
        return "keep-base";
    }
    
    @Override
    public String getDescription() {
        return "Uses the base version of the file, ignoring the other & current version";
    }
    
    @Override
    public MergeResult execute(MergeContext context, List<String> parameters) throws IOException {
        logger.debug("Executing keep-base operation");
        
        Path basePath = context.getBasePath();
        if (basePath == null) {
            // If this is a merge tool operation, use the merged path
            Object mergedPathObj = context.getAttribute("mergedPath");
            if (mergedPathObj instanceof Path) {
                Files.deleteIfExists((Path) mergedPathObj); // Base file doesn't exist, so it should be removed
                logger.debug("Keep-base operation successful");
                return MergeResult.success("Keep-base operation successful", null);
            } else { // Still a success since the file should be missing, if the base is missing
                logger.debug("Base path is null");
                return MergeResult.success("Base path is null", null);
            }
        }
        
        Path outputPath = basePath;
        
        // If this is a merge tool operation, use the merged path
        Object mergedPathObj = context.getAttribute("mergedPath");
        if (mergedPathObj instanceof Path) {
            outputPath = (Path) mergedPathObj;
            
            // Copy the current file to the output path
            Files.copy(basePath, outputPath, StandardCopyOption.REPLACE_EXISTING);
        }
        
        logger.debug("Keep-base operation successful");
        return MergeResult.success("Keep-base operation successful", outputPath);
    }
}