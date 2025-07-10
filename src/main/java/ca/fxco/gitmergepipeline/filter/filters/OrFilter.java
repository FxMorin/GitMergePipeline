package ca.fxco.gitmergepipeline.filter.filters;

import ca.fxco.gitmergepipeline.filter.Filter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.jgit.treewalk.filter.OrTreeFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;

import java.util.List;

/**
 * A special type of filter, which combines multiple other filters together into a single filter. <br>
 * By default all filters are `AND` together. Which means they must all be true,
 * this filter allows you to change that to `OR`.
 *
 * @author FX
 */
public class OrFilter implements Filter {

    private final TreeFilter filter;

    /**
     * Creates a new Or filter.
     *
     * @param filters The filters that should be combined.
     */
    @JsonCreator
    public OrFilter(
            @JsonProperty("filters") List<Filter> filters
    ) {
        this.filter = OrTreeFilter.create(filters.stream().map(Filter::getTreeFilter).toArray(TreeFilter[]::new));
    }
    
    @Override
    public String getDescription() {
        return "A filter that matches if any of the sub-filters match";
    }

    @Override
    public TreeFilter getTreeFilter() {
        return this.filter;
    }
}