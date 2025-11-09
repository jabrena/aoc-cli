package info.jab.aoc.command;

import info.jab.aoc.client.AocClient;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Command to list pending years (no args) or pending parts for specific year (with year arg)
 */
@Command(name = "pending", description = "List pending years (no args) or pending parts for specific year (with year arg)")
public class PendingCommand extends BaseCommand implements Callable<Integer> {

    public PendingCommand(AocClient aocClient) {
        super(aocClient);
    }

    @Parameters(index = "0", description = "Year (optional)", arity = "0..1")
    private Integer year;

    @Override
    public Integer call() throws Exception {
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
            if (isVerbose()) {
                e.printStackTrace();
            }
            return 1;
        }
    }
}

