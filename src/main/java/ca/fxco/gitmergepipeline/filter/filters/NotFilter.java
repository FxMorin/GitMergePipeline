package ca.fxco.gitmergepipeline.filter.filters;

import ca.fxco.gitmergepipeline.filter.Filter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.jgit.treewalk.filter.NotTreeFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;

/**
 * A special type of filter, which inverts the condition of the filter provided.
 *
 * @author FX
 */
public class NotFilter implements Filter {

    private final TreeFilter filter;

    /**
     * Creates a new Not filter.
     *
     * @param filter The filter that should be inverted.
     */
    @JsonCreator
    public NotFilter(
            @JsonProperty("filter") Filter filter
    ) {
        this.filter = NotTreeFilter.create(filter.getTreeFilter());
    }

    @Override
    public String getDescription() {
        return "A filter that inverts another filter";
    }

    @Override
    public TreeFilter getTreeFilter() {
        return this.filter;
    }
}
