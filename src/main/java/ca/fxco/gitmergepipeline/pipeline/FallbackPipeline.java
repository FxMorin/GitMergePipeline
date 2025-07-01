package ca.fxco.gitmergepipeline.pipeline;

import ca.fxco.gitmergepipeline.merge.MergeContext;
import ca.fxco.gitmergepipeline.merge.MergeOperation;
import ca.fxco.gitmergepipeline.merge.MergeOperationRegistry;
import ca.fxco.gitmergepipeline.merge.MergeResult;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A fallback pipeline that tries different merge operations until one succeeds.
 * Each step in the pipeline is executed if its rule applies to the merge context.
 * If a step fails, the pipeline continues to the next step instead of stopping.
 *
 * @author FX
 */
public class FallbackPipeline implements Pipeline {
    private static final Logger logger = LoggerFactory.getLogger(FallbackPipeline.class);

    private final String name;
    private final List<Step> steps;
    private final MergeOperationRegistry operationRegistry;

    /**
     * Creates a new fallback pipeline.
     * 
     * @param name The name of the pipeline
     * @param steps The steps in the pipeline
     * @param operationRegistry The registry of merge operations
     */
    @JsonCreator
    public FallbackPipeline(
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
    public MergeResult execute(MergeContext context) throws IOException {
        logger.info("Executing fallback pipeline: {}", name);

        MergeResult lastResult = null;

        for (Step step : steps) {
            if (step.applies(context)) {
                logger.debug("Executing step with operation: {}", step.getOperation());

                MergeOperation operation = operationRegistry.getOperation(step.getOperation());
                if (operation == null) {
                    logger.error("Unknown operation: {}", step.getOperation());
                    lastResult = MergeResult.error("Unknown operation: " + step.getOperation(), null);
                    continue; // Try the next step
                }

                try {
                    MergeResult result = operation.execute(context, step.getParameters());
                    lastResult = result;

                    if (result.isSuccess()) {
                        logger.info("Pipeline step succeeded: {}", result.getMessage());
                        return result; // Return on first success
                    } else {
                        logger.info("Pipeline step failed: {}, trying next step", result.getMessage());
                    }
                } catch (Exception e) {
                    logger.error("Error executing operation: {}", step.getOperation(), e);
                    lastResult = MergeResult.error("Error executing operation: " + step.getOperation(), e);
                    // Continue to the next step
                }
            } else {
                logger.debug("Skipping step with operation: {} (rule does not apply)", step.getOperation());
            }
        }

        if (lastResult != null) {
            logger.info("All pipeline steps failed, returning last result: {}", lastResult.getMessage());
            return lastResult;
        } else {
            // Check if there are no steps or no applicable steps
            if (steps.isEmpty()) {
                logger.info("No steps to execute");
                return MergeResult.success("No steps to execute", null);
            } else {
                logger.info("No applicable steps found");
                return MergeResult.success("No applicable steps found", null);
            }
        }
    }

    @Override
    public String getDescription() {
        return "Fallback pipeline: " + name;
    }
}
