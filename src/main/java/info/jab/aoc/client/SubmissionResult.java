package info.jab.aoc.client;

/**
 * Result of a submission to Advent of Code
 */
public class SubmissionResult {
    private final SubmissionStatus status;
    private final String message;
    private final String fullResponse;

    public SubmissionResult(SubmissionStatus status, String message, String fullResponse) {
        this.status = status;
        this.message = message;
        this.fullResponse = fullResponse;
    }

    public SubmissionStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getFullResponse() {
        return fullResponse;
    }

    // Backward compatibility methods
    public static final SubmissionResult CORRECT = new SubmissionResult(SubmissionStatus.CORRECT, "That's the right answer!", "");
    public static final SubmissionResult ALREADY_COMPLETE = new SubmissionResult(SubmissionStatus.ALREADY_COMPLETE, "Already complete", "");

    @Override
    public String toString() {
        return status + (message.isEmpty() ? "" : ": " + message);
    }
}

