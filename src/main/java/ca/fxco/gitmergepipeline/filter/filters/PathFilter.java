package ca.fxco.gitmergepipeline.filter.filters;

import ca.fxco.gitmergepipeline.filter.Filter;
import ca.fxco.gitmergepipeline.utils.FileUtils;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.TreeFilter;

import java.util.regex.Pattern;

/**
 * A filter that matches the FileMode of a file.
 *
 * @author FX
 */
public final class PathFilter extends TreeFilter implements Filter {

    private final String pattern;
    private final boolean isRegex;
    private final boolean caseSensitive;
    private final Pattern compiledPattern;

    @JsonCreator
    public PathFilter(
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

    @Override
    public String getDescription() {
        String patternType = isRegex ? "regex" : "glob";
        String sensitivity = caseSensitive ? "case-sensitive" : "case-insensitive";
        return String.format("Path filter: %s pattern '%s' (%s)", patternType, pattern, sensitivity);
    }


    @Override
    public TreeFilter getTreeFilter() {
        return this;
    }

    @Override
    public boolean include(TreeWalk walker) {
        return this.compiledPattern.matcher(walker.getPathString()).matches();
    }

    @Override
    public boolean shouldBeRecursive() {
        return false;
    }

    @Override
    public TreeFilter clone() {
        return this;
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
}
