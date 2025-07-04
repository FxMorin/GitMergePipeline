package ca.fxco.gitmergepipeline.pipeline;

import ca.fxco.gitmergepipeline.merge.MergeContext;
import ca.fxco.gitmergepipeline.merge.MergeResult;
import ca.fxco.gitmergepipeline.rule.Rule;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A conditional pipeline that executes different pipelines based on rules.
 * Each branch in the pipeline has a rule and a pipeline to execute if the rule applies.
 *
 * @author FX
 */
public class ConditionalPipeline implements Pipeline {
    private static final Logger logger = LoggerFactory.getLogger(ConditionalPipeline.class);
    
    private final String name;
    private final List<Branch> branches;
    private final Pipeline defaultPipeline;
    
    /**
     * Creates a new conditional pipeline.
     * 
     * @param name The name of the pipeline
     * @param branches The branches in the pipeline
     * @param defaultPipeline The default pipeline to execute if no branch applies
     */
    @JsonCreator
    public ConditionalPipeline(
            @JsonProperty("name") String name,
            @JsonProperty("branches") List<Branch> branches,
            @JsonProperty("defaultPipeline") Pipeline defaultPipeline
    ) {
        this.name = name;
        this.branches = branches != null ? branches : new ArrayList<>();
        this.defaultPipeline = defaultPipeline;
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
     * Gets the branches in the pipeline.
     * 
     * @return The branches
     */
    public List<Branch> getBranches() {
        return branches;
    }
    
    /**
     * Gets the default pipeline.
     * 
     * @return The default pipeline
     */
    public Pipeline getDefaultPipeline() {
        return defaultPipeline;
    }
    
    @Override
    public MergeResult execute(MergeContext context) throws IOException {
        logger.debug("Executing conditional pipeline: {}", name);

        for (Branch branch : branches) {
            if (branch.getRule().applies(context)) {
                logger.debug("Branch rule applies, executing pipeline: {}", branch.getPipeline().getDescription());
                return branch.getPipeline().execute(context);
            }
        }
        
        if (defaultPipeline != null) {
            logger.debug("No branch rule applies, executing default pipeline: {}", defaultPipeline.getDescription());
            return defaultPipeline.execute(context);
        }
        
        logger.debug("No branch rule applies and no default pipeline, returning success");
        return MergeResult.success("No branch rule applies and no default pipeline", null);
    }
    
    @Override
    public String getDescription() {
        return "Conditional pipeline: " + name;
    }
    
    /**
     * A branch in a conditional pipeline, consisting of a rule and a pipeline.
     */
    public static class Branch {
        private final Rule rule;
        private final Pipeline pipeline;
        
        /**
         * Creates a new branch.
         * 
         * @param rule The rule that determines whether this branch should be taken
         * @param pipeline The pipeline to execute if the rule applies
         */
        @JsonCreator
        public Branch(
                @JsonProperty("rule") Rule rule,
                @JsonProperty("pipeline") Pipeline pipeline
        ) {
            this.rule = rule;
            this.pipeline = pipeline;
        }
        
        /**
         * Gets the rule for this branch.
         * 
         * @return The rule
         */
        public Rule getRule() {
            return rule;
        }
        
        /**
         * Gets the pipeline for this branch.
         * 
         * @return The pipeline
         */
        public Pipeline getPipeline() {
            return pipeline;
        }
    }
}