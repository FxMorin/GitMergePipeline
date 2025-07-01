package ca.fxco.gitmergepipeline.rule;

import ca.fxco.gitmergepipeline.merge.MergeContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the FilePatternRule class which matches files based on glob or regex patterns.
 * Tests include pattern matching with different configurations including case sensitivity
 * and various file path patterns.
 *
 * @author FX
 */
class FilePatternRuleTest {

    @Test
    void appliesWithGlobPattern() {
        FilePatternRule rule = new FilePatternRule("*.java", false, false);

        MergeContext javaContext = new MergeContext(null, null, null, "Test.java");
        MergeContext txtContext = new MergeContext(null, null, null, "Test.txt");

        assertTrue(rule.applies(javaContext));
        assertFalse(rule.applies(txtContext));
    }

    @Test
    void appliesWithRegexPattern() {
        FilePatternRule rule = new FilePatternRule(".*\\.java", true, false);

        MergeContext javaContext = new MergeContext(null, null, null, "Test.java");
        MergeContext txtContext = new MergeContext(null, null, null, "Test.txt");

        assertTrue(rule.applies(javaContext));
        assertFalse(rule.applies(txtContext));
    }

    @Test
    void appliesWithCaseSensitivity() {
        FilePatternRule caseSensitiveRule = new FilePatternRule("*.JAVA", false, true);
        FilePatternRule caseInsensitiveRule = new FilePatternRule("*.JAVA", false, false);

        MergeContext upperContext = new MergeContext(null, null, null, "Test.JAVA");
        MergeContext lowerContext = new MergeContext(null, null, null, "Test.java");

        assertTrue(caseSensitiveRule.applies(upperContext));
        assertFalse(caseSensitiveRule.applies(lowerContext));

        assertTrue(caseInsensitiveRule.applies(upperContext));
        assertTrue(caseInsensitiveRule.applies(lowerContext));
    }

    @Test
    void appliesWithDirectoryPattern() {
        FilePatternRule rule = new FilePatternRule("src/main/java/*.java", false, false);

        MergeContext matchingContext = new MergeContext(null, null, null, "src/main/java/Test.java");
        MergeContext nonMatchingContext = new MergeContext(null, null, null, "src/test/java/Test.java");

        assertTrue(rule.applies(matchingContext));
        assertFalse(rule.applies(nonMatchingContext));
    }

    @Test
    void appliesWithWildcardDirectoryPattern() {
        FilePatternRule rule = new FilePatternRule("src/**/java/*.java", false, false);

        MergeContext mainContext = new MergeContext(null, null, null, "src/main/java/Test.java");
        MergeContext testContext = new MergeContext(null, null, null, "src/test/java/Test.java");
        MergeContext otherContext = new MergeContext(null, null, null, "src/other/Test.java");

        assertTrue(rule.applies(mainContext));
        assertTrue(rule.applies(testContext));
        assertFalse(rule.applies(otherContext));
    }

    @Test
    void getDescription() {
        FilePatternRule globRule = new FilePatternRule("*.java", false, false);
        FilePatternRule regexRule = new FilePatternRule(".*\\.java", true, true);

        assertEquals("File matches glob pattern '*.java' (case-insensitive)", globRule.getDescription());
        assertEquals("File matches regex pattern '.*\\.java' (case-sensitive)", regexRule.getDescription());
    }

    @Test
    void getPattern() {
        FilePatternRule rule = new FilePatternRule("*.java", false, false);

        assertEquals("*.java", rule.getPattern());
    }

    @Test
    void isRegex() {
        FilePatternRule globRule = new FilePatternRule("*.java", false, false);
        FilePatternRule regexRule = new FilePatternRule(".*\\.java", true, false);

        assertFalse(globRule.isRegex());
        assertTrue(regexRule.isRegex());
    }

    @Test
    void isCaseSensitive() {
        FilePatternRule insensitiveRule = new FilePatternRule("*.java", false, false);
        FilePatternRule sensitiveRule = new FilePatternRule("*.java", false, true);

        assertFalse(insensitiveRule.isCaseSensitive());
        assertTrue(sensitiveRule.isCaseSensitive());
    }
}
