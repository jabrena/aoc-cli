package info.jab.aoc.command;

import info.jab.aoc.client.AocClient;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

/**
 * Command to get personal stats for year
 */
@Command(name = "stats", description = "Get personal stats for year")
public class StatsCommand extends BaseCommand implements Callable<Integer> {

    public StatsCommand(AocClient aocClient) {
        super(aocClient);
    }

    @Parameters(index = "0", description = "Year")
    private int year;

    @Override
    public Integer call() throws Exception {
        try {
            printInfo(String.format("Getting personal stats for %d...", year));

            int completed = aocClient.getCompletedDaysCount(year);
            printSuccess(String.format("Year %d: %d days completed", year, completed));

            return 0;

        } catch (Exception e) {
            printError("Failed to get stats: " + e.getMessage());
            if (isVerbose()) {
                e.printStackTrace();
            }
            return 1;
        }
    }
}

