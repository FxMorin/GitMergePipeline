package ca.fxco.gitmergepipeline.pipeline;

import ca.fxco.gitmergepipeline.merge.MergeContext;
import ca.fxco.gitmergepipeline.merge.MergeResult;
import ca.fxco.gitmergepipeline.rule.Rule;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Interface for pipelines that define how files should be merged.
 * A pipeline consists of a sequence of steps, each with a rule and a merge operation.
 *
 * @author FX
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
public interface Pipeline {

    /**
     * Executes the pipeline on the given merge context.
     * 
     * @param context The merge context to execute the pipeline on
     * @return The result of the merge operation
     * @throws IOException If there's an error during the merge
     */
    MergeResult execute(MergeContext context) throws IOException;

    /**
     * Gets a description of this pipeline.
     * 
     * @return A human-readable description of the pipeline
     */
    String getDescription();

    /**
     * A step in a pipeline, consisting of a rule and a merge operation.
     */
    class Step {
        private final Rule rule;
        private final String operation;
        private final List<String> parameters;

        /**
         * Creates a new pipeline step.
         * 
         * @param rule The rule that determines whether this step should be executed
         * @param operation The merge operation to perform
         * @param parameters Parameters for the merge operation
         */
        @JsonCreator
        public Step(
                @JsonProperty("rule") Rule rule,
                @JsonProperty("operation") String operation,
                @JsonProperty("parameters") List<String> parameters
        ) {
            this.rule = rule;
            this.operation = operation;
            this.parameters = parameters != null ? parameters : new ArrayList<>();
        }

        /**
         * Gets the rule for this step.
         * 
         * @return The rule
         */
        public Rule getRule() {
            return rule;
        }

        /**
         * Gets the merge operation for this step.
         * 
         * @return The merge operation
         */
        public String getOperation() {
            return operation;
        }

        /**
         * Gets the parameters for the merge operation.
         * 
         * @return The parameters
         */
        public List<String> getParameters() {
            return parameters;
        }

        /**
         * Checks whether this step applies to the given merge context.
         * 
         * @param context The merge context to check
         * @return true if the step applies, false otherwise
         */
        public boolean applies(MergeContext context) {
            return rule == null || rule.applies(context);
        }
    }
}
