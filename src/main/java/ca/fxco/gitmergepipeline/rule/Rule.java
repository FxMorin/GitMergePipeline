package ca.fxco.gitmergepipeline.rule;

import ca.fxco.gitmergepipeline.merge.MergeContext;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Interface for rules that can be applied in a merge pipeline.
 * Rules determine whether a specific merge operation should be performed
 * based on the current merge context.
 *
 * @author FX
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = FilePatternRule.class, name = "filePattern"),
        @JsonSubTypes.Type(value = ContentPatternRule.class, name = "contentPattern"),
        @JsonSubTypes.Type(value = CompositeRule.class, name = "composite")
})
public interface Rule {
    
    /**
     * Evaluates whether this rule applies to the given merge context.
     * 
     * @param context The merge context to evaluate
     * @return true if the rule applies, false otherwise
     */
    boolean applies(MergeContext context);
    
    /**
     * Gets a description of this rule.
     * 
     * @return A human-readable description of the rule
     */
    String getDescription();
}