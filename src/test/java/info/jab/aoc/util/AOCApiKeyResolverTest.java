package info.jab.aoc.util;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AOCApiKeyResolver
 * Uses Mockito with dependency injection for clean, testable code
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AOCApiKeyResolver Tests")
class AOCApiKeyResolverTest {

    @Mock
    private Function<String, String> mockEnvVarProvider;

    @Mock
    private Supplier<Optional<Dotenv>> mockDotenvSupplier;

    @Mock
    private Dotenv mockDotenv;

    private AOCApiKeyResolver resolver;

    @BeforeEach
    void setUp() {
        // Default resolver uses real implementations
        resolver = new AOCApiKeyResolver();
    }

    @Test
    @DisplayName("Should resolve API key from .env file")
    void should_resolveApiKey_fromEnvFile() {
        // Given - Mock Dotenv to return test key, env var provider to return null
        when(mockDotenv.get(AOCApiKeyResolver.AOC_API_KEY)).thenReturn("test_api_key_from_file");
        when(mockDotenvSupplier.get()).thenReturn(Optional.of(mockDotenv));
        when(mockEnvVarProvider.apply(AOCApiKeyResolver.AOC_API_KEY)).thenReturn(null);

        resolver = new AOCApiKeyResolver(mockEnvVarProvider, mockDotenvSupplier);

        // When
        String apiKey = resolver.resolveApiKey();

        // Then
        assertThat(apiKey).isEqualTo("test_api_key_from_file");
    }

    @Test
    @DisplayName("Should resolve API key from system environment when .env not available")
    void should_resolveApiKey_fromSystemEnvironment() {
        // Given - Mock Dotenv to return empty, env var provider to return test key
        when(mockDotenvSupplier.get()).thenReturn(Optional.empty());
        when(mockEnvVarProvider.apply(AOCApiKeyResolver.AOC_API_KEY)).thenReturn("test_key_from_env");

        resolver = new AOCApiKeyResolver(mockEnvVarProvider, mockDotenvSupplier);

        // When
        String apiKey = resolver.resolveApiKey();

        // Then
        assertThat(apiKey).isEqualTo("test_key_from_env");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when no API key is found")
    void should_throwIllegalArgumentException_when_noApiKeyFound() {
        // Given - Mock both Dotenv and env var provider to return empty/null
        when(mockDotenvSupplier.get()).thenReturn(Optional.empty());
        when(mockEnvVarProvider.apply(AOCApiKeyResolver.AOC_API_KEY)).thenReturn(null);

        resolver = new AOCApiKeyResolver(mockEnvVarProvider, mockDotenvSupplier);

        // When & Then
        assertThatThrownBy(() -> resolver.resolveApiKey())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("API key not found")
                .hasMessageContaining(".env file")
                .hasMessageContaining("Environment variable");
    }

    @Test
    @DisplayName("Should trim whitespace from API key")
    void should_trimWhitespace_fromApiKey() {
        // Given - Mock Dotenv to return key with whitespace
        when(mockDotenv.get(AOCApiKeyResolver.AOC_API_KEY)).thenReturn("  test_key_with_spaces  ");
        when(mockDotenvSupplier.get()).thenReturn(Optional.of(mockDotenv));
        when(mockEnvVarProvider.apply(AOCApiKeyResolver.AOC_API_KEY)).thenReturn(null);

        resolver = new AOCApiKeyResolver(mockEnvVarProvider, mockDotenvSupplier);

        // When
        String apiKey = resolver.resolveApiKey();

        // Then - Should be trimmed
        assertThat(apiKey).isEqualTo("test_key_with_spaces");
        assertThat(apiKey).doesNotStartWith(" ");
        assertThat(apiKey).doesNotEndWith(" ");
    }

    @Test
    @DisplayName("Should handle empty API key in .env file")
    void should_handleEmptyApiKey_inEnvFile() {
        // Given - Mock Dotenv to return empty string, env var provider to return null
        when(mockDotenv.get(AOCApiKeyResolver.AOC_API_KEY)).thenReturn("");
        when(mockDotenvSupplier.get()).thenReturn(Optional.of(mockDotenv));
        when(mockEnvVarProvider.apply(AOCApiKeyResolver.AOC_API_KEY)).thenReturn(null);

        resolver = new AOCApiKeyResolver(mockEnvVarProvider, mockDotenvSupplier);

        // When & Then - Should throw exception for empty key
        assertThatThrownBy(() -> resolver.resolveApiKey())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("API key not found");
    }

    @Test
    @DisplayName("Should handle malformed .env file gracefully")
    void should_handleMalformedEnvFile_gracefully() {
        // Given - Mock Dotenv to return valid key (malformed parts are ignored by Dotenv)
        when(mockDotenv.get(AOCApiKeyResolver.AOC_API_KEY)).thenReturn("valid_key");
        when(mockDotenvSupplier.get()).thenReturn(Optional.of(mockDotenv));
        when(mockEnvVarProvider.apply(AOCApiKeyResolver.AOC_API_KEY)).thenReturn(null);

        resolver = new AOCApiKeyResolver(mockEnvVarProvider, mockDotenvSupplier);

        // When
        String apiKey = resolver.resolveApiKey();

        // Then
        assertThat(apiKey).isEqualTo("valid_key");
    }

    @Test
    @DisplayName("Should handle system environment variable with whitespace")
    void should_handleSystemEnvironmentVariable_withWhitespace() {
        // Given - Mock Dotenv to return empty, env var provider to return key with whitespace
        when(mockDotenvSupplier.get()).thenReturn(Optional.empty());
        when(mockEnvVarProvider.apply(AOCApiKeyResolver.AOC_API_KEY)).thenReturn("  env_key_with_spaces  ");

        resolver = new AOCApiKeyResolver(mockEnvVarProvider, mockDotenvSupplier);

        // When
        String apiKey = resolver.resolveApiKey();

        // Then - Should be trimmed
        assertThat(apiKey).isEqualTo("env_key_with_spaces");
        assertThat(apiKey).doesNotStartWith(" ");
        assertThat(apiKey).doesNotEndWith(" ");
    }

    @Test
    @DisplayName("Should handle resolveFromEnvFile exception path")
    void should_handleResolveFromEnvFile_exceptionPath() {
        // Given - Mock Dotenv supplier to throw exception, env var provider to return null
        when(mockDotenvSupplier.get()).thenThrow(new RuntimeException("File read error"));
        when(mockEnvVarProvider.apply(AOCApiKeyResolver.AOC_API_KEY)).thenReturn(null);

        resolver = new AOCApiKeyResolver(mockEnvVarProvider, mockDotenvSupplier);

        // When & Then - Should throw IllegalArgumentException when no key found
        assertThatThrownBy(() -> resolver.resolveApiKey())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("API key not found");
    }

    @Test
    @DisplayName("Should handle Dotenv returning null for key")
    void should_handleDotenvReturningNullForKey() {
        // Given - Mock Dotenv to return null for key, env var provider to return null
        when(mockDotenv.get(AOCApiKeyResolver.AOC_API_KEY)).thenReturn(null);
        when(mockDotenvSupplier.get()).thenReturn(Optional.of(mockDotenv));
        when(mockEnvVarProvider.apply(AOCApiKeyResolver.AOC_API_KEY)).thenReturn(null);

        resolver = new AOCApiKeyResolver(mockEnvVarProvider, mockDotenvSupplier);

        // When & Then
        assertThatThrownBy(() -> resolver.resolveApiKey())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("API key not found");
    }

    @Test
    @DisplayName("Should prioritize .env file over system environment")
    void should_prioritizeEnvFileOverSystemEnvironment() {
        // Given - Both sources have keys, .env should be used
        when(mockDotenv.get(AOCApiKeyResolver.AOC_API_KEY)).thenReturn("key_from_env_file");
        when(mockDotenvSupplier.get()).thenReturn(Optional.of(mockDotenv));
        when(mockEnvVarProvider.apply(AOCApiKeyResolver.AOC_API_KEY)).thenReturn("key_from_system");

        resolver = new AOCApiKeyResolver(mockEnvVarProvider, mockDotenvSupplier);

        // When
        String apiKey = resolver.resolveApiKey();

        // Then - Should use .env file value
        assertThat(apiKey).isEqualTo("key_from_env_file");
    }
}

