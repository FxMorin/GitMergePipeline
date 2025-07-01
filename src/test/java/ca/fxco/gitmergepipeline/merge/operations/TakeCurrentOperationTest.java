package ca.fxco.gitmergepipeline.merge.operations;

import ca.fxco.gitmergepipeline.merge.MergeContext;
import ca.fxco.gitmergepipeline.merge.MergeResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the TakeCurrentOperation class which resolves conflicts by using the current version.
 * Tests include operation with merge driver, merge tool, and handling of null paths.
 *
 * @author FX
 */
class TakeCurrentOperationTest {

    @TempDir
    Path tempDir;

    private Path baseFile;
    private Path currentFile;
    private Path otherFile;
    private Path mergedFile;
    private TakeCurrentOperation operation;

    @BeforeEach
    void setUp() throws IOException {
        // Create test files
        baseFile = tempDir.resolve("base.txt");
        currentFile = tempDir.resolve("current.txt");
        otherFile = tempDir.resolve("other.txt");
        mergedFile = tempDir.resolve("merged.txt");

        Files.writeString(baseFile, "Base content");
        Files.writeString(currentFile, "Current content");
        Files.writeString(otherFile, "Other content");

        // Create the operation
        operation = new TakeCurrentOperation();
    }

    @Test
    void getName() {
        assertEquals("take-current", operation.getName());
    }

    @Test
    void getDescription() {
        assertTrue(operation.getDescription().contains("current version"));
    }

    @Test
    void executeWithMergeDriver() throws IOException {
        MergeContext context = new MergeContext(baseFile, currentFile, otherFile, "test.txt");

        MergeResult result = operation.execute(context, Collections.emptyList());

        assertTrue(result.isSuccess());
        assertEquals(currentFile, result.getOutputPath());
        assertEquals("Current content", Files.readString(currentFile));
    }

    @Test
    void executeWithMergeTool() throws IOException {
        MergeContext context = MergeContext.forMergeTool(currentFile, otherFile, mergedFile);

        MergeResult result = operation.execute(context, Collections.emptyList());

        assertTrue(result.isSuccess());
        assertEquals(mergedFile, result.getOutputPath());
        assertEquals("Current content", Files.readString(mergedFile));
    }

    @Test
    void executeWithNullCurrentPath() throws IOException {
        MergeContext context = new MergeContext(baseFile, null, otherFile, "test.txt");

        MergeResult result = operation.execute(context, Collections.emptyList());

        assertTrue(result.isError());
        assertTrue(result.getMessage().contains("Current path is null"));
    }
}
