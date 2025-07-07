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
 * Tests for the KeepBaseOperation class which resolves conflicts by using the base version.
 * Tests include operation with merge driver, merge tool, and handling of null paths.
 *
 * @author FX
 */
class KeepBaseOperationTest {

    @TempDir
    Path tempDir;

    private Path baseFile;
    private Path currentFile;
    private Path otherFile;
    private Path mergedFile;
    private KeepBaseOperation operation;

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
        operation = new KeepBaseOperation();
    }

    @Test
    void getName() {
        assertEquals("keep-base", operation.getName());
    }

    @Test
    void getDescription() {
        assertTrue(operation.getDescription().contains("base version"));
    }

    @Test
    void executeWithMergeDriver() throws IOException {
        MergeContext context = new MergeContext(baseFile, currentFile, otherFile, "test.txt");

        MergeResult result = operation.execute(context, Collections.emptyList());

        assertTrue(result.isSuccess());
        assertEquals(baseFile, result.getOutputPath());
        assertEquals("Base content", Files.readString(baseFile));
    }

    @Test
    void executeWithMergeTool() throws IOException {
        // Attempts merge tool with no base, which means it should stay removed.
        MergeContext context = MergeContext.forMergeTool(currentFile, otherFile, mergedFile);

        MergeResult result = operation.execute(context, Collections.emptyList());

        assertTrue(result.isSuccess());
        assertTrue(result.getMessage().contains("Keep-base operation successful"));
        assertNull(result.getOutputPath());
    }

    @Test
    void executeWithNullBasePath() throws IOException {
        MergeContext context = new MergeContext(null, currentFile, otherFile, "test.txt");

        MergeResult result = operation.execute(context, Collections.emptyList());

        assertTrue(result.isSuccess());
        assertTrue(result.getMessage().contains("Base path is null"));
    }
}
