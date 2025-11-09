package info.jab.aoc.command;

import info.jab.aoc.AocCli;
import info.jab.aoc.client.AocClient;
import picocli.CommandLine.IFactory;

/**
 * Factory to create command instances with injected AocClient
 */
public class CommandFactory implements IFactory {
    private final AocClient aocClient;

    public CommandFactory(AocClient aocClient) {
        this.aocClient = aocClient;
    }

    @Override
    public <T> T create(Class<T> cls) throws Exception {
        return switch (cls) {
            case Class<?> c when c == TestCommand.class ->
                cls.cast(new TestCommand(aocClient));
            case Class<?> c when c == InputCommand.class ->
                cls.cast(new InputCommand(aocClient));
            case Class<?> c when c == SubmitCommand.class ->
                cls.cast(new SubmitCommand(aocClient));
            case Class<?> c when c == StatsCommand.class ->
                cls.cast(new StatsCommand(aocClient));
            case Class<?> c when c == PendingCommand.class ->
                cls.cast(new PendingCommand(aocClient));
            case Class<?> c when c == ProblemCommand.class ->
                cls.cast(new ProblemCommand(aocClient));
            case Class<?> c when c == AocCli.class ->
                // This shouldn't be called, but handle it just in case
                cls.getDeclaredConstructor().newInstance();
            default ->
                // Default: try to use default constructor
                cls.getDeclaredConstructor().newInstance();
        };
    }
}

