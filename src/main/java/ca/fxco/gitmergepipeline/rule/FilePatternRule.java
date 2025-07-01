package ca.fxco.gitmergepipeline.rule;

import ca.fxco.gitmergepipeline.merge.MergeContext;
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
            String regex = convertGlobToRegex(pattern);
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
    public String getDescription() {
        String patternType = isRegex ? "regex" : "glob";
        String sensitivity = caseSensitive ? "case-sensitive" : "case-insensitive";
        return String.format("File matches %s pattern '%s' (%s)", patternType, pattern, sensitivity);
    }

    /**
     * Converts a glob pattern to a regular expression pattern.
     * 
     * @param glob The glob pattern to convert
     * @return The equivalent regular expression pattern
     */
    private String convertGlobToRegex(String glob) {
        StringBuilder regex = new StringBuilder("^");
        boolean escaping = false;

        for (int i = 0; i < glob.length(); i++) {
            char c = glob.charAt(i);

            if (escaping) {
                // If we're escaping, just add the character
                regex.append(Pattern.quote(String.valueOf(c)));
                escaping = false;
            } else {
                switch (c) {
                    case '\\':
                        escaping = true;
                        break;
                    case '*':
                        if (i + 1 < glob.length() && glob.charAt(i + 1) == '*') {
                            // ** matches any number of directories
                            regex.append(".*");
                            i++; // Skip the next *
                        } else {
                            // * matches any number of characters except /
                            regex.append("[^/]*");
                        }
                        break;
                    case '?':
                        // ? matches a single character except /
                        regex.append("[^/]");
                        break;
                    case '.', '(', ')', '+', '|', '^', '$', '@', '%':
                        // Escape special regex characters
                        regex.append("\\").append(c);
                        break;
                    case '{':
                        // {a,b,c} matches any of a, b, or c
                        regex.append("(?:");
                        break;
                    case '}':
                        regex.append(")");
                        break;
                    case ',':
                        regex.append("|");
                        break;
                    case '[':
                        // Character class
                        regex.append("[");
                        if (i + 1 < glob.length() && glob.charAt(i + 1) == '!') {
                            regex.append("^");
                            i++;
                        } else if (i + 1 < glob.length() && glob.charAt(i + 1) == '^') {
                            regex.append("\\^");
                            i++;
                        }
                        break;
                    default:
                        regex.append(c);
                }
            }
        }

        regex.append("$");
        return regex.toString();
    }
}
