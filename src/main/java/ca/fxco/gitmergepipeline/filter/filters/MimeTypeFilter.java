package ca.fxco.gitmergepipeline.filter.filters;

import ca.fxco.gitmergepipeline.filter.Filter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.TreeFilter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A filter that matches the file mime type.
 *
 * @author FX
 */
public final class MimeTypeFilter extends TreeFilter implements Filter {

    private final String mimeType;

    @JsonCreator
    public MimeTypeFilter(
            @JsonProperty("mimeType") String mimeType
    ) {
        this.mimeType = mimeType;
    }

    @Override
    public String getDescription() {
        return "MimeType filter: " + mimeType;
    }

    @Override
    public TreeFilter getTreeFilter() {
        return this;
    }

    @Override
    public boolean include(TreeWalk walker) {
        try {
            return mimeType.equals(Files.probeContentType(Path.of(walker.getPathString())));
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public boolean shouldBeRecursive() {
        return false;
    }

    @Override
    public TreeFilter clone() {
        return this;
    }
}
