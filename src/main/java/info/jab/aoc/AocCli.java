package info.jab.aoc;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

import info.jab.aoc.client.AocClient;
import info.jab.aoc.command.CommandFactory;
import info.jab.aoc.command.InputCommand;
import info.jab.aoc.command.PendingCommand;
import info.jab.aoc.command.ProblemCommand;
import info.jab.aoc.command.StatsCommand;
import info.jab.aoc.command.SubmitCommand;
import info.jab.aoc.command.TestCommand;
import info.jab.aoc.util.AOCApiKeyResolver;

/**
 * Command Line Interface for Advent of Code authentication and interaction
 */
@Command(
    name = "aoc",
    mixinStandardHelpOptions = true,
    version = "AOC CLI 1.0",
    description = "Advent of Code CLI",
    subcommands = {
        TestCommand.class,
        InputCommand.class,
        SubmitCommand.class,
        StatsCommand.class,
        PendingCommand.class,
        ProblemCommand.class
    })
public class AocCli implements Callable<Integer> {

    @Option(names = {"-v", "--verbose"}, description = "Verbose output")
    public boolean verbose; // Public for access by subcommands via @ParentCommand

    private static final String AOC_BASE_URL = "https://adventofcode.com";

    AOCApiKeyResolver apiKeyResolver;
    AocClient aocClient; // Injected AocClient instance

    public AocCli() {
        AOCApiKeyResolver resolver = new AOCApiKeyResolver();
        String cookie = resolver.resolveApiKey(); // Precondition: must succeed, throws IllegalArgumentException if fails
        AocClient client = new AocClient(cookie, AOC_BASE_URL);
        this.apiKeyResolver = resolver;
        this.aocClient = client;
    }

    // Package-private constructor for testing with injected AocClient
    AocCli(AOCApiKeyResolver apiKeyResolver, AocClient aocClient) {
        this.apiKeyResolver = apiKeyResolver;
        this.aocClient = aocClient;
    }

    public static void main(String[] args) {
        AocCli aocCli = new AocCli();
        CommandLine cmd = createCommandLine(aocCli);
        int exitCode = cmd.execute(args);
        System.exit(exitCode);
    }

    /**
     * Creates a CommandLine instance with the appropriate factory for subcommands
     * Package-private for testing
     */
    static CommandLine createCommandLine(AocCli aocCli) {
        return new CommandLine(aocCli, new CommandFactory(aocCli.aocClient));
    }

    @Override
    public Integer call() throws Exception {
        System.out.println("Use --help to see available commands");
        return 0;
    }
}
