package edu.psgv.healpointbackend;

import edu.psgv.healpointbackend.utilities.ConfigReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * Main application class for Healpoint Backend.
 * Configures and runs the Spring Boot application.
 *
 * @author Mahfuzur Rahman
 */
@SpringBootApplication
public class HealpointBackendApplication {
    public static String additionalAllowedOrigin = "";
    public static final Logger LOGGER = LogManager.getLogger();
    public static final ConfigReader CONFIG_READER = new ConfigReader("config.properties");

    public static void main(String[] args) {
        for (String arg : args) {
            if (arg.startsWith("--origin=")) {
                additionalAllowedOrigin = arg.substring("--origin=".length());
                LOGGER.info("Additional allowed origin set to: " + additionalAllowedOrigin);
            }
        }

        SpringApplication.run(HealpointBackendApplication.class, args);
    }

    /**
     * Configures Cross-Origin Resource Sharing (CORS) for the application.
     * Override the WebMvcConfigurer object with CORS configuration
     */
    @Configuration
    public static class WebCorsConfig implements WebMvcConfigurer {
        @Override
        public void addCorsMappings(CorsRegistry registry) {
            registry.addMapping("/api/**")
                    .allowedOrigins(CONFIG_READER.get("originUrlDev"), CONFIG_READER.get("originUrlQa"), CONFIG_READER.get("originUrlProd"), CONFIG_READER.get("originUrlProd2"), additionalAllowedOrigin)
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .allowedHeaders("*");
        }
    }
}
