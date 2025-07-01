package ca.fxco.gitmergepipeline.rule;

import ca.fxco.gitmergepipeline.merge.MergeContext;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

/**
 * A rule that checks if the content of a file matches a specific pattern.
 * The pattern is a regular expression that matched against the file content.
 *
 * @author FX
 */
public class ContentPatternRule implements Rule {
    private static final Logger logger = LoggerFactory.getLogger(ContentPatternRule.class);
    
    private final String pattern;
    private final boolean caseSensitive;
    private final Pattern compiledPattern;
    private final boolean checkBase;
    private final boolean checkCurrent;
    private final boolean checkOther;
    
    /**
     * Creates a new content pattern rule.
     * 
     * @param pattern The regular expression pattern to match against file content
     * @param caseSensitive Whether the pattern matching should be case-sensitive
     * @param checkBase Whether to check the base version of the file
     * @param checkCurrent Whether to check the current version of the file
     * @param checkOther Whether to check the other version of the file
     */
    @JsonCreator
    public ContentPatternRule(
            @JsonProperty("pattern") String pattern,
            @JsonProperty("caseSensitive") boolean caseSensitive,
            @JsonProperty("checkBase") boolean checkBase,
            @JsonProperty("checkCurrent") boolean checkCurrent,
            @JsonProperty("checkOther") boolean checkOther
    ) {
        this.pattern = pattern;
        this.caseSensitive = caseSensitive;
        this.checkBase = checkBase;
        this.checkCurrent = checkCurrent;
        this.checkOther = checkOther;
        
        int flags = caseSensitive ? 0 : Pattern.CASE_INSENSITIVE;
        this.compiledPattern = Pattern.compile(pattern, flags);
    }
    
    /**
     * Creates a new content pattern rule that checks all versions of the file.
     * 
     * @param pattern The regular expression pattern to match against file content
     * @param caseSensitive Whether the pattern matching should be case-sensitive
     */
    public ContentPatternRule(String pattern, boolean caseSensitive) {
        this(pattern, caseSensitive, true, true, true);
    }
    
    /**
     * Creates a new content pattern rule that checks all versions of the file with case-insensitive matching.
     * 
     * @param pattern The regular expression pattern to match against file content
     */
    public ContentPatternRule(String pattern) {
        this(pattern, false);
    }
    
    /**
     * Gets the pattern used by this rule.
     * 
     * @return The pattern string
     */
    public String getPattern() {
        return pattern;
    }
    
    /**
     * Checks whether the pattern matching is case-sensitive.
     * 
     * @return true if the pattern matching is case-sensitive, false otherwise
     */
    public boolean isCaseSensitive() {
        return caseSensitive;
    }
    
    /**
     * Checks whether the base version of the file should be checked.
     * 
     * @return true if the base version should be checked, false otherwise
     */
    public boolean isCheckBase() {
        return checkBase;
    }
    
    /**
     * Checks whether the current version of the file should be checked.
     * 
     * @return true if the current version should be checked, false otherwise
     */
    public boolean isCheckCurrent() {
        return checkCurrent;
    }
    
    /**
     * Checks whether the other version of the file should be checked.
     * 
     * @return true if the other version should be checked, false otherwise
     */
    public boolean isCheckOther() {
        return checkOther;
    }
    
    @Override
    public boolean applies(MergeContext context) {
        if (checkBase && context.getBasePath() != null) {
            if (matchesContent(context.getBasePath())) {
                return true;
            }
        }
        
        if (checkCurrent && context.getCurrentPath() != null) {
            if (matchesContent(context.getCurrentPath())) {
                return true;
            }
        }
        
        if (checkOther && context.getOtherPath() != null) {
            if (matchesContent(context.getOtherPath())) {
                return true;
            }
        }
        
        return false;
    }
    
    private boolean matchesContent(Path path) {
        try {
            String content = Files.readString(path);
            return compiledPattern.matcher(content).find();
        } catch (IOException e) {
            logger.error("Error reading file content: {}", path, e);
            return false;
        }
    }
    
    @Override
    public String getDescription() {
        StringBuilder description = new StringBuilder("Content matches pattern '")
                .append(pattern)
                .append("' (")
                .append(caseSensitive ? "case-sensitive" : "case-insensitive")
                .append(") in ");
        
        if (checkBase && checkCurrent && checkOther) {
            description.append("any version");
        } else {
            boolean first = true;
            
            if (checkBase) {
                description.append("base version");
                first = false;
            }
            
            if (checkCurrent) {
                if (!first) {
                    description.append(", ");
                }
                description.append("current version");
                first = false;
            }
            
            if (checkOther) {
                if (!first) {
                    description.append(", ");
                }
                description.append("other version");
            }
        }
        
        return description.toString();
    }
}