package ca.fxco.gitmergepipeline.filter;

import ca.fxco.gitmergepipeline.filter.filters.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Registry for filters.
 * This class manages the available filters and provides methods to retrieve them.
 *
 * @author FX
 */
public class FilterRegistry {
    private static final Logger logger = LoggerFactory.getLogger(FilterRegistry.class);

    private final Map<String, Class<? extends Filter>> filters;

    /**
     * Creates a new filter registry.
     */
    public FilterRegistry() {
        this.filters = new HashMap<>();
        registerBuiltInFilters();
        loadFiltersFromServiceLoader();
    }

    /**
     * Registers a filter class in this registry.
     *
     * @param filterId    The id associated with the filter class
     * @param filterClass The filter class to register
     */
    public void registerFilter(String filterId, Class<? extends Filter> filterClass) {
        filters.put(filterId, filterClass);
        logger.debug("Registered filter: {}", filterId);
    }

    /**
     * Gets a filter class by id.
     *
     * @param filterId The id of the filter class to get
     * @return The filter class, or null if no filter with the given id exists
     */
    public Class<? extends Filter> getFilter(String filterId) {
        return filters.get(filterId);
    }

    /**
     * Gets all registered filter classes.
     *
     * @return A map of filter ids to filter classes
     */
    public Map<String, Class<? extends Filter>> getFilters() {
        return new HashMap<>(filters);
    }

    /**
     * Registers all built-in filters.
     */
    private void registerBuiltInFilters() {
        // Register built-in filters
        logger.info("Registering built-in filters");
        registerFilter("not", NotFilter.class);
        registerFilter("or", OrFilter.class);
        registerFilter("mimeType", MimeTypeFilter.class);
        registerFilter("fileMode", FileModeFilter.class);
        registerFilter("filePattern", PathFilter.class);
    }

    /**
     * Loads filters from the ServiceLoader.
     * This allows third-party filters to be registered.
     */
    private void loadFiltersFromServiceLoader() {
        ServiceLoader<FilterClassSupplier> filterClassSuppliers = ServiceLoader.load(FilterClassSupplier.class);
        for (FilterClassSupplier supplier : filterClassSuppliers) {
            for (Map.Entry<String, Class<? extends Filter>> entry : supplier.getFilterClasses().entrySet()) {
                registerFilter(entry.getKey(), entry.getValue());
            }
        }
    }
}
