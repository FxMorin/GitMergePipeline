package ca.fxco.gitmergepipeline.rule;

import ca.fxco.gitmergepipeline.merge.MergeContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the CompositeRule class which combines multiple rules with logical operations.
 * Tests include AND, OR, and NOT operations with various combinations of rules,
 * as well as nested composite rules.
 *
 * @author FX
 */
class CompositeRuleTest {

    private MergeContext context;
    private Rule alwaysAppliesRule;
    private Rule neverAppliesRule;

    @BeforeEach
    void setUp() {
        // Create a mock context
        context = new MergeContext(
                Paths.get("base.txt"),
                Paths.get("current.txt"),
                Paths.get("other.txt"),
                "test.txt"
        );

        // Create mock rules
        alwaysAppliesRule = new Rule() {
            @Override
            public boolean applies(MergeContext context) {
                return true;
            }

            @Override
            public String getDescription() {
                return "Always applies";
            }
        };

        neverAppliesRule = new Rule() {
            @Override
            public boolean applies(MergeContext context) {
                return false;
            }

            @Override
            public String getDescription() {
                return "Never applies";
            }
        };
    }

    @Test
    void andOperationWithAllRulesApplying() {
        CompositeRule rule = new CompositeRule(
                CompositeRule.Operation.AND,
                Arrays.asList(alwaysAppliesRule, alwaysAppliesRule)
        );

        assertTrue(rule.applies(context));
    }

    @Test
    void andOperationWithOneRuleNotApplying() {
        CompositeRule rule = new CompositeRule(
                CompositeRule.Operation.AND,
                Arrays.asList(alwaysAppliesRule, neverAppliesRule)
        );

        assertFalse(rule.applies(context));
    }

    @Test
    void andOperationWithNoRules() {
        CompositeRule rule = new CompositeRule(
                CompositeRule.Operation.AND,
                Collections.emptyList()
        );

        assertFalse(rule.applies(context));
    }

    @Test
    void orOperationWithOneRuleApplying() {
        CompositeRule rule = new CompositeRule(
                CompositeRule.Operation.OR,
                Arrays.asList(alwaysAppliesRule, neverAppliesRule)
        );

        assertTrue(rule.applies(context));
    }

    @Test
    void orOperationWithNoRulesApplying() {
        CompositeRule rule = new CompositeRule(
                CompositeRule.Operation.OR,
                Arrays.asList(neverAppliesRule, neverAppliesRule)
        );

        assertFalse(rule.applies(context));
    }

    @Test
    void orOperationWithNoRules() {
        CompositeRule rule = new CompositeRule(
                CompositeRule.Operation.OR,
                Collections.emptyList()
        );

        assertFalse(rule.applies(context));
    }

    @Test
    void notOperationWithApplyingRule() {
        CompositeRule rule = new CompositeRule(
                CompositeRule.Operation.NOT,
                Collections.singletonList(alwaysAppliesRule)
        );

        assertFalse(rule.applies(context));
    }

    @Test
    void notOperationWithNonApplyingRule() {
        CompositeRule rule = new CompositeRule(
                CompositeRule.Operation.NOT,
                Collections.singletonList(neverAppliesRule)
        );

        assertTrue(rule.applies(context));
    }

    @Test
    void notOperationRequiresExactlyOneRule() {
        assertThrows(IllegalArgumentException.class, () -> {
            new CompositeRule(
                    CompositeRule.Operation.NOT,
                    Arrays.asList(alwaysAppliesRule, neverAppliesRule)
            );
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new CompositeRule(
                    CompositeRule.Operation.NOT,
                    Collections.emptyList()
            );
        });
    }

    @Test
    void staticFactoryMethodsCreateCorrectRules() {
        CompositeRule andRule = CompositeRule.and(Arrays.asList(alwaysAppliesRule, neverAppliesRule));
        CompositeRule orRule = CompositeRule.or(Arrays.asList(alwaysAppliesRule, neverAppliesRule));
        CompositeRule notRule = CompositeRule.not(alwaysAppliesRule);

        assertEquals(CompositeRule.Operation.AND, andRule.getOperation());
        assertEquals(2, andRule.getRules().size());

        assertEquals(CompositeRule.Operation.OR, orRule.getOperation());
        assertEquals(2, orRule.getRules().size());

        assertEquals(CompositeRule.Operation.NOT, notRule.getOperation());
        assertEquals(1, notRule.getRules().size());
    }

    @Test
    void getDescription() {
        CompositeRule andRule = new CompositeRule(
                CompositeRule.Operation.AND,
                Arrays.asList(alwaysAppliesRule, neverAppliesRule)
        );

        CompositeRule orRule = new CompositeRule(
                CompositeRule.Operation.OR,
                Arrays.asList(alwaysAppliesRule, neverAppliesRule)
        );

        CompositeRule notRule = new CompositeRule(
                CompositeRule.Operation.NOT,
                Collections.singletonList(alwaysAppliesRule)
        );

        assertEquals("All of the following apply: [Always applies, Never applies]", andRule.getDescription());
        assertEquals("Any of the following apply: [Always applies, Never applies]", orRule.getDescription());
        assertEquals("The following does not apply: [Always applies]", notRule.getDescription());
    }

    @Test
    void nestedCompositeRules() {
        // (true AND false) OR (NOT true) = false OR false = false
        CompositeRule andRule = new CompositeRule(
                CompositeRule.Operation.AND,
                Arrays.asList(alwaysAppliesRule, neverAppliesRule)
        );

        CompositeRule notRule = new CompositeRule(
                CompositeRule.Operation.NOT,
                Collections.singletonList(alwaysAppliesRule)
        );

        CompositeRule orRule = new CompositeRule(
                CompositeRule.Operation.OR,
                Arrays.asList(andRule, notRule)
        );

        assertFalse(orRule.applies(context));

        // (true AND true) OR (NOT false) = true OR true = true
        CompositeRule andRule2 = new CompositeRule(
                CompositeRule.Operation.AND,
                Arrays.asList(alwaysAppliesRule, alwaysAppliesRule)
        );

        CompositeRule notRule2 = new CompositeRule(
                CompositeRule.Operation.NOT,
                Collections.singletonList(neverAppliesRule)
        );

        CompositeRule orRule2 = new CompositeRule(
                CompositeRule.Operation.OR,
                Arrays.asList(andRule2, notRule2)
        );

        assertTrue(orRule2.applies(context));
    }
}
