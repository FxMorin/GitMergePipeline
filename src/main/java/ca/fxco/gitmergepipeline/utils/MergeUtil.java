package ca.fxco.gitmergepipeline.utils;

import ca.fxco.gitmergepipeline.config.PipelineConfiguration;
import ca.fxco.gitmergepipeline.merge.MergeContext;
import ca.fxco.gitmergepipeline.pipeline.Pipeline;
import ca.fxco.gitmergepipeline.pipeline.StandardPipeline;
import ca.fxco.gitmergepipeline.rule.FilePatternRule;
import ca.fxco.gitmergepipeline.rule.Rule;

import java.util.List;
import java.util.Map;

/**
 * Utility class for managing merge operations and finding applicable pipelines
 * within a given merge context.
 *
 * @author FX
 */
public class MergeUtil {

    /**
     * Finds a pipeline that applies to the given merge context.
     *
     * @param context The merge context to find a pipeline for
     * @return The pipeline to use, or null if no pipeline applies
     */
    public static Pipeline findPipeline(PipelineConfiguration configuration, MergeContext context) {
        List<Pipeline> pipelines = configuration.getPipelines();

        // Create a new context with just the filename for matching file pattern rules
        String filePath = context.getFilePath();
        String fileName = filePath.contains("/") || filePath.contains("\\") 
            ? filePath.substring(Math.max(filePath.lastIndexOf('/'), filePath.lastIndexOf('\\')) + 1)
            : filePath;
        MergeContext fileNameContext = new MergeContext(
            context.getBasePath(), 
            context.getCurrentPath(), 
            context.getOtherPath(), 
            fileName
        );
        // Copy all attributes from the original context
        for (Map.Entry<String, Object> entry : context.getAttributes().entrySet()) {
            fileNameContext.setAttribute(entry.getKey(), entry.getValue());
        }

        // Try to find a pipeline with a file pattern rule that matches the file path
        // TODO: Abstract pipelines and rules so that any pipeline and rule can do this
        for (Pipeline pipeline : pipelines) {
            if (pipeline instanceof StandardPipeline standardPipeline) {

                // Check if the pipeline has a file pattern rule that matches the file path
                for (Pipeline.Step step : standardPipeline.getSteps()) {
                    Rule rule = step.getRule();
                    if (rule instanceof FilePatternRule && rule.applies(fileNameContext)) {
                        return pipeline;
                    }
                }
            }
        }

        // If no pipeline with a matching file pattern rule is found, use the first pipeline
        if (!pipelines.isEmpty()) {
            return pipelines.getFirst();
        }

        return null;
    }
}
