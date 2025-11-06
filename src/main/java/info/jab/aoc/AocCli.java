package info.jab.aoc;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.List;
import java.util.concurrent.Callable;

import info.jab.aoc.client.AocClient;
import info.jab.aoc.client.SubmissionResult;
import info.jab.aoc.client.SubmissionStatus;
import info.jab.aoc.util.AOCApiKeyResolver;

/**
 * Command Line Interface for Advent of Code authentication and interaction
 */
@Command(
    name = "aoc",
    mixinStandardHelpOptions = true,
    version = "AOC CLI 1.0",
    description = "Advent of Code authentication and interaction tool")
public class AocCli implements Callable<Integer> {

    @Option(names = {"-v", "--verbose"}, description = "Verbose output")
    private boolean verbose;

    private static final String AOC_BASE_URL = "https://adventofcode.com";

    AOCApiKeyResolver apiKeyResolver;
    AocClient aocClient; // Injected AocClient instance
    String baseUrl; // For testing - if null, uses default AOC URL

    public AocCli() {
        AOCApiKeyResolver resolver = new AOCApiKeyResolver();
        String cookie = resolver.resolveApiKey(); // Precondition: must succeed, throws IllegalArgumentException if fails
        AocClient client = new AocClient(cookie, AOC_BASE_URL);
        this.apiKeyResolver = resolver;
        this.aocClient = client;
        this.baseUrl = AOC_BASE_URL;
    }

    // Package-private constructor for testing with injected AocClient
    AocCli(AOCApiKeyResolver apiKeyResolver, AocClient aocClient, String baseUrl) {
        this.apiKeyResolver = apiKeyResolver;
        this.aocClient = aocClient;
        this.baseUrl = baseUrl;
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new AocCli()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        System.out.println("Use --help to see available commands");
        return 0;
    }

    @Command(name = "test", description = "Test authentication")
    public int test() {
        try {
            printInfo("Testing authentication...");

            if (aocClient.testAuthentication()) {
                printSuccess("Authentication successful!");
                String username = aocClient.getUsername();
                if (!"Unknown".equals(username)) {
                    printInfo("Logged in as: " + username);
                }
                return 0;
            } else {
                printError("Authentication failed or session expired");
                return 1;
            }
        } catch (Exception e) {
            printError("Authentication test failed: " + e.getMessage());
            if (verbose) {
                e.printStackTrace();
            }
            return 1;
        }
    }

    @Command(name = "input", description = "Download input for specific day")
    public int input(@Parameters(index = "0", description = "Year") int year,
                     @Parameters(index = "1", description = "Day") int day) {
        try {
            printInfo(String.format("Downloading input for %d day %d...", year, day));

            String input = aocClient.downloadInput(year, day);
            System.out.println(input);

            printSuccess("Input downloaded successfully");
            return 0;

        } catch (Exception e) {
            printError("Failed to download input: " + e.getMessage());
            if (verbose) {
                e.printStackTrace();
            }
            return 1;
        }
    }

    @Command(name = "submit", description = "Submit answer")
    public int submit(@Parameters(index = "0", description = "Year") int year,
                      @Parameters(index = "1", description = "Day") int day,
                      @Parameters(index = "2", description = "Part (1 or 2)") int part,
                      @Parameters(index = "3", description = "Answer") String answer) {
        try {
            printInfo(String.format("Submitting answer for %d day %d part %d: %s", year, day, part, answer));

            SubmissionResult result = aocClient.submitAnswer(year, day, part, answer);

            switch (result.getStatus()) {
                case CORRECT:
                    printSuccess("Correct answer!");
                    break;
                case WRONG:
                    printError("Wrong answer: " + result.getMessage());
                    if (verbose && !result.getFullResponse().isEmpty()) {
                        System.out.println("\nFull response for debugging:");
                        System.out.println(result.getFullResponse());
                    }
                    break;
                case TOO_RECENT:
                    printWarning("Rate limited: " + result.getMessage());
                    if (verbose && !result.getFullResponse().isEmpty()) {
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
                    if (verbose && !result.getFullResponse().isEmpty()) {
                        System.out.println("\nFull response for debugging:");
                        System.out.println(result.getFullResponse());
                    }
                    break;
            }

            return result.getStatus() == SubmissionStatus.CORRECT ? 0 : 1;

        } catch (Exception e) {
            printError("Failed to submit answer: " + e.getMessage());
            if (verbose) {
                e.printStackTrace();
            }
            return 1;
        }
    }

    @Command(name = "stats", description = "Get personal stats for year")
    public int stats(@Parameters(index = "0", description = "Year") int year) {
        try {
            printInfo(String.format("Getting personal stats for %d...", year));

            int completed = aocClient.getCompletedDaysCount(year);
            printSuccess(String.format("Year %d: %d days completed", year, completed));

            return 0;

        } catch (Exception e) {
            printError("Failed to get stats: " + e.getMessage());
            if (verbose) {
                e.printStackTrace();
            }
            return 1;
        }
    }

    @Command(name = "pending", description = "List pending years (no args) or pending parts for specific year (with year arg)")
    public int pending(@Parameters(index = "0", description = "Year (optional)", arity = "0..1") Integer year) {
        try {
            if (year == null) {
                // No year provided - show pending years
                printInfo("Getting years with pending work...");

                List<Integer> pendingYears = aocClient.getPendingYears();

                if (pendingYears.isEmpty()) {
                    printSuccess("All years completed! 🎉");
                } else {
                    // Print as space-separated years
                    System.out.println(pendingYears.stream()
                            .map(String::valueOf)
                            .reduce((a, b) -> a + " " + b)
                            .orElse(""));
                }
            } else {
                // Year provided - show pending parts for that year
                printInfo(String.format("Getting pending parts for %d...", year));

                List<String> pendingParts = aocClient.getPendingParts(year);

                if (pendingParts.isEmpty()) {
                    printSuccess("All parts completed for " + year + "!");
                } else {
                    // Print as space-separated day_part format
                    System.out.println(String.join(" ", pendingParts));
                }
            }

            return 0;

        } catch (Exception e) {
            printError("Failed to get pending information: " + e.getMessage());
            if (verbose) {
                e.printStackTrace();
            }
            return 1;
        }
    }

    @Command(name = "problem", description = "Get problem statement for specific day")
    public int problem(@Parameters(index = "0", description = "Year") int year,
                       @Parameters(index = "1", description = "Day") int day) {
        try {
            printInfo(String.format("Getting problem statement for %d day %d...", year, day));

            String problemStatement = aocClient.getProblemStatement(year, day);
            System.out.println(problemStatement);

            printSuccess("Problem statement retrieved successfully");
            return 0;

        } catch (Exception e) {
            printError("Failed to get problem statement: " + e.getMessage());
            if (verbose) {
                e.printStackTrace();
            }
            return 1;
        }
    }

    // Color output methods
    private static final String RED = "\033[0;31m";
    private static final String GREEN = "\033[0;32m";
    private static final String YELLOW = "\033[1;33m";
    private static final String NC = "\033[0m"; // No Color

    private void printSuccess(String message) {
        System.err.println(GREEN + "✅ " + message + NC);
    }

    private void printError(String message) {
        System.err.println(RED + "❌ " + message + NC);
    }

    private void printWarning(String message) {
        System.err.println(YELLOW + "⚠️  " + message + NC);
    }

    private void printInfo(String message) {
        System.err.println("ℹ️  " + message);
    }
}
