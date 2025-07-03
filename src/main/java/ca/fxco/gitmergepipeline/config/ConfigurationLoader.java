package ca.fxco.gitmergepipeline.config;

import ca.fxco.gitmergepipeline.pipeline.*;
import ca.fxco.gitmergepipeline.rule.Rule;
import ca.fxco.gitmergepipeline.rule.RuleRegistry;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Loads the configuration for the GitMergePipeline from various sources.
 * Configuration can be loaded from:
 * 1. A file specified by the GITMERGEPIPELINE_CONFIG environment variable
 * 2. A .gitmergepipeline.json file in the user's home directory
 * 3. A .gitmergepipeline.json file in the current working directory
 *
 * @author FX
 */
public class ConfigurationLoader {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationLoader.class);

    private static final String ENV_CONFIG_PATH = "GITMERGEPIPELINE_CONFIG";
    private static final String CONFIG_FILENAME = ".gitmergepipeline.json";

    private final ObjectMapper objectMapper;

    public ConfigurationLoader() {
        this.objectMapper = new ObjectMapper();

        // Add custom pipeline types to the object mapper
        Map<String, Class<? extends Pipeline>> pipelines = new PipelineRegistry().getPipelines();
        for (Map.Entry<String, Class<? extends Pipeline>> entry : pipelines.entrySet()) {
            objectMapper.registerSubtypes(new NamedType(entry.getValue(), entry.getKey()));
        }

        // Add custom rule types to the object mapper
        Map<String, Class<? extends Rule>> rules = new RuleRegistry().getRules();
        for (Map.Entry<String, Class<? extends Rule>> entry : rules.entrySet()) {
            objectMapper.registerSubtypes(new NamedType(entry.getValue(), entry.getKey()));
        }

        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Loads the configuration from the available sources.
     * 
     * @return The loaded configuration, or a default configuration if none is found
     * @throws IOException If there's an error reading the configuration file
     */
    public PipelineConfiguration loadConfiguration() throws IOException {
        try {
            // Try to load from environment variable
            String configPath = System.getenv(ENV_CONFIG_PATH);
            if (configPath != null && !configPath.isEmpty()) {
                File configFile = new File(configPath);
                if (configFile.exists() && configFile.isFile()) {
                    logger.info("Loading configuration from environment variable: {}", configPath);
                    return loadFromFile(configFile);
                }
            }

            // Try to load from current working directory
            Path workingDirConfig = Paths.get(CONFIG_FILENAME);
            if (Files.exists(workingDirConfig) && Files.isRegularFile(workingDirConfig)) {
                logger.info("Loading configuration from working directory: {}", workingDirConfig);
                return loadFromFile(workingDirConfig.toFile());
            }

            // Try to load from user's home directory
            Path homeConfig = Paths.get(System.getProperty("user.home"), CONFIG_FILENAME);
            if (Files.exists(homeConfig) && Files.isRegularFile(homeConfig)) {
                logger.info("Loading configuration from home directory: {}", homeConfig);
                return loadFromFile(homeConfig.toFile());
            }

            // No configuration found, return default
            logger.info("No configuration found, using default configuration");
            return createDefaultConfiguration();
        } catch (Exception e) {
            logger.warn("Error loading configuration, using default configuration", e);
            return createDefaultConfiguration();
        }
    }

    /// VisibleForTesting
    protected PipelineConfiguration loadFromFile(File file) throws IOException {
        try {
            return objectMapper.readValue(file, PipelineConfiguration.class);
        } catch (IOException e) {
            logger.error("Error loading configuration from file: {}", file, e);
            throw new IOException("Failed to load configuration from " + file, e);
        }
    }

    /// VisibleForTesting
    public static PipelineConfiguration createDefaultConfiguration() {
        return new PipelineConfiguration();
    }
}
