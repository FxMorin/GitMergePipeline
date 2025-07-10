package ca.fxco.gitmergepipeline.filter.filters;

import ca.fxco.gitmergepipeline.filter.Filter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.TreeFilter;

/**
 * A filter that matches the FileMode of a file.
 *
 * @author FX
 */
public final class FileModeFilter extends TreeFilter implements Filter {

    private final FileMode mode;

    @JsonCreator
    public FileModeFilter(
            @JsonProperty("mode") FileModeEnum mode
    ) {
        this.mode = mode.getMode();
    }

    @Override
    public String getDescription() {
        return "FileMode filter: " + this.mode;
    }

    @Override
    public TreeFilter getTreeFilter() {
        return this;
    }

    @Override
    public boolean include(TreeWalk walker) {
        return this.mode.equals(walker.getFileMode().getBits());
    }

    @Override
    public boolean shouldBeRecursive() {
        return false;
    }

    @Override
    public TreeFilter clone() {
        return this;
    }


    public enum FileModeEnum {
        EXECUTABLE(FileMode.EXECUTABLE_FILE),
        REGULAR(FileMode.REGULAR_FILE),
        SYMLINK(FileMode.SYMLINK),
        GITLINK(FileMode.GITLINK),
        TREE(FileMode.TREE);

        private final FileMode mode;

        FileModeEnum(FileMode mode) {
            this.mode = mode;
        }

        public FileMode getMode() {
            return mode;
        }

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }
}
