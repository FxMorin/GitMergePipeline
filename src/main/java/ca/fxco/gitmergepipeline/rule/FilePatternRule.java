package ca.fxco.gitmergepipeline.rule;

import ca.fxco.gitmergepipeline.merge.GitMergeContext;
import ca.fxco.gitmergepipeline.merge.MergeContext;
import ca.fxco.gitmergepipeline.utils.FileUtils;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.regex.Pattern;

/**
 * A rule that checks if a file matches a specific pattern.
 * The pattern can be a glob pattern (e.g., "*.java") or a regular expression.
 *
 * @author FX
 */
public class FilePatternRule implements Rule {
    private final String pattern;
    private final boolean isRegex;
    private final boolean caseSensitive;
    private final Pattern compiledPattern;

    /**
     * Creates a new file pattern rule.
     * 
     * @param pattern The pattern to match against file paths
     * @param isRegex Whether the pattern is a regular expression (true) or a glob pattern (false)
     * @param caseSensitive Whether the pattern matching should be case-sensitive
     */
    @JsonCreator
    public FilePatternRule(
            @JsonProperty("pattern") String pattern,
            @JsonProperty("isRegex") boolean isRegex,
            @JsonProperty("caseSensitive") boolean caseSensitive
    ) {
        this.pattern = pattern;
        this.isRegex = isRegex;
        this.caseSensitive = caseSensitive;

        if (isRegex) {
            int flags = caseSensitive ? 0 : Pattern.CASE_INSENSITIVE;
            this.compiledPattern = Pattern.compile(pattern, flags);
        } else {
            // Convert glob pattern to regex
            String regex = FileUtils.convertGlobToRegex(pattern);
            int flags = caseSensitive ? 0 : Pattern.CASE_INSENSITIVE;
            this.compiledPattern = Pattern.compile(regex, flags);
        }
    }

    /**
     * Creates a new file pattern rule with a glob pattern and case-insensitive matching.
     * 
     * @param pattern The glob pattern to match against file paths
     */
    public FilePatternRule(String pattern) {
        this(pattern, false, false);
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
     * Checks whether the pattern is a regular expression.
     * 
     * @return true if the pattern is a regular expression, false if it's a glob pattern
     */
    public boolean isRegex() {
        return isRegex;
    }

    /**
     * Checks whether the pattern matching is case-sensitive.
     * 
     * @return true if the pattern matching is case-sensitive, false otherwise
     */
    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    @Override
    public boolean applies(MergeContext context) {
        String filePath = context.getFilePath();
        return compiledPattern.matcher(filePath).matches();
    }

    @Override
    public boolean applies(GitMergeContext context) {
        String filePath = context.getFilePath();
        return compiledPattern.matcher(filePath).matches();
    }

    @Override
    public String getDescription() {
        String patternType = isRegex ? "regex" : "glob";
        String sensitivity = caseSensitive ? "case-sensitive" : "case-insensitive";
        return String.format("File matches %s pattern '%s' (%s)", patternType, pattern, sensitivity);
    }
}
