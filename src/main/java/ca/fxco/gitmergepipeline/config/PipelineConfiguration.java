package ca.fxco.gitmergepipeline.config;

import ca.fxco.gitmergepipeline.pipeline.Pipeline;
import ca.fxco.gitmergepipeline.rule.Rule;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration for the GitMergePipeline system.
 * Contains definitions for rules and pipelines.
 *
 * @author FX
 */
public class PipelineConfiguration {
    private final Map<String, Rule> rules;
    private final List<Pipeline> pipelines;
    
    /**
     * Creates a new configuration with the specified rules and pipelines.
     * 
     * @param rules Map of rule names to rule definitions
     * @param pipelines List of pipeline definitions
     */
    @JsonCreator
    public PipelineConfiguration(
            @JsonProperty("rules") Map<String, Rule> rules,
            @JsonProperty("pipelines") List<Pipeline> pipelines
    ) {
        this.rules = rules != null ? rules : new HashMap<>();
        this.pipelines = pipelines != null ? pipelines : new ArrayList<>();
    }
    
    /**
     * Creates a new empty configuration.
     */
    public PipelineConfiguration() {
        this(new HashMap<>(), new ArrayList<>());
    }
    
    /**
     * Gets the map of rule names to rule definitions.
     * 
     * @return Map of rule names to rule definitions
     */
    public Map<String, Rule> getRules() {
        return rules;
    }
    
    /**
     * Gets the list of pipeline definitions.
     * 
     * @return List of pipeline definitions
     */
    public List<Pipeline> getPipelines() {
        return pipelines;
    }
    
    /**
     * Gets a rule by name.
     * 
     * @param name Name of the rule to get
     * @return The rule with the specified name, or null if no such rule exists
     */
    public Rule getRule(String name) {
        return rules.get(name);
    }
    
    /**
     * Adds a rule to the configuration.
     * 
     * @param name Name of the rule
     * @param rule Rule definition
     */
    public void addRule(String name, Rule rule) {
        rules.put(name, rule);
    }
    
    /**
     * Adds a pipeline to the configuration.
     * 
     * @param pipeline Pipeline definition
     */
    public void addPipeline(Pipeline pipeline) {
        pipelines.add(pipeline);
    }
}