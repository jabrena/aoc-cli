package info.jab.aoc.command;

import info.jab.aoc.client.AocClient;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

/**
 * Command to download input for specific day
 */
@Command(name = "input", description = "Download input for specific day")
public class InputCommand extends BaseCommand implements Callable<Integer> {

    public InputCommand(AocClient aocClient) {
        super(aocClient);
    }

    @Parameters(index = "0", description = "Year")
    private int year;

    @Parameters(index = "1", description = "Day")
    private int day;

    @Override
    public Integer call() throws Exception {
        try {
            printInfo(String.format("Downloading input for %d day %d...", year, day));

            String input = aocClient.downloadInput(year, day);
            System.out.println(input);

            printSuccess("Input downloaded successfully");
            return 0;

        } catch (Exception e) {
            printError("Failed to download input: " + e.getMessage());
            if (isVerbose()) {
                e.printStackTrace();
            }
            return 1;
        }
    }
}

