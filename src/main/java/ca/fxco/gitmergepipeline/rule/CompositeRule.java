package ca.fxco.gitmergepipeline.rule;

import ca.fxco.gitmergepipeline.merge.MergeContext;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * A rule that combines multiple rules with a logical operation.
 * Supported operations are AND, OR, and NOT.
 *
 * @author FX
 */
public class CompositeRule implements Rule {
    private final Operation operation;
    private final List<Rule> rules;
    
    /**
     * Creates a new composite rule with the specified operation and rules.
     * 
     * @param operation The logical operation to apply
     * @param rules The rules to combine
     */
    @JsonCreator
    public CompositeRule(
            @JsonProperty("operation") Operation operation,
            @JsonProperty("rules") List<Rule> rules
    ) {
        this.operation = operation;
        this.rules = rules != null ? rules : new ArrayList<>();
        
        if (operation == Operation.NOT && this.rules.size() != 1) {
            throw new IllegalArgumentException("NOT operation requires exactly one rule");
        }
    }
    
    /**
     * Creates a new AND composite rule with the specified rules.
     * 
     * @param rules The rules to combine with AND
     * @return A new composite rule
     */
    public static CompositeRule and(List<Rule> rules) {
        return new CompositeRule(Operation.AND, rules);
    }
    
    /**
     * Creates a new OR composite rule with the specified rules.
     * 
     * @param rules The rules to combine with OR
     * @return A new composite rule
     */
    public static CompositeRule or(List<Rule> rules) {
        return new CompositeRule(Operation.OR, rules);
    }
    
    /**
     * Creates a new NOT composite rule with the specified rule.
     * 
     * @param rule The rule to negate
     * @return A new composite rule
     */
    public static CompositeRule not(Rule rule) {
        List<Rule> rules = new ArrayList<>();
        rules.add(rule);
        return new CompositeRule(Operation.NOT, rules);
    }
    
    /**
     * Gets the logical operation applied by this rule.
     * 
     * @return The logical operation
     */
    public Operation getOperation() {
        return operation;
    }
    
    /**
     * Gets the rules combined by this composite rule.
     * 
     * @return The list of rules
     */
    public List<Rule> getRules() {
        return rules;
    }
    
    @Override
    public boolean applies(MergeContext context) {
        return switch (operation) {
            case AND -> {
                for (Rule rule : rules) {
                    if (!rule.applies(context)) {
                        yield false;
                    }
                }
                yield !rules.isEmpty();
            }
            case OR -> {
                for (Rule rule : rules) {
                    if (rule.applies(context)) {
                        yield true;
                    }
                }
                yield false;
            }
            case NOT -> !rules.getFirst().applies(context);
        };
    }
    
    @Override
    public String getDescription() {
        StringBuilder description = new StringBuilder();
        
        switch (operation) {
            case AND:
                description.append("All of the following apply: [");
                break;
            
            case OR:
                description.append("Any of the following apply: [");
                break;
            
            case NOT:
                description.append("The following does not apply: [");
                break;
        }
        
        boolean first = true;
        for (Rule rule : rules) {
            if (!first) {
                description.append(", ");
            }
            description.append(rule.getDescription());
            first = false;
        }
        
        description.append("]");
        return description.toString();
    }

    /// The logical operation to apply to the rules.
    public enum Operation {
        /// Logical AND - all rules must apply
        AND,

        ///Logical OR - at least one rule must apply
        OR,

        ///Logical NOT - the rule must not apply
        NOT
    }
}