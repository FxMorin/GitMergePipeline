package ca.fxco.gitmergepipeline.config;

import ca.fxco.gitmergepipeline.filter.Filter;
import ca.fxco.gitmergepipeline.merge.MergeContext;
import ca.fxco.gitmergepipeline.pipeline.Pipeline;
import ca.fxco.gitmergepipeline.rule.Rule;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.jgit.annotations.Nullable;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;

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
    private final boolean detectRenames;
    private final int binaryFileThreshold;
    private final List<Filter> filters;
    private final Map<String, Rule> rules;
    private final List<Pipeline> pipelines;

    private TreeFilter combinedFilter;
    
    /**
     * Creates a new configuration with the specified rules and pipelines.
     *
     * @param filters   Map of filter names to filter definitions
     * @param rules     Map of rule names to rule definitions
     * @param pipelines List of pipeline definitions
     */
    @JsonCreator
    public PipelineConfiguration(
            @JsonProperty("detectRenames") boolean detectRenames,
            @JsonProperty("binaryFileThreshold") int BinaryFileThreshold,
            @JsonProperty("filters") List<Filter> filters,
            @JsonProperty("rules") Map<String, Rule> rules,
            @JsonProperty("pipelines") List<Pipeline> pipelines
    ) {
        this.detectRenames = detectRenames;
        this.binaryFileThreshold = BinaryFileThreshold;
        this.filters = filters != null ? filters : new ArrayList<>();
        this.rules = rules != null ? rules : new HashMap<>();
        this.pipelines = pipelines != null ? pipelines : new ArrayList<>();
    }

    public PipelineConfiguration(List<Filter> filters, Map<String, Rule> rules, List<Pipeline> pipelines) {
        this.detectRenames = true;
        this.binaryFileThreshold = 200000;
        this.filters = filters != null ? filters : new ArrayList<>();
        this.rules = rules != null ? rules : new HashMap<>();
        this.pipelines = pipelines != null ? pipelines : new ArrayList<>();
    }
    
    /**
     * Creates a new empty configuration.
     */
    public PipelineConfiguration() {
        this(new ArrayList<>(), new HashMap<>(), new ArrayList<>());
    }

    /**
     * If true, GitMergePipeline will attempt to detect renames when using JGit.
     *
     * @return {@code true} if renames should be detected, otherwise {@code false}
     */
    public boolean detectRenames() {
        return detectRenames;
    }

    /**
     * Gets the threshold for binary files when using JGit.
     *
     * @return The threshold
     */
    public int binaryFileThreshold() {
        return binaryFileThreshold;
    }

    /**
     * Gets the combined tree filter for all filters in the configuration.
     *
     * @return The combined tree filter
     */
    public TreeFilter getCombinedFilter() {
        if (combinedFilter == null) {
            combinedFilter = combineFilters();
        }

        return combinedFilter;
    }

    /**
     * Gets the list of filters.
     *
     * @return List of filters
     */
    public List<Filter> getFilters() {
        return filters;
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
     * Adds a filter to the configuration.
     *
     * @param filter Filter definition
     */
    public void addFilter(Filter filter) {
        filters.add(filter);
        combinedFilter = null;
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

    private TreeFilter combineFilters() {
        if (filters.isEmpty()) {
            return TreeFilter.ALL;
        }
        if (filters.size() == 1) {
            return filters.getFirst().getTreeFilter();
        }
        return AndTreeFilter.create(filters
                .stream()
                .map(Filter::getTreeFilter)
                .toArray(TreeFilter[]::new));
    }

    /**
     * Finds a pipeline that applies to the given merge context.
     *
     * @param context The merge context to find a pipeline for
     * @return The pipeline to use, or null if no pipeline applies
     */
    public @Nullable Pipeline findPipeline(MergeContext context) {
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
        for (Pipeline pipeline : pipelines) {
            if (pipeline.matchesFileRule(fileNameContext)) {
                return pipeline;
            }
        }

        // If no pipeline with a matching file pattern rule is found, use the first pipeline
        if (!pipelines.isEmpty()) {
            return pipelines.getFirst();
        }

        return null;
    }


    public static PipelineConfiguration onlyFilters(Filter... filters) {
        return new PipelineConfiguration(List.of(filters), null, null);
    }

    public static PipelineConfiguration onlyRules(Map<String, Rule> rules) {
        return new PipelineConfiguration(null, rules, null);
    }

    public static PipelineConfiguration onlyPipelines(Pipeline... pipelines) {
        return new PipelineConfiguration(null, null, List.of(pipelines));
    }
}