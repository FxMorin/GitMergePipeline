package ca.fxco.gitmergepipeline.config;

import ca.fxco.gitmergepipeline.pipeline.Pipeline;
import ca.fxco.gitmergepipeline.rule.FilePatternRule;
import ca.fxco.gitmergepipeline.rule.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the ConfigurationLoader class which loads pipeline configurations.
 * Tests include loading default configurations when no file exists and
 * loading configurations from a JSON file.
 *
 * @author FX
 */
class ConfigurationLoaderTest {

    private ConfigurationLoader configurationLoader;

    @BeforeEach
    void setUp() {
        configurationLoader = new ConfigurationLoader();
    }

    @Test
    void loadDefaultConfigurationWhenNoFileExists() {
        PipelineConfiguration configuration = ConfigurationLoader.createDefaultConfiguration();

        assertNotNull(configuration);
        assertTrue(configuration.getRules().isEmpty());
        assertTrue(configuration.getPipelines().isEmpty());
    }

    @Test
    void loadConfigurationFromFile(@TempDir Path tempDir) throws IOException {
        // Create a test configuration file
        String configJson = """
                {
                   "filters": [
                     {
                       "type": "fileMode",
                       "mode": "regular"
                     },
                     {
                       "type": "mimeType",
                       "mimeType": "image/png"
                     },
                     {
                       "type": "not",
                       "filter": {
                         "type": "or",
                         "filters": [
                           {
                             "type": "filePattern",
                             "pattern": "*contains*"
                           },
                           {
                             "type": "filePattern",
                             "pattern": ".*me.*",
                             "isRegex": true,
                             "caseSensitive": true
                           }
                         ]
                       }
                     }
                   ],
                   "rules": {
                     "javaFiles": {
                       "type": "filePattern",
                       "pattern": "*.java",
                       "isRegex": false,
                       "caseSensitive": false
                     },
                     "xmlFiles": {
                       "type": "fileExtension",
                       "extensions": ["xml"],
                       "invert": false
                     },
                     "random1": {
                       "type": "composite",
                       "operation": "OR",
                       "isRegex": false,
                       "rules": [
                         {
                           "type": "mimeType",
                           "mimeType": "text/plain"
                         },
                         {
                           "type": "contentPattern",
                           "pattern": ".*something cool.*",
                           "caseSensitive": false,
                           "checkBase": false,
                           "checkCurrent": true,
                           "checkOther": true
                         }
                       ]
                     }
                   },
                   "pipelines": [
                     {
                       "type": "standard",
                       "name": "Java Files Pipeline",
                       "steps": [
                         {
                           "rule": {
                             "type": "filePattern",
                             "pattern": "*.java",
                             "isRegex": false,
                             "caseSensitive": false
                           },
                           "operation": "git-merge",
                           "parameters": ["recursive"]
                         }
                       ]
                     },
                     {
                       "type": "fallback",
                       "name": "XML Files Fallback Pipeline",
                       "steps": [
                         {
                           "rule": {
                             "type": "filePattern",
                             "pattern": "*.xml",
                             "isRegex": false,
                             "caseSensitive": false
                           },
                           "operation": "git-merge",
                           "parameters": ["recursive"]
                         },
                         {
                           "rule": {
                             "type": "filePattern",
                             "pattern": "*.xml",
                             "isRegex": false,
                             "caseSensitive": false
                           },
                           "operation": "git-merge",
                           "parameters": ["ours"]
                         },
                         {
                           "rule": {
                             "type": "filePattern",
                             "pattern": "*.xml",
                             "isRegex": false,
                             "caseSensitive": false
                           },
                           "operation": "take-current",
                           "parameters": []
                         }
                       ]
                     },
                     {
                       "type": "conditional",
                       "name": "Configuration Files Pipeline",
                       "branches": [
                         {
                           "rule": {
                             "type": "filePattern",
                             "pattern": "*.properties",
                             "isRegex": false,
                             "caseSensitive": false
                           },
                           "pipeline": {
                             "type": "standard",
                             "name": "Properties Files Pipeline",
                             "steps": [
                               {
                                 "operation": "git-merge",
                                 "parameters": ["recursive"]
                               }
                             ]
                           }
                         },
                         {
                           "rule": {
                             "type": "filePattern",
                             "pattern": "*.yml",
                             "isRegex": false,
                             "caseSensitive": false
                           },
                           "pipeline": {
                             "type": "fallback",
                             "name": "YAML Files Pipeline",
                             "steps": [
                               {
                                 "operation": "git-merge",
                                 "parameters": ["recursive"]
                               },
                               {
                                 "operation": "take-current",
                                 "parameters": []
                               }
                             ]
                           }
                         }
                       ],
                       "defaultPipeline": {
                         "type": "standard",
                         "name": "Default Config Pipeline",
                         "steps": [
                           {
                             "operation": "take-other",
                             "parameters": []
                           },
                           {
                             "operation": "keep-base"
                           }
                         ]
                       }
                     },
                     {
                       "type": "standard",
                       "name": "Default Pipeline",
                       "steps": [
                         {
                           "operation": "git-merge",
                           "parameters": ["recursive"]
                         }
                       ]
                     },
                     {
                       "type": "standard",
                       "name": "Default Pipeline",
                       "steps": [
                         {
                           "rule": {
                             "type": "filePattern",
                             "pattern": "*.json",
                             "isRegex": false,
                             "caseSensitive": false
                           },
                           "operation": "command-line-merge",
                           "parameters": [
                             "echo %CURRENT% > %OUTPUT%",
                             "30"
                           ]
                         }
                       ]
                     }
                   ]
                 }
                """;

        Path configFile = tempDir.resolve(".gitmergepipeline.json");
        Files.writeString(configFile, configJson);

        try {
            // Use reflection to set the environment variable
            //noinspection JavaReflectionMemberAccess
            java.lang.reflect.Field field = System.class.getDeclaredField("env");
            field.setAccessible(true);
            //noinspection unchecked
            java.util.Map<String, String> env = (java.util.Map<String, String>) field.get(null);
            java.util.Map<String, String> modifiableEnv = new java.util.HashMap<>(env);
            modifiableEnv.put("GITMERGEPIPELINE_CONFIG", configFile.toString());
            field.set(null, modifiableEnv);
        } catch (Exception e) {
            // If reflection fails, we'll use a different approach
            // Create a new configuration loader that directly loads from the file
            configurationLoader = new ConfigurationLoader() {
                @Override
                public PipelineConfiguration loadConfiguration() throws IOException {
                    return loadFromFile(configFile.toFile());
                }
            };
        }

        // Load the configuration
        PipelineConfiguration configuration = configurationLoader.loadConfiguration();

        // Verify the configuration
        assertNotNull(configuration);

        Map<String, Rule> rules = configuration.getRules();
        assertEquals(3, rules.size());
        assertTrue(rules.containsKey("javaFiles"));
        assertInstanceOf(FilePatternRule.class, rules.get("javaFiles"));

        FilePatternRule javaFilesRule = (FilePatternRule) rules.get("javaFiles");
        assertEquals("*.java", javaFilesRule.getPattern());
        assertFalse(javaFilesRule.isRegex());
        assertFalse(javaFilesRule.isCaseSensitive());

        List<Pipeline> pipelines = configuration.getPipelines();
        assertEquals(5, pipelines.size());
        assertEquals("Java Files Pipeline", pipelines.getFirst().getDescription().replace("Standard pipeline: ", ""));
    }
}
