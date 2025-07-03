package ca.fxco.gitmergepipeline.rule;

import java.util.Map;

/**
 * Provides a collection of rule classes.
 * This interface is passed through a service loader to allow for custom rule implementations.
 *
 * @author FX
 */
public interface RuleClassSupplier {

    /**
     * Gets the collection of rule classes.
     *
     * @return The rule classes
     */
    Map<String, Class<? extends Rule>> getRuleClasses();
}
