package info.jab.aoc;

import info.jab.aoc.client.AocClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit test for AocClient
 */
@DisplayName("AocClient Tests")
class AocClientTest {

    @Test
    @DisplayName("Should create client instance when valid cookie is provided")
    void should_createClient_when_validCookieProvided() {
        // Given
        String validCookie = "dummy_cookie";

        // When
        AocClient client = new AocClient(validCookie);

        // Then
        assertThat(client).isNotNull();
    }

    @ParameterizedTest(name = "Should throw IllegalArgumentException when cookie is {0}")
    @DisplayName("Should throw IllegalArgumentException when cookie is invalid")
    @CsvSource({
        "null, ''",
        "empty, ''",
        "blank, '   '"
    })
    void should_throwException_when_cookieIsInvalid(String description, String cookie) {
        // Given
        String invalidCookie = "null".equals(description) ? null : cookie;

        // When & Then
        assertThatThrownBy(() -> new AocClient(invalidCookie))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Session cookie cannot be null or empty");
    }
}
