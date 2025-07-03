package ca.fxco.gitmergepipeline.pipeline;

import java.util.Map;

/**
 * Provides a collection of pipeline classes.
 * This interface is passed through a service loader to allow for custom pipeline implementations.
 *
 * @author FX
 */
public interface PipelineClassSupplier {

    /**
     * Gets the collection of pipeline classes.
     *
     * @return The pipeline classes
     */
    Map<String, Class<? extends Pipeline>> getPipelineClasses();
}
