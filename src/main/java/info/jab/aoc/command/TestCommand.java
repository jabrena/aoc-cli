package info.jab.aoc.command;

import info.jab.aoc.client.AocClient;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

/**
 * Command to test authentication
 */
@Command(name = "test", description = "Test authentication")
public class TestCommand extends BaseCommand implements Callable<Integer> {

    public TestCommand(AocClient aocClient) {
        super(aocClient);
    }

    @Override
    public Integer call() throws Exception {
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
            if (isVerbose()) {
                e.printStackTrace();
            }
            return 1;
        }
    }
}

