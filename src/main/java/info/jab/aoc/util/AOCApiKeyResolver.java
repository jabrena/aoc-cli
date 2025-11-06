package info.jab.aoc.util;

import io.github.cdimascio.dotenv.Dotenv;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Class for resolving API keys from various sources.
 * Uses dependency injection for testability.
 */
public class AOCApiKeyResolver {

    /**
     * The name of the environment variable for the AOC API key.
     */
    public static final String AOC_API_KEY = "AOC_API_KEY";

    // Injected dependencies for testability
    private final Function<String, String> envVarProvider;  // For System.getenv()
    private final Supplier<Optional<Dotenv>> dotenvSupplier; // For Dotenv creation

    /**
     * Default constructor - uses real implementations.
     */
    public AOCApiKeyResolver() {
        this(System::getenv, AOCApiKeyResolver::createDotenv);
    }

    /**
     * Constructor for dependency injection (package-private for testing).
     *
     * @param envVarProvider Function to get environment variables (replaces System.getenv)
     * @param dotenvSupplier Supplier to create Dotenv instance (replaces Dotenv.configure().load())
     */
    AOCApiKeyResolver(Function<String, String> envVarProvider, Supplier<Optional<Dotenv>> dotenvSupplier) {
        this.envVarProvider = envVarProvider;
        this.dotenvSupplier = dotenvSupplier;
    }

    /**
     * Resolves the API key from .env file or system environment using functional approach.
     * Priority: .env file > system environment variable
     *
     * @return The resolved API key
     * @throws IllegalArgumentException if no API key is found
     */
    public String resolveApiKey() {
        return resolveFromEnvFile()
            .or(() -> resolveFromSystemEnvironment())
            .orElseThrow(() -> new IllegalArgumentException(
                "API key not found. Please provide it via:\n" +
                "  1. .env file: " + AOC_API_KEY + "=YOUR_API_KEY\n" +
                "  2. Environment variable: export " + AOC_API_KEY + "=YOUR_API_KEY"
            ));
    }

    /**
     * Resolves API key from .env file.
     * Returns Optional.empty() if not found or if there's an error.
     */
    private Optional<String> resolveFromEnvFile() {
        try {
            Optional<Dotenv> dotenvOpt = dotenvSupplier.get();
            if (dotenvOpt.isPresent()) {
                Dotenv dotenv = dotenvOpt.get();
                String envApiKey = dotenv.get(AOC_API_KEY);
                if (envApiKey != null && !envApiKey.trim().isEmpty()) {
                    return Optional.of(envApiKey.trim());
                }
            }
            return Optional.empty();
        } catch (Exception e) {
            System.err.println("⚠️  Could not read .env file: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Resolves API key from system environment variable.
     * Returns Optional.empty() if not found.
     */
    private Optional<String> resolveFromSystemEnvironment() {
        return Optional.ofNullable(envVarProvider.apply(AOC_API_KEY))
            .filter(key -> !key.trim().isEmpty())
            .map(String::trim);
    }

    /**
     * Helper method to create Dotenv instance (extracted for testability).
     * Returns Optional.empty() if creation fails.
     */
    private static Optional<Dotenv> createDotenv() {
        try {
            Dotenv dotenv = Dotenv.configure()
                .directory(".")
                .ignoreIfMalformed()
                .ignoreIfMissing()
                .load();
            return Optional.of(dotenv);
        } catch (Exception e) {
            System.out.println("⚠️  Error loading .env : " + e.getMessage());
            return Optional.empty();
        }
    }
}
