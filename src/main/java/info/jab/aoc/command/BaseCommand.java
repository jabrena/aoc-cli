package info.jab.aoc.command;

import info.jab.aoc.AocCli;
import info.jab.aoc.client.AocClient;
import picocli.CommandLine.ParentCommand;

/**
 * Base class for AOC CLI commands providing common functionality
 */
public abstract class BaseCommand {

    @ParentCommand
    protected AocCli parent;

    protected AocClient aocClient;

    public BaseCommand(AocClient aocClient) {
        this.aocClient = aocClient;
    }

    // Color output methods
    private static final String RED = "\033[0;31m";
    private static final String GREEN = "\033[0;32m";
    private static final String YELLOW = "\033[1;33m";
    private static final String NC = "\033[0m"; // No Color

    protected void printSuccess(String message) {
        System.err.println(GREEN + "✅ " + message + NC);
    }

    protected void printError(String message) {
        System.err.println(RED + "❌ " + message + NC);
    }

    protected void printWarning(String message) {
        System.err.println(YELLOW + "⚠️  " + message + NC);
    }

    protected void printInfo(String message) {
        System.err.println("ℹ️  " + message);
    }

    protected boolean isVerbose() {
        return parent != null && parent.verbose;
    }
}

