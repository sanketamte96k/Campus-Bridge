package com.campusbridge.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads configuration values from the root git-ignored .env file into the Spring Environment
 * for normal application execution as well as test execution.
 *
 * Configuration Precedence:
 * 1. Java System Properties (-D...)
 * 2. Operating System Environment Variables (System.getenv())
 * 3. Root .env file properties (added after systemEnvironment)
 * 4. application.properties and profile properties (e.g., application-test.properties)
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DotEnvEnvironmentPostProcessor implements EnvironmentPostProcessor, ApplicationContextInitializer<ConfigurableApplicationContext>, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(DotEnvEnvironmentPostProcessor.class);
    public static final String PROPERTY_SOURCE_NAME = "dotEnvPropertySource";

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        loadDotEnvIntoEnvironment(environment);
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        loadDotEnvIntoEnvironment(applicationContext.getEnvironment());
    }

    private synchronized void loadDotEnvIntoEnvironment(ConfigurableEnvironment environment) {
        if (environment.getPropertySources().contains(PROPERTY_SOURCE_NAME)) {
            return;
        }

        Map<String, Object> dotEnvProps = loadDotEnvMap();
        if (!dotEnvProps.isEmpty()) {
            if (environment.getPropertySources().contains(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME)) {
                environment.getPropertySources().addAfter(
                        StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                        new MapPropertySource(PROPERTY_SOURCE_NAME, dotEnvProps)
                );
            } else {
                environment.getPropertySources().addLast(new MapPropertySource(PROPERTY_SOURCE_NAME, dotEnvProps));
            }
            logger.info("Loaded {} configuration keys from .env file into environment.", dotEnvProps.size());
        }
    }

    public static Map<String, Object> loadDotEnvMap() {
        Map<String, Object> map = new HashMap<>();
        Path dotEnvPath = findDotEnvPath();
        if (dotEnvPath == null || !Files.exists(dotEnvPath)) {
            return map;
        }

        try (BufferedReader reader = Files.newBufferedReader(dotEnvPath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                int eqIdx = trimmed.indexOf('=');
                if (eqIdx > 0) {
                    String key = trimmed.substring(0, eqIdx).trim();
                    String value = trimmed.substring(eqIdx + 1).trim();

                    // Strip surrounding single or double quotes if present
                    if ((value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) ||
                            (value.startsWith("'") && value.endsWith("'") && value.length() >= 2)) {
                        value = value.substring(1, value.length() - 1);
                    }

                    if (!key.isEmpty()) {
                        map.put(key, value);
                    }
                }
            }
        } catch (IOException e) {
            logger.warn("Failed to read .env file at {}: {}", dotEnvPath, e.getMessage());
        }
        return map;
    }

    public static Path findDotEnvPath() {
        Path[] candidates = new Path[]{
                Paths.get(".env"),
                Paths.get("..", ".env"),
                Paths.get("..", "..", ".env"),
                Paths.get(System.getProperty("user.dir", "."), ".env"),
                Paths.get(System.getProperty("user.dir", "."), "..", ".env")
        };

        for (Path candidate : candidates) {
            try {
                if (Files.exists(candidate) && Files.isRegularFile(candidate)) {
                    return candidate.toAbsolutePath().normalize();
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}
