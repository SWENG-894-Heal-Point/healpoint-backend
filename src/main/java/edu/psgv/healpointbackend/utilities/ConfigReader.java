package edu.psgv.healpointbackend.utilities;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static edu.psgv.healpointbackend.HealpointBackendApplication.LOGGER;

/**
 * ConfigReader is a utility class that reads configuration from a file
 * and provides the values to the application.
 *
 * @author Mahfuzur Rahman
 */
public class ConfigReader {
    private final Properties properties;

    /**
     * This constructor takes a file path and reads the configuration from the file.
     *
     * @param filePath the path to the configuration file
     */
    public ConfigReader(String filePath) {
        LOGGER.debug(">>ConfigReader()");
        LOGGER.info("Reading configuration from file: {}", filePath);
        this.properties = new Properties();
        try (FileInputStream input = new FileInputStream(filePath)) {
            properties.load(input);
            LOGGER.info("Configuration read successfully");
        } catch (IOException e) {
            LOGGER.error("Error reading configuration file: {}", e.getMessage());
        }
    }

    /**
     * This method takes a key and returns the value from the configuration.
     *
     * @param key the key to retrieve the value
     * @return the value for the key
     */
    public String get(String key) {
        LOGGER.debug(">>ConfigReader.get()");
        String value = properties.getProperty(key);
        LOGGER.debug("Retrieved value for key {}: {}", key, value);
        return value;
    }
}