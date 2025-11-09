package info.jab.aoc.command;

import info.jab.aoc.client.AocClient;
import info.jab.aoc.client.SubmissionResult;
import info.jab.aoc.client.SubmissionStatus;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

/**
 * Command to submit answer
 */
@Command(name = "submit", description = "Submit answer")
public class SubmitCommand extends BaseCommand implements Callable<Integer> {

    public SubmitCommand(AocClient aocClient) {
        super(aocClient);
    }

    @Parameters(index = "0", description = "Year")
    private int year;

    @Parameters(index = "1", description = "Day")
    private int day;

    @Parameters(index = "2", description = "Part (1 or 2)")
    private int part;

    @Parameters(index = "3", description = "Answer")
    private String answer;

    @Override
    public Integer call() throws Exception {
        try {
            printInfo(String.format("Submitting answer for %d day %d part %d: %s", year, day, part, answer));

            SubmissionResult result = aocClient.submitAnswer(year, day, part, answer);

            switch (result.getStatus()) {
                case CORRECT:
                    printSuccess("Correct answer!");
                    break;
                case WRONG:
                    printError("Wrong answer: " + result.getMessage());
                    if (isVerbose() && !result.getFullResponse().isEmpty()) {
                        System.out.println("\nFull response for debugging:");
                        System.out.println(result.getFullResponse());
                    }
                    break;
                case TOO_RECENT:
                    printWarning("Rate limited: " + result.getMessage());
                    if (isVerbose() && !result.getFullResponse().isEmpty()) {
                        System.out.println("\nFull response with timing details:");
                        System.out.println(result.getFullResponse());
                    }
                    break;
                case ALREADY_COMPLETE:
                    printWarning("This part is already completed");
                    break;
                case UNKNOWN:
                default:
                    printWarning("Unexpected response: " + result.getMessage());
                    if (isVerbose() && !result.getFullResponse().isEmpty()) {
                        System.out.println("\nFull response for debugging:");
                        System.out.println(result.getFullResponse());
                    }
                    break;
            }

            return result.getStatus() == SubmissionStatus.CORRECT ? 0 : 1;

        } catch (Exception e) {
            printError("Failed to submit answer: " + e.getMessage());
            if (isVerbose()) {
                e.printStackTrace();
            }
            return 1;
        }
    }
}

