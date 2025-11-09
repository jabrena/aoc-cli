package info.jab.aoc.client;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for SubmissionResult
 */
@DisplayName("SubmissionResult Tests")
class SubmissionResultTest {

    @Test
    @DisplayName("Should have backward compatibility constants for submission results")
    void should_haveBackwardCompatibilityConstants_when_staticFieldsExist() {
        // Given & When
        SubmissionResult correctResult = SubmissionResult.CORRECT;
        SubmissionResult alreadyCompleteResult = SubmissionResult.ALREADY_COMPLETE;

        // Then
        assertThat(correctResult)
            .isNotNull()
            .extracting(SubmissionResult::getStatus)
            .isEqualTo(SubmissionStatus.CORRECT);

        assertThat(alreadyCompleteResult)
            .isNotNull()
            .extracting(SubmissionResult::getStatus)
            .isEqualTo(SubmissionStatus.ALREADY_COMPLETE);
    }

    @Test
    @DisplayName("Should create submission result with all provided fields")
    void should_createSubmissionResult_when_allFieldsProvided() {
        // Given
        SubmissionStatus expectedStatus = SubmissionStatus.WRONG;
        String expectedMessage = "Test message";
        String expectedFullResponse = "Full response";

        // When
        SubmissionResult result = new SubmissionResult(
            expectedStatus,
            expectedMessage,
            expectedFullResponse
        );

        // Then
        assertThat(result)
            .extracting(
                SubmissionResult::getStatus,
                SubmissionResult::getMessage,
                SubmissionResult::getFullResponse
            )
            .containsExactly(
                expectedStatus,
                expectedMessage,
                expectedFullResponse
            );
    }

    @Test
    @DisplayName("Should return correct toString format with message")
    void should_returnCorrectToStringFormat_withMessage() {
        // Given
        SubmissionResult result = new SubmissionResult(
            SubmissionStatus.WRONG,
            "That's not the right answer",
            "Full HTML response"
        );

        // When
        String toString = result.toString();

        // Then
        assertThat(toString).isEqualTo("WRONG: That's not the right answer");
    }

    @Test
    @DisplayName("Should return correct toString format without message")
    void should_returnCorrectToStringFormat_withoutMessage() {
        // Given - CORRECT constant has a message, so we create one without
        SubmissionResult result = new SubmissionResult(SubmissionStatus.CORRECT, "", "");

        // When
        String toString = result.toString();

        // Then
        assertThat(toString).isEqualTo("CORRECT");
    }

    @Test
    @DisplayName("Should return correct toString format for CORRECT constant")
    void should_returnCorrectToStringFormat_forCorrectConstant() {
        // Given
        SubmissionResult result = SubmissionResult.CORRECT;

        // When
        String toString = result.toString();

        // Then - CORRECT constant has a message
        assertThat(toString).isEqualTo("CORRECT: That's the right answer!");
    }

    @Test
    @DisplayName("Should return correct toString format with empty message")
    void should_returnCorrectToStringFormat_withEmptyMessage() {
        // Given
        SubmissionResult result = new SubmissionResult(
            SubmissionStatus.ALREADY_COMPLETE,
            "",
            ""
        );

        // When
        String toString = result.toString();

        // Then
        assertThat(toString).isEqualTo("ALREADY_COMPLETE");
    }

    @Test
    @DisplayName("Should handle all submission statuses in toString")
    void should_handleAllSubmissionStatuses_inToString() {
        // Test all status types
        for (SubmissionStatus status : SubmissionStatus.values()) {
            // Given
            SubmissionResult result = new SubmissionResult(status, "Test message", "");

            // When
            String toString = result.toString();

            // Then
            assertThat(toString).contains(status.toString());
        }
    }
}

