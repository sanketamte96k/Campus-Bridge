package com.campusbridge.backend.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JWT Service Startup & Validation Tests")
class JwtServiceTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("Should throw IllegalStateException when JWT_SECRET is blank or null")
    void shouldThrowWhenJwtSecretIsBlankOrNull(String blankSecret) {
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> new JwtService(blankSecret, 86400000L)
        );

        assertEquals("JWT_SECRET must be configured and must be at least 32 characters long.", ex.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "a",
            "short",
            "1234567890",
            "short-secret-under-32-chars!",
            "1234567890123456789012345678901" // 31 characters
    })
    @DisplayName("Should throw IllegalStateException when JWT_SECRET is shorter than 32 characters")
    void shouldThrowWhenJwtSecretIsShorterThan32Characters(String shortSecret) {
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> new JwtService(shortSecret, 86400000L)
        );

        assertEquals("JWT_SECRET must be configured and must be at least 32 characters long.", ex.getMessage());
    }

    @Test
    @DisplayName("Should initialize successfully when JWT_SECRET is 32 characters or longer")
    void shouldInitializeWhenJwtSecretIsValid() {
        String validSecret = "this-is-a-valid-test-secret-with-more-than-32-characters";
        assertDoesNotThrow(() -> new JwtService(validSecret, 86400000L));
    }
}
