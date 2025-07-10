package ca.fxco.gitmergepipeline.filter;

import java.util.Map;

/**
 * Provides a collection of filter classes.
 * This interface is passed through a service loader to allow for custom filter implementations.
 *
 * @author FX
 */
public interface FilterClassSupplier {

    /**
     * Gets the collection of filter classes.
     *
     * @return The filter classes
     */
    Map<String, Class<? extends Filter>> getFilterClasses();
}
