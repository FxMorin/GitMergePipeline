package ca.fxco.gitmergepipeline.rule;

import ca.fxco.gitmergepipeline.merge.MergeContext;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.List;

/**
 * A rule that checks if a files extension matches one of the extensions specified.
 *
 * @author FX
 */
public class FileExtensionRule implements Rule {
    private final String[] extensions;
    private final boolean invert;

    /**
     * Creates a new file extension rule.
     *
     * @param extensions The file extensions to match
     */
    @JsonCreator
    public FileExtensionRule(
            @JsonProperty("extensions") List<String> extensions,
            @JsonProperty("invert") boolean invert
    ) {
        this.extensions = extensions != null ? extensions.toArray(new String[0]) : new String[0];
        this.invert = invert;
    }

    @Override
    public boolean applies(MergeContext context) {
        String extension = context.getFileExtension();
        for (String ext : extensions) {
            if (ext.equals(extension)) {
                return !invert;
            }
        }
        return invert;
    }

    @Override
    public String getDescription() {
        return "File extensions rule: " + Arrays.toString(extensions) + (invert ? "- inverted" : "");
    }
}
