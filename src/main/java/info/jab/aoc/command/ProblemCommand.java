package info.jab.aoc.command;

import info.jab.aoc.client.AocClient;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

/**
 * Command to get problem statement for specific day
 */
@Command(name = "problem", description = "Get problem statement for specific day")
public class ProblemCommand extends BaseCommand implements Callable<Integer> {

    public ProblemCommand(AocClient aocClient) {
        super(aocClient);
    }

    @Parameters(index = "0", description = "Year")
    private int year;

    @Parameters(index = "1", description = "Day")
    private int day;

    @Override
    public Integer call() throws Exception {
        try {
            printInfo(String.format("Getting problem statement for %d day %d...", year, day));

            String problemStatement = aocClient.getProblemStatement(year, day);
            System.out.println(problemStatement);

            printSuccess("Problem statement retrieved successfully");
            return 0;

        } catch (Exception e) {
            printError("Failed to get problem statement: " + e.getMessage());
            if (isVerbose()) {
                e.printStackTrace();
            }
            return 1;
        }
    }
}

