package info.jab.aoc.client;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for SubmissionStatus
 */
@DisplayName("SubmissionStatus Tests")
class SubmissionStatusTest {

    @Test
    @DisplayName("Should have all expected submission statuses available")
    void should_haveAllExpectedStatuses_when_enumIsDefined() {
        // Given & When
        SubmissionStatus[] statuses = SubmissionStatus.values();

        // Then
        assertThat(statuses)
            .hasSize(5)
            .contains(
                SubmissionStatus.CORRECT,
                SubmissionStatus.WRONG,
                SubmissionStatus.TOO_RECENT,
                SubmissionStatus.ALREADY_COMPLETE,
                SubmissionStatus.UNKNOWN
            );
    }
}

