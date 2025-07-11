package ca.fxco.gitmergepipeline.rule;

import ca.fxco.gitmergepipeline.merge.GitMergeContext;
import ca.fxco.gitmergepipeline.merge.MergeContext;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A rule that checks if a files mime type matches a specific mime type.
 *
 * @author FX
 */
public class MimeTypeRule implements Rule {
    private final String mimeType;

    /**
     * Creates a new mime type rule.
     *
     * @param mimeType The mime type of the file
     */
    @JsonCreator
    public MimeTypeRule(@JsonProperty("mimeType") String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * Gets the mime type used by this rule.
     * 
     * @return The mime type string
     */
    public String getMimeType() {
        return this.mimeType;
    }

    @Override
    public boolean applies(MergeContext context) {
        try {
            return mimeType.equals(Files.probeContentType(Path.of(context.getFilePath())));
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public boolean applies(GitMergeContext context) {
        try {
            return mimeType.equals(Files.probeContentType(Path.of(context.getFilePath())));
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public String getDescription() {
        return "MimeType rule: " + mimeType;
    }
}
