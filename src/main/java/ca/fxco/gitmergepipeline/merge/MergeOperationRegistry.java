package ca.fxco.gitmergepipeline.merge;

import ca.fxco.gitmergepipeline.merge.operations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Registry for merge operations that can be executed in a pipeline.
 * This class manages the available merge operations and provides methods to retrieve them.
 *
 * @author FX
 */
public class MergeOperationRegistry {
    private static final Logger logger = LoggerFactory.getLogger(MergeOperationRegistry.class);

    private static MergeOperationRegistry defaultInstance;

    private final Map<String, MergeOperation> operations;

    /**
     * Creates a new merge operation registry.
     */
    public MergeOperationRegistry() {
        this.operations = new HashMap<>();
    }

    /**
     * Gets the default merge operation registry.
     * The default registry includes all built-in merge operations.
     * 
     * @return The default merge operation registry
     */
    public static synchronized MergeOperationRegistry getDefault() {
        if (defaultInstance == null) {
            defaultInstance = new MergeOperationRegistry();
            defaultInstance.registerBuiltInOperations();
            defaultInstance.loadOperationsFromServiceLoader();
        }
        return defaultInstance;
    }

    /**
     * Registers a merge operation in this registry.
     * 
     * @param operation The merge operation to register
     */
    public void registerOperation(MergeOperation operation) {
        operations.put(operation.getName(), operation);
        logger.debug("Registered merge operation: {}", operation.getName());
    }

    /**
     * Gets a merge operation by name.
     * 
     * @param name The name of the operation to get
     * @return The merge operation, or null if no operation with the given name exists
     */
    public MergeOperation getOperation(String name) {
        return operations.get(name);
    }

    /**
     * Gets all registered merge operations.
     * 
     * @return A map of operation names to operations
     */
    public Map<String, MergeOperation> getOperations() {
        return new HashMap<>(operations);
    }

    /**
     * Registers all built-in merge operations.
     */
    private void registerBuiltInOperations() {
        // Register built-in operations
        logger.info("Registering built-in operations");
        registerOperation(new GitMergeOperation());
        registerOperation(new CommandLineMergeOperation());
    }

    /**
     * Loads merge operations from the ServiceLoader.
     * This allows third-party operations to be registered.
     */
    private void loadOperationsFromServiceLoader() {
        ServiceLoader<MergeOperation> loader = ServiceLoader.load(MergeOperation.class);
        for (MergeOperation operation : loader) {
            registerOperation(operation);
        }
    }
}
