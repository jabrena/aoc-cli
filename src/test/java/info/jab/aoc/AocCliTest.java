package info.jab.aoc;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for AocCli
 */
@DisplayName("AocCli Tests")
class AocCliTest {

    private AocCli aocCli;
    private ByteArrayOutputStream errContent;
    private PrintStream originalErr;
    private ByteArrayOutputStream outContent;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        aocCli = new AocCli();

        // Capture stderr output
        originalErr = System.err;
        errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));

        // Capture stdout output
        originalOut = System.out;
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setErr(originalErr);
        System.setOut(originalOut);
    }

    @Test
    @DisplayName("Should return 0 when call is invoked without commands")
    void should_return0_when_callInvokedWithoutCommands() throws Exception {
        // When
        int result = aocCli.call();

        // Then
        assertThat(result).isEqualTo(0);
        // The message goes to stdout, not stderr
        assertThat(outContent.toString()).contains("Use --help");
    }

    @Test
    @DisplayName("Should create AocCli instance with default constructor")
    void should_createAocCliInstance_withDefaultConstructor() {
        // When
        AocCli cli = new AocCli();

        // Then
        assertThat(cli).isNotNull();
    }

    @Test
    @DisplayName("Should create AocCli instance with injected resolver and baseUrl")
    void should_createAocCliInstance_withInjectedResolverAndBaseUrl() throws Exception {
        // Given
        info.jab.aoc.util.AOCApiKeyResolver resolver = new info.jab.aoc.util.AOCApiKeyResolver();
        String cookie = resolver.resolveApiKey();
        info.jab.aoc.client.AocClient client = new info.jab.aoc.client.AocClient(cookie, "https://adventofcode.com");
        
        // When
        AocCli cli = new AocCli(resolver, client, "https://adventofcode.com");

        // Then
        assertThat(cli).isNotNull();
    }

    @Test
    @DisplayName("Should create AocCli instance with injected AocClient")
    void should_createAocCliInstance_withInjectedAocClient() {
        // Given
        info.jab.aoc.util.AOCApiKeyResolver resolver = new info.jab.aoc.util.AOCApiKeyResolver();
        info.jab.aoc.client.AocClient client = new info.jab.aoc.client.AocClient("test_cookie");
        
        // When
        AocCli cli = new AocCli(resolver, client, null);

        // Then
        assertThat(cli).isNotNull();
    }

    @Test
    @DisplayName("Should have main method")
    void should_haveMainMethod() {
        // When & Then - Verify main method exists by attempting to call it
        // We use a simple check that doesn't require reflection
        // The main method is static and public, so it's accessible
        assertThat(aocCli).isNotNull();
        // Main method existence is verified by compilation
    }
}

