package ca.fxco.gitmergepipeline.pipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Registry for pipelines.
 * This class manages the available pipelines and provides methods to retrieve them.
 *
 * @author FX
 */
public class PipelineRegistry {
    private static final Logger logger = LoggerFactory.getLogger(PipelineRegistry.class);

    private final Map<String, Class<? extends Pipeline>> pipelines;

    /**
     * Creates a new pipeline registry.
     */
    public PipelineRegistry() {
        this.pipelines = new HashMap<>();
        registerBuiltInPipelines();
        loadPipelinesFromServiceLoader();
    }

    /**
     * Registers a pipeline class in this registry.
     *
     * @param pipelineId    The id associated with the pipeline class
     * @param pipelineClass The pipeline class to register
     */
    public void registerPipeline(String pipelineId, Class<? extends Pipeline> pipelineClass) {
        pipelines.put(pipelineId, pipelineClass);
        logger.debug("Registered pipeline: {}", pipelineId);
    }

    /**
     * Gets a pipeline class by id.
     *
     * @param pipelineId The id of the pipeline class to get
     * @return The pipeline class, or null if no pipeline with the given id exists
     */
    public Class<? extends Pipeline> getPipeline(String pipelineId) {
        return pipelines.get(pipelineId);
    }

    /**
     * Gets all registered pipeline classes.
     *
     * @return A map of pipeline ids to pipeline classes
     */
    public Map<String, Class<? extends Pipeline>> getPipelines() {
        return new HashMap<>(pipelines);
    }

    /**
     * Registers all built-in pipelines.
     */
    private void registerBuiltInPipelines() {
        // Register built-in pipelines
        logger.info("Registering built-in pipelines");
        registerPipeline("standard", StandardPipeline.class);
        registerPipeline("conditional", ConditionalPipeline.class);
        registerPipeline("fallback", FallbackPipeline.class);
    }

    /**
     * Loads pipelines from the ServiceLoader.
     * This allows third-party pipelines to be registered.
     */
    private void loadPipelinesFromServiceLoader() {
        ServiceLoader<PipelineClassSupplier> pipelineClassSuppliers = ServiceLoader.load(PipelineClassSupplier.class);
        for (PipelineClassSupplier supplier : pipelineClassSuppliers) {
            for (Map.Entry<String, Class<? extends Pipeline>> entry : supplier.getPipelineClasses().entrySet()) {
                registerPipeline(entry.getKey(), entry.getValue());
            }
        }
    }
}
