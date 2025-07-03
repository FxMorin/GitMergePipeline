package ca.fxco.gitmergepipeline.rule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Registry for rules.
 * This class manages the available rules and provides methods to retrieve them.
 *
 * @author FX
 */
public class RuleRegistry {
    private static final Logger logger = LoggerFactory.getLogger(RuleRegistry.class);

    private final Map<String, Class<? extends Rule>> rules;

    /**
     * Creates a new rule registry.
     */
    public RuleRegistry() {
        this.rules = new HashMap<>();
        registerBuiltInRules();
        loadRulesFromServiceLoader();
    }

    /**
     * Registers a rule class in this registry.
     *
     * @param ruleId    The id associated with the rule class
     * @param ruleClass The rule class to register
     */
    public void registerRule(String ruleId, Class<? extends Rule> ruleClass) {
        rules.put(ruleId, ruleClass);
        logger.debug("Registered rule: {}", ruleId);
    }

    /**
     * Gets a rule class by id.
     *
     * @param ruleId The id of the rule class to get
     * @return The rule class, or null if no rule with the given id exists
     */
    public Class<? extends Rule> getRule(String ruleId) {
        return rules.get(ruleId);
    }

    /**
     * Gets all registered rule classes.
     *
     * @return A map of rule ids to rule classes
     */
    public Map<String, Class<? extends Rule>> getRules() {
        return new HashMap<>(rules);
    }

    /**
     * Registers all built-in rules.
     */
    private void registerBuiltInRules() {
        // Register built-in rules
        logger.info("Registering built-in rules");
        registerRule("filePattern", FilePatternRule.class);
        registerRule("contentPattern", ContentPatternRule.class);
        registerRule("composite", CompositeRule.class);
    }

    /**
     * Loads rules from the ServiceLoader.
     * This allows third-party rules to be registered.
     */
    private void loadRulesFromServiceLoader() {
        ServiceLoader<RuleClassSupplier> ruleClassSuppliers = ServiceLoader.load(RuleClassSupplier.class);
        for (RuleClassSupplier supplier : ruleClassSuppliers) {
            for (Map.Entry<String, Class<? extends Rule>> entry : supplier.getRuleClasses().entrySet()) {
                registerRule(entry.getKey(), entry.getValue());
            }
        }
    }
}