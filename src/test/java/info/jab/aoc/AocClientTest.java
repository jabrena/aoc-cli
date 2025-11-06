package info.jab.aoc;

import info.jab.aoc.client.AocClient;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for AocClient
 */
public class AocClientTest {

    @Test
    public void testClientCreation() {
        // Test that we can create a client with a dummy cookie
        AocClient client = new AocClient("dummy_cookie");
        assertNotNull(client);
    }

    @Test
    public void testSubmissionResultClass() {
        // Test that all submission result statuses are available
        assertEquals(5, AocClient.SubmissionResult.Status.values().length);
        assertNotNull(AocClient.SubmissionResult.Status.CORRECT);
        assertNotNull(AocClient.SubmissionResult.Status.WRONG);
        assertNotNull(AocClient.SubmissionResult.Status.TOO_RECENT);
        assertNotNull(AocClient.SubmissionResult.Status.ALREADY_COMPLETE);
        assertNotNull(AocClient.SubmissionResult.Status.UNKNOWN);

        // Test backward compatibility constants
        assertNotNull(AocClient.SubmissionResult.CORRECT);
        assertNotNull(AocClient.SubmissionResult.ALREADY_COMPLETE);

        // Test creating custom results
        AocClient.SubmissionResult wrongResult = new AocClient.SubmissionResult(
            AocClient.SubmissionResult.Status.WRONG,
            "Test message",
            "Full response"
        );
        assertEquals(AocClient.SubmissionResult.Status.WRONG, wrongResult.getStatus());
        assertEquals("Test message", wrongResult.getMessage());
        assertEquals("Full response", wrongResult.getFullResponse());
    }

    @Test
    public void testPendingPartsLogic() {
        // Test the logic for generating pending parts in day_part format
        String htmlWithMixedCompletion =
            "<div class=\"calendar-day1 calendar-complete\">" +
            "<div class=\"calendar-day2 calendar-verycomplete\">" +
            "<div class=\"calendar-day3\">" +
            "<div class=\"calendar-day4 calendar-complete\">" +
            "<div class=\"calendar-day5 calendar-verycomplete\">";

        // Test pattern for any completion (used in getCompletedDaysCount)
        java.util.regex.Pattern anyCompletePattern = java.util.regex.Pattern.compile("calendar-(verycomplete|complete)");
        java.util.regex.Matcher anyCompleteMatcher = anyCompletePattern.matcher(htmlWithMixedCompletion);
        int anyCompleteCount = 0;
        while (anyCompleteMatcher.find()) {
            anyCompleteCount++;
        }
        assertEquals(4, anyCompleteCount, "Should find 4 completed days (any status)");

        // Test pattern for very complete only (used in getPendingParts)
        java.util.regex.Pattern veryCompletePattern = java.util.regex.Pattern.compile("calendar-day(\\d+) calendar-[^\"]*verycomplete");
        java.util.regex.Matcher veryCompleteMatcher = veryCompletePattern.matcher(htmlWithMixedCompletion);
        java.util.Set<Integer> veryCompleteDays = new java.util.TreeSet<>();
        while (veryCompleteMatcher.find()) {
            veryCompleteDays.add(Integer.parseInt(veryCompleteMatcher.group(1)));
        }
        assertEquals(2, veryCompleteDays.size(), "Should find 2 very complete days");
        assertTrue(veryCompleteDays.contains(2), "Day 2 should be very complete");
        assertTrue(veryCompleteDays.contains(5), "Day 5 should be very complete");

        // Test pattern for any completion with day extraction (used in getPendingParts)
        java.util.regex.Pattern anyCompleteWithDayPattern = java.util.regex.Pattern.compile("calendar-day(\\d+) calendar-[^\"]*complete");
        java.util.regex.Matcher anyCompleteWithDayMatcher = anyCompleteWithDayPattern.matcher(htmlWithMixedCompletion);
        java.util.Set<Integer> anyCompleteDays = new java.util.TreeSet<>();
        while (anyCompleteWithDayMatcher.find()) {
            anyCompleteDays.add(Integer.parseInt(anyCompleteWithDayMatcher.group(1)));
        }
        assertEquals(4, anyCompleteDays.size(), "Should find 4 completed days with day numbers");
        assertTrue(anyCompleteDays.contains(1), "Day 1 should be complete");
        assertTrue(anyCompleteDays.contains(2), "Day 2 should be complete");
        assertTrue(anyCompleteDays.contains(4), "Day 4 should be complete");
        assertTrue(anyCompleteDays.contains(5), "Day 5 should be complete");

        // Verify logic for pending parts
        java.util.List<String> expectedPendingParts = new java.util.ArrayList<>();
        for (int day = 1; day <= 5; day++) {
            if (veryCompleteDays.contains(day)) {
                // Both parts completed - nothing pending (days 2, 5)
                continue;
            } else if (anyCompleteDays.contains(day)) {
                // Only Part 1 completed - need Part 2 (days 1, 4)
                expectedPendingParts.add(day + "_2");
            } else {
                // No progress - need Part 1 (day 3)
                expectedPendingParts.add(day + "_1");
            }
        }

        assertEquals(3, expectedPendingParts.size(), "Should have 3 pending parts");
        assertTrue(expectedPendingParts.contains("1_2"), "Day 1 should need Part 2");
        assertTrue(expectedPendingParts.contains("3_1"), "Day 3 should need Part 1");
        assertTrue(expectedPendingParts.contains("4_2"), "Day 4 should need Part 2");
    }
}
