package com.campusbridge.backend.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SecurityConfig CORS Profile Configuration Tests")
class SecurityConfigCorsTest {

    @Test
    @DisplayName("In dev profile, should automatically allow localhost 5173 and 5174")
    void shouldAllowDevOriginsInDevProfile() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("dev");

        SecurityConfig config = new SecurityConfig(null, null, env);
        ReflectionTestUtils.setField(config, "allowedOrigins", "");

        UrlBasedCorsConfigurationSource source = (UrlBasedCorsConfigurationSource) config.corsConfigurationSource();
        CorsConfiguration cors = source.getCorsConfiguration(new MockHttpServletRequest("GET", "/api/jobs"));

        assertThat(cors).isNotNull();
        assertThat(cors.getAllowedOrigins()).containsExactlyInAnyOrder("http://localhost:5173", "http://localhost:5174");
        assertThat(cors.getAllowCredentials()).isTrue();
    }

    @Test
    @DisplayName("In prod profile, should NOT allow dev origins and only allow configured origins")
    void shouldOnlyAllowConfiguredOriginsInProdProfile() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("prod");

        SecurityConfig config = new SecurityConfig(null, null, env);
        ReflectionTestUtils.setField(config, "allowedOrigins", "https://campusbridge.edu, https://admin.campusbridge.edu");

        UrlBasedCorsConfigurationSource source = (UrlBasedCorsConfigurationSource) config.corsConfigurationSource();
        CorsConfiguration cors = source.getCorsConfiguration(new MockHttpServletRequest("GET", "/api/jobs"));

        assertThat(cors).isNotNull();
        assertThat(cors.getAllowedOrigins()).containsExactly("https://campusbridge.edu", "https://admin.campusbridge.edu");
        assertThat(cors.getAllowedOrigins()).doesNotContain("http://localhost:5173", "http://localhost:5174");
        assertThat(cors.getAllowCredentials()).isTrue();
    }

    @Test
    @DisplayName("In prod profile with no configured origins, allowed origins list should be empty")
    void shouldHaveEmptyOriginsInProdWhenUnconfigured() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("prod");

        SecurityConfig config = new SecurityConfig(null, null, env);
        ReflectionTestUtils.setField(config, "allowedOrigins", "");

        UrlBasedCorsConfigurationSource source = (UrlBasedCorsConfigurationSource) config.corsConfigurationSource();
        CorsConfiguration cors = source.getCorsConfiguration(new MockHttpServletRequest("GET", "/api/jobs"));

        assertThat(cors).isNotNull();
        assertThat(cors.getAllowedOrigins()).isEmpty();
    }

    @Test
    @DisplayName("When both prod and dev profiles are active, prod takes precedence and does NOT allow localhost origins")
    void shouldNotAllowLocalhostOriginsWhenBothProdAndDevActive() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("prod", "dev");

        SecurityConfig config = new SecurityConfig(null, null, env);
        ReflectionTestUtils.setField(config, "allowedOrigins", "https://campusbridge.edu");

        UrlBasedCorsConfigurationSource source = (UrlBasedCorsConfigurationSource) config.corsConfigurationSource();
        CorsConfiguration cors = source.getCorsConfiguration(new MockHttpServletRequest("GET", "/api/jobs"));

        assertThat(cors).isNotNull();
        assertThat(cors.getAllowedOrigins()).containsExactly("https://campusbridge.edu");
        assertThat(cors.getAllowedOrigins()).doesNotContain("http://localhost:5173", "http://localhost:5174");
    }

    @Test
    @DisplayName("When both dev and prod profiles are active in reverse order, prod still takes precedence")
    void shouldNotAllowLocalhostOriginsWhenDevAndProdActiveInReverseOrder() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("dev", "prod");

        SecurityConfig config = new SecurityConfig(null, null, env);
        ReflectionTestUtils.setField(config, "allowedOrigins", "");

        UrlBasedCorsConfigurationSource source = (UrlBasedCorsConfigurationSource) config.corsConfigurationSource();
        CorsConfiguration cors = source.getCorsConfiguration(new MockHttpServletRequest("GET", "/api/jobs"));

        assertThat(cors).isNotNull();
        assertThat(cors.getAllowedOrigins()).doesNotContain("http://localhost:5173", "http://localhost:5174");
        assertThat(cors.getAllowedOrigins()).isEmpty();
    }

    @Test
    @DisplayName("When no active profiles are configured, defaults to allowing dev origins")
    void shouldAllowDevOriginsWhenNoProfilesConfigured() {
        MockEnvironment env = new MockEnvironment();

        SecurityConfig config = new SecurityConfig(null, null, env);
        ReflectionTestUtils.setField(config, "allowedOrigins", "");

        UrlBasedCorsConfigurationSource source = (UrlBasedCorsConfigurationSource) config.corsConfigurationSource();
        CorsConfiguration cors = source.getCorsConfiguration(new MockHttpServletRequest("GET", "/api/jobs"));

        assertThat(cors).isNotNull();
        assertThat(cors.getAllowedOrigins()).containsExactlyInAnyOrder("http://localhost:5173", "http://localhost:5174");
    }
}
