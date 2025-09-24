package edu.psgv.healpointbackend.utilities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ConfigReaderTest {
    private ConfigReader configReader;
    private Path tempFile;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = Files.createTempFile("test", ".properties");
        Files.write(tempFile, "key1=value1\nkey2=value2".getBytes());

        configReader = new ConfigReader(tempFile.toString());
    }

    @Test
    void get_requestValidKey_returnsCorrectValue() {
        assertEquals("value1", configReader.get("key1"));
        assertEquals("value2", configReader.get("key2"));
    }

    @Test
    void get_requestInvalidKey_returnsNull() {
        assertNull(configReader.get("nonexistentKey"));
    }
}