package ca.fxco.gitmergepipeline.merge;

import ca.fxco.gitmergepipeline.config.PipelineConfiguration;

/**
 * Abstract base class for merge tools.
 *
 * @author FX
 */
public abstract class Merger {

    private final PipelineConfiguration configuration;

    protected Merger(PipelineConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Gets the current pipeline configuration.
     *
     * @return The pipeline configuration
     */
    public PipelineConfiguration getConfiguration() {
        return this.configuration;
    }
}
