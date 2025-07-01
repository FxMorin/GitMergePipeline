package ca.fxco.gitmergepipeline.rule;

import ca.fxco.gitmergepipeline.merge.MergeContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the ContentPatternRule class which matches files based on their content.
 * Tests include pattern matching with different configurations including case sensitivity
 * and checking different versions of files (base, current, other).
 *
 * @author FX
 */
class ContentPatternRuleTest {

    @TempDir
    Path tempDir;

    private Path baseFile;
    private Path currentFile;
    private Path otherFile;

    @BeforeEach
    void setUp() throws IOException {
        // Create test files with different content
        baseFile = tempDir.resolve("base.txt");
        currentFile = tempDir.resolve("current.txt");
        otherFile = tempDir.resolve("other.txt");

        Files.writeString(baseFile, "This is the base version of the file.");
        Files.writeString(currentFile, "This is the CURRENT version of the file with some changes.");
        Files.writeString(otherFile, "This is the other version with different changes.");
    }

    @Test
    void appliesWithCaseSensitivity() throws IOException {
        ContentPatternRule caseSensitiveRule = new ContentPatternRule("CURRENT", true);
        ContentPatternRule caseInsensitiveRule = new ContentPatternRule("CURRENT", false);

        MergeContext context = new MergeContext(baseFile, currentFile, otherFile, "test.txt");

        assertTrue(caseSensitiveRule.applies(context));
        assertTrue(caseInsensitiveRule.applies(context));

        // Create a context with files that don't contain "CURRENT" in uppercase
        Path newCurrentFile = tempDir.resolve("new_current.txt");
        Files.writeString(newCurrentFile, "This is the current version without uppercase.");

        MergeContext newContext = new MergeContext(baseFile, newCurrentFile, otherFile, "test.txt");

        assertFalse(caseSensitiveRule.applies(newContext));
        assertTrue(caseInsensitiveRule.applies(newContext));
    }

    @Test
    void appliesWithVersionSelection() throws IOException {
        // Rule that only checks the base version
        ContentPatternRule baseOnlyRule = new ContentPatternRule(
                "base version", false, true, false, false);

        // Rule that only checks the current version
        ContentPatternRule currentOnlyRule = new ContentPatternRule(
                "current version", false, false, true, false);

        // Rule that only checks the other version
        ContentPatternRule otherOnlyRule = new ContentPatternRule(
                "other version", false, false, false, true);

        MergeContext context = new MergeContext(baseFile, currentFile, otherFile, "test.txt");

        assertTrue(baseOnlyRule.applies(context));
        assertTrue(currentOnlyRule.applies(context));
        assertTrue(otherOnlyRule.applies(context));

        // Create a context with files that don't contain the patterns
        Path newBaseFile = tempDir.resolve("new_base.txt");
        Path newCurrentFile = tempDir.resolve("new_current.txt");
        Path newOtherFile = tempDir.resolve("new_other.txt");

        Files.writeString(newBaseFile, "This is a different base file.");
        Files.writeString(newCurrentFile, "This is a different current file.");
        Files.writeString(newOtherFile, "This is a different other file.");

        MergeContext newContext = new MergeContext(newBaseFile, newCurrentFile, newOtherFile, "test.txt");

        assertFalse(baseOnlyRule.applies(newContext));
        assertFalse(currentOnlyRule.applies(newContext));
        assertFalse(otherOnlyRule.applies(newContext));
    }

    @Test
    void appliesWithMultipleVersions() throws IOException {
        // Rule that checks base and current versions
        ContentPatternRule baseAndCurrentRule = new ContentPatternRule(
                "version", false, true, true, false);

        // Create a context where only the base file matches
        Path newCurrentFile = tempDir.resolve("new_current.txt");
        Files.writeString(newCurrentFile, "This is a different current file.");

        MergeContext context = new MergeContext(baseFile, newCurrentFile, otherFile, "test.txt");

        assertTrue(baseAndCurrentRule.applies(context));

        // Create a context where only the current file matches
        Path newBaseFile = tempDir.resolve("new_base.txt");
        Files.writeString(newBaseFile, "This is a different base file.");

        MergeContext newContext = new MergeContext(newBaseFile, currentFile, otherFile, "test.txt");

        assertTrue(baseAndCurrentRule.applies(newContext));

        // Create a context where neither file matches
        MergeContext nonMatchingContext = new MergeContext(newBaseFile, newCurrentFile, otherFile, "test.txt");

        assertFalse(baseAndCurrentRule.applies(nonMatchingContext));
    }

    @Test
    void getDescription() {
        ContentPatternRule allVersionsRule = new ContentPatternRule("pattern", true);
        ContentPatternRule baseOnlyRule = new ContentPatternRule(
                "pattern", false, true, false, false);
        ContentPatternRule currentAndOtherRule = new ContentPatternRule(
                "pattern", false, false, true, true);

        assertEquals("Content matches pattern 'pattern' (case-sensitive) in any version", 
                allVersionsRule.getDescription());
        assertEquals("Content matches pattern 'pattern' (case-insensitive) in base version", 
                baseOnlyRule.getDescription());
        assertEquals("Content matches pattern 'pattern' (case-insensitive) in current version, other version", 
                currentAndOtherRule.getDescription());
    }

    @Test
    void getPattern() {
        ContentPatternRule rule = new ContentPatternRule("pattern", true);

        assertEquals("pattern", rule.getPattern());
    }

    @Test
    void isCaseSensitive() {
        ContentPatternRule sensitiveRule = new ContentPatternRule("pattern", true);
        ContentPatternRule insensitiveRule = new ContentPatternRule("pattern", false);

        assertTrue(sensitiveRule.isCaseSensitive());
        assertFalse(insensitiveRule.isCaseSensitive());
    }

    @Test
    void isCheckBase() {
        ContentPatternRule allVersionsRule = new ContentPatternRule("pattern", true);
        ContentPatternRule noBaseRule = new ContentPatternRule(
                "pattern", true, false, true, true);

        assertTrue(allVersionsRule.isCheckBase());
        assertFalse(noBaseRule.isCheckBase());
    }

    @Test
    void isCheckCurrent() {
        ContentPatternRule allVersionsRule = new ContentPatternRule("pattern", true);
        ContentPatternRule noCurrentRule = new ContentPatternRule(
                "pattern", true, true, false, true);

        assertTrue(allVersionsRule.isCheckCurrent());
        assertFalse(noCurrentRule.isCheckCurrent());
    }

    @Test
    void isCheckOther() {
        ContentPatternRule allVersionsRule = new ContentPatternRule("pattern", true);
        ContentPatternRule noOtherRule = new ContentPatternRule(
                "pattern", true, true, true, false);

        assertTrue(allVersionsRule.isCheckOther());
        assertFalse(noOtherRule.isCheckOther());
    }
}
