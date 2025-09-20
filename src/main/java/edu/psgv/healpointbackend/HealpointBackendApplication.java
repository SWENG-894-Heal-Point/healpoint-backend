package edu.psgv.healpointbackend;

import edu.psgv.healpointbackend.model.*;
import edu.psgv.healpointbackend.repository.*;

import edu.psgv.healpointbackend.utilities.ConfigReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class HealpointBackendApplication {

    public static final Logger LOGGER = LogManager.getLogger();
    public static final ConfigReader CONFIG_READER = new ConfigReader("src/main/resources/config.properties");

    public static void main(String[] args) {
        SpringApplication.run(HealpointBackendApplication.class, args);
    }

    /**
     * Configures Cross-Origin Resource Sharing (CORS) for the application.
     *
     * @return the WebMvcConfigurer object with CORS configuration
     */
    @Configuration
    public class WebCorsConfig implements WebMvcConfigurer {
        @Override
        public void addCorsMappings(CorsRegistry registry) {
            registry.addMapping("/api/**")
                    .allowedOrigins(CONFIG_READER.get("originUrlDev"), CONFIG_READER.get("originUrlProd"))
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .allowedHeaders("*");
        }
    }
}
