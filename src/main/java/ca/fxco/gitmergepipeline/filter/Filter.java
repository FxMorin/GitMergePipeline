package ca.fxco.gitmergepipeline.filter;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.eclipse.jgit.treewalk.filter.TreeFilter;

/**
 * Interface for diff filters.<br>
 * Filters are executed on the diff between two commits, and can be used to filter what files are merged.<br>
 * Filtering files will prevent them from being compared, which can save time and memory.
 *
 * @author FX
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
public interface Filter {

    /**
     * Gets a description of this filter.
     *
     * @return A human-readable description of the filter
     */
    String getDescription();

    /**
     * Gets the JGit tree filter used by this filter.
     * The filter is applied to the diff between two commits.
     *
     * @return The JGit tree filter
     */
    TreeFilter getTreeFilter();
}
