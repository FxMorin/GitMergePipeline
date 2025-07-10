package ca.fxco.gitmergepipeline.pipeline;

import ca.fxco.gitmergepipeline.merge.MergeContext;
import ca.fxco.gitmergepipeline.merge.MergeOperation;
import ca.fxco.gitmergepipeline.merge.MergeOperationRegistry;
import ca.fxco.gitmergepipeline.merge.MergeResult;
import ca.fxco.gitmergepipeline.rule.FilePatternRule;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A standard pipeline that executes a sequence of merge operations based on rules.
 * Each step in the pipeline is executed if its rule applies to the merge context.
 *
 * @author FX
 */
public class StandardPipeline implements Pipeline {
    private static final Logger logger = LoggerFactory.getLogger(StandardPipeline.class);
    
    private final String name;
    private final List<Step> steps;
    private final MergeOperationRegistry operationRegistry;
    
    /**
     * Creates a new standard pipeline.
     * 
     * @param name The name of the pipeline
     * @param steps The steps in the pipeline
     * @param operationRegistry The registry of merge operations
     */
    @JsonCreator
    public StandardPipeline(
            @JsonProperty("name") String name,
            @JsonProperty("steps") List<Step> steps,
            @JsonProperty("operationRegistry") MergeOperationRegistry operationRegistry
    ) {
        this.name = name;
        this.steps = steps != null ? steps : new ArrayList<>();
        this.operationRegistry = operationRegistry != null ? operationRegistry : MergeOperationRegistry.getDefault();
    }
    
    /**
     * Gets the name of the pipeline.
     * 
     * @return The name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the steps in the pipeline.
     * 
     * @return The steps
     */
    public List<Step> getSteps() {
        return steps;
    }

    @Override
    public boolean matchesFileRule(MergeContext fileNameContext) {
        // Check if the pipeline has a file pattern rule that matches the file path
        for (Pipeline.Step step : steps) {
            if (step.getRule() instanceof FilePatternRule filePatternRule &&
                    filePatternRule.applies(fileNameContext)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public MergeResult execute(MergeContext context) throws IOException {
        logger.debug("Executing pipeline: {}", name);
        
        for (Step step : steps) {
            if (step.applies(context)) {
                logger.debug("Executing step with operation: {}", step.getOperation());
                
                MergeOperation operation = operationRegistry.getOperation(step.getOperation());
                if (operation == null) {
                    logger.error("Unknown operation: {}", step.getOperation());
                    return MergeResult.error("Unknown operation: " + step.getOperation(), null);
                }
                
                try {
                    MergeResult result = operation.execute(context, step.getParameters());
                    
                    if (!result.isSuccess()) {
                        logger.debug("Pipeline step failed: {}", result.getMessage());
                        return result;
                    }
                } catch (Exception e) {
                    logger.error("Error executing operation: {}", step.getOperation(), e);
                    return MergeResult.error("Error executing operation: " + step.getOperation(), e);
                }
            } else {
                logger.debug("Skipping step with operation: {} (rule does not apply)", step.getOperation());
            }
        }
        
        logger.debug("Pipeline executed successfully");
        return MergeResult.success("Pipeline executed successfully", null);
    }
    
    @Override
    public String getDescription() {
        return "Standard pipeline: " + name;
    }
}