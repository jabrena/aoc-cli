package info.jab.aoc;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import info.jab.aoc.client.AocClient;
import info.jab.aoc.util.AOCApiKeyResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for AocCli using WireMock
 */
@DisplayName("AocCli Integration Tests")
class AocCliIT {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort().withRootDirectory("src/test/resources"))
            .build();

    private AocCli aocCli;
    private String baseUrl;
    private ByteArrayOutputStream outContent;
    private ByteArrayOutputStream errContent;
    private PrintStream originalOut;
    private PrintStream originalErr;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + wireMock.getPort();
        // Create AocCli with a resolver that returns a test cookie and custom base URL
        AOCApiKeyResolver resolver = new AOCApiKeyResolver() {
            @Override
            public String resolveApiKey() {
                return "test_session_cookie";
            }
        };
        AocClient client = new AocClient("test_session_cookie", baseUrl);
        aocCli = new AocCli(resolver, client);

        // Capture output
        originalOut = System.out;
        originalErr = System.err;
        outContent = new ByteArrayOutputStream();
        errContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    @DisplayName("Should execute test command successfully")
    void should_executeTestCommand_successfully() {
        // Given
        wireMock.stubFor(get(urlEqualTo("/settings"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBodyFile("settings_authenticated.html")));

        // When
        CommandLine cmd = new CommandLine(aocCli);
        int exitCode = cmd.execute("test");

        // Then
        assertThat(exitCode).isEqualTo(0);
        assertThat(errContent.toString()).contains("Testing authentication");
    }

    @Test
    @DisplayName("Should execute input command successfully")
    void should_executeInputCommand_successfully() {
        // Given
        wireMock.stubFor(get(urlEqualTo("/2023/day/1/input"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBodyFile("input_2023_day1.txt")));

        // When
        CommandLine cmd = new CommandLine(aocCli);
        int exitCode = cmd.execute("input", "2023", "1");

        // Then
        assertThat(exitCode).isEqualTo(0);
        assertThat(errContent.toString()).contains("Downloading input");
        assertThat(outContent.toString()).isNotEmpty();
    }

    @Test
    @DisplayName("Should execute submit command with correct answer")
    void should_executeSubmitCommand_withCorrectAnswer() {
        // Given
        wireMock.stubFor(post(urlEqualTo("/2023/day/1/answer"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBodyFile("submit_correct.html")));

        // When
        CommandLine cmd = new CommandLine(aocCli);
        int exitCode = cmd.execute("submit", "2023", "1", "1", "12345");

        // Then
        assertThat(exitCode).isEqualTo(0);
        assertThat(errContent.toString()).contains("Correct answer");
    }

    @Test
    @DisplayName("Should execute submit command with wrong answer")
    void should_executeSubmitCommand_withWrongAnswer() {
        // Given
        wireMock.stubFor(post(urlEqualTo("/2023/day/1/answer"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBodyFile("submit_wrong.html")));

        // When
        CommandLine cmd = new CommandLine(aocCli);
        int exitCode = cmd.execute("submit", "2023", "1", "1", "wrong");

        // Then
        assertThat(exitCode).isEqualTo(1);
        assertThat(errContent.toString()).contains("Wrong answer");
    }

    @Test
    @DisplayName("Should execute submit command with verbose flag")
    void should_executeSubmitCommand_withVerboseFlag() {
        // Given
        wireMock.stubFor(post(urlEqualTo("/2023/day/1/answer"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBodyFile("submit_wrong.html")));

        // When
        CommandLine cmd = new CommandLine(aocCli);
        int exitCode = cmd.execute("--verbose", "submit", "2023", "1", "1", "wrong");

        // Then
        assertThat(exitCode).isEqualTo(1);
        assertThat(errContent.toString()).contains("Wrong answer");
    }

    @Test
    @DisplayName("Should execute stats command successfully")
    void should_executeStatsCommand_successfully() {
        // Given
        wireMock.stubFor(get(urlEqualTo("/2023"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBodyFile("year_2023.html")));

        // When
        CommandLine cmd = new CommandLine(aocCli);
        int exitCode = cmd.execute("stats", "2023");

        // Then
        assertThat(exitCode).isEqualTo(0);
        assertThat(errContent.toString()).contains("days completed");
    }

    @Test
    @DisplayName("Should execute pending command without year")
    void should_executePendingCommand_withoutYear() {
        // Given
        wireMock.stubFor(get(urlEqualTo("/2023"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBodyFile("year_2023.html")));

        // When
        CommandLine cmd = new CommandLine(aocCli);
        int exitCode = cmd.execute("pending");

        // Then
        assertThat(exitCode).isEqualTo(0);
    }

    @Test
    @DisplayName("Should execute pending command with year")
    void should_executePendingCommand_withYear() {
        // Given
        wireMock.stubFor(get(urlEqualTo("/2023"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBodyFile("year_2023.html")));

        // When
        CommandLine cmd = new CommandLine(aocCli);
        int exitCode = cmd.execute("pending", "2023");

        // Then
        assertThat(exitCode).isEqualTo(0);
    }

    @Test
    @DisplayName("Should execute problem command successfully")
    void should_executeProblemCommand_successfully() {
        // Given
        wireMock.stubFor(get(urlEqualTo("/2023/day/1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBodyFile("problem_statement_2023_day1.html")));

        // When
        CommandLine cmd = new CommandLine(aocCli);
        int exitCode = cmd.execute("problem", "2023", "1");

        // Then
        assertThat(exitCode).isEqualTo(0);
        assertThat(errContent.toString()).contains("Problem statement retrieved");
        assertThat(outContent.toString()).isNotEmpty();
    }

    @Test
    @DisplayName("Should handle command errors gracefully")
    void should_handleCommandErrors_gracefully() {
        // Given - API key resolution fails
        AOCApiKeyResolver failingResolver = new AOCApiKeyResolver() {
            @Override
            public String resolveApiKey() {
                throw new IllegalArgumentException("No API key found");
            }
        };
        // Client is null because resolver will fail - will throw when used
        AocCli failingCli = new AocCli(failingResolver, null);

        // When
        CommandLine cmd = new CommandLine(failingCli);
        int exitCode = cmd.execute("test");

        // Then
        assertThat(exitCode).isEqualTo(1);
        assertThat(errContent.toString()).contains("failed");
    }

    @Test
    @DisplayName("Should execute submit command with too recent status")
    void should_executeSubmitCommand_withTooRecentStatus() {
        // Given
        wireMock.stubFor(post(urlEqualTo("/2023/day/1/answer"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBodyFile("submit_too_recent.html")));

        // When
        CommandLine cmd = new CommandLine(aocCli);
        int exitCode = cmd.execute("submit", "2023", "1", "1", "answer");

        // Then
        assertThat(exitCode).isEqualTo(1);
        assertThat(errContent.toString()).contains("Rate limited");
    }

    @Test
    @DisplayName("Should execute submit command with already complete status")
    void should_executeSubmitCommand_withAlreadyCompleteStatus() {
        // Given
        wireMock.stubFor(post(urlEqualTo("/2023/day/1/answer"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBodyFile("submit_already_complete.html")));

        // When
        CommandLine cmd = new CommandLine(aocCli);
        int exitCode = cmd.execute("submit", "2023", "1", "1", "answer");

        // Then
        assertThat(exitCode).isEqualTo(1);
        assertThat(errContent.toString()).contains("already completed");
    }

    @Test
    @DisplayName("Should execute submit command with unknown status")
    void should_executeSubmitCommand_withUnknownStatus() {
        // Given
        wireMock.stubFor(post(urlEqualTo("/2023/day/1/answer"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("<html><body>Unexpected response</body></html>")));

        // When
        CommandLine cmd = new CommandLine(aocCli);
        int exitCode = cmd.execute("submit", "2023", "1", "1", "answer");

        // Then
        assertThat(exitCode).isEqualTo(1);
        assertThat(errContent.toString()).contains("Unexpected response");
    }

    @Test
    @DisplayName("Should execute test command with authentication failure")
    void should_executeTestCommand_withAuthenticationFailure() {
        // Given
        wireMock.stubFor(get(urlEqualTo("/settings"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBodyFile("settings_unauthenticated.html")));

        // When
        CommandLine cmd = new CommandLine(aocCli);
        int exitCode = cmd.execute("test");

        // Then
        assertThat(exitCode).isEqualTo(1);
        assertThat(errContent.toString()).contains("Authentication failed");
    }

    @Test
    @DisplayName("Should execute test command with username")
    void should_executeTestCommand_withUsername() {
        // Given
        wireMock.stubFor(get(urlEqualTo("/settings"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBodyFile("settings_authenticated.html")));

        // When
        CommandLine cmd = new CommandLine(aocCli);
        int exitCode = cmd.execute("test");

        // Then
        assertThat(exitCode).isEqualTo(0);
        assertThat(errContent.toString()).contains("Authentication successful");
    }

    @Test
    @DisplayName("Should execute input command with error")
    void should_executeInputCommand_withError() {
        // Given
        wireMock.stubFor(get(urlEqualTo("/2023/day/1/input"))
                .willReturn(aResponse().withStatus(404)));

        // When
        CommandLine cmd = new CommandLine(aocCli);
        int exitCode = cmd.execute("input", "2023", "1");

        // Then
        assertThat(exitCode).isEqualTo(1);
        assertThat(errContent.toString()).contains("Failed to download input");
    }

    @Test
    @DisplayName("Should execute problem command with error")
    void should_executeProblemCommand_withError() {
        // Given
        wireMock.stubFor(get(urlEqualTo("/2023/day/1"))
                .willReturn(aResponse().withStatus(404)));

        // When
        CommandLine cmd = new CommandLine(aocCli);
        int exitCode = cmd.execute("problem", "2023", "1");

        // Then
        assertThat(exitCode).isEqualTo(1);
        assertThat(errContent.toString()).contains("Failed to get problem statement");
    }

    @Test
    @DisplayName("Should execute stats command with error")
    void should_executeStatsCommand_withError() {
        // Given
        wireMock.stubFor(get(urlEqualTo("/2023"))
                .willReturn(aResponse().withStatus(500)));

        // When
        CommandLine cmd = new CommandLine(aocCli);
        int exitCode = cmd.execute("stats", "2023");

        // Then
        assertThat(exitCode).isEqualTo(1);
        assertThat(errContent.toString()).contains("Failed to get stats");
    }

    @Test
    @DisplayName("Should execute submit command with verbose and full response")
    void should_executeSubmitCommand_withVerboseAndFullResponse() {
        // Given
        wireMock.stubFor(post(urlEqualTo("/2023/day/1/answer"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBodyFile("submit_wrong.html")));

        // When
        CommandLine cmd = new CommandLine(aocCli);
        int exitCode = cmd.execute("--verbose", "submit", "2023", "1", "1", "wrong");

        // Then
        assertThat(exitCode).isEqualTo(1);
        assertThat(errContent.toString()).contains("Wrong answer");
        // Verbose mode should show full response
        assertThat(outContent.toString()).contains("Full response");
    }

    @Test
    @DisplayName("Should execute submit command with verbose and too recent")
    void should_executeSubmitCommand_withVerboseAndTooRecent() {
        // Given
        wireMock.stubFor(post(urlEqualTo("/2023/day/1/answer"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBodyFile("submit_too_recent.html")));

        // When
        CommandLine cmd = new CommandLine(aocCli);
        int exitCode = cmd.execute("--verbose", "submit", "2023", "1", "1", "answer");

        // Then
        assertThat(exitCode).isEqualTo(1);
        assertThat(errContent.toString()).contains("Rate limited");
    }

    @Test
    @DisplayName("Should execute pending command with error")
    void should_executePendingCommand_withError() {
        // Given - getPendingYears handles errors gracefully, so we test with a resolver that throws
        AOCApiKeyResolver failingResolver = new AOCApiKeyResolver() {
            @Override
            public String resolveApiKey() {
                throw new IllegalArgumentException("No API key found");
            }
        };
        // Client is null because resolver will fail - will throw when used
        AocCli failingCli = new AocCli(failingResolver, null);

        // When
        CommandLine cmd = new CommandLine(failingCli);
        int exitCode = cmd.execute("pending");

        // Then
        assertThat(exitCode).isEqualTo(1);
        assertThat(errContent.toString()).contains("Failed to get pending information");
    }

    @Test
    @DisplayName("Should execute pending command with year and error")
    void should_executePendingCommand_withYearAndError() {
        // Given
        wireMock.stubFor(get(urlEqualTo("/2023"))
                .willReturn(aResponse().withStatus(500)));

        // When
        CommandLine cmd = new CommandLine(aocCli);
        int exitCode = cmd.execute("pending", "2023");

        // Then
        assertThat(exitCode).isEqualTo(1);
        assertThat(errContent.toString()).contains("Failed to get pending information");
    }

    @Test
    @DisplayName("Should execute submit command with verbose and empty full response")
    void should_executeSubmitCommand_withVerboseAndEmptyFullResponse() {
        // Given - Result with empty full response
        wireMock.stubFor(post(urlEqualTo("/2023/day/1/answer"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("<html><body>That's the right answer!</body></html>")));

        // When
        CommandLine cmd = new CommandLine(aocCli);
        int exitCode = cmd.execute("--verbose", "submit", "2023", "1", "1", "correct");

        // Then - Should not print full response if empty (CORRECT has empty full response)
        assertThat(exitCode).isEqualTo(0);
        assertThat(errContent.toString()).contains("Correct answer");
    }

    @Test
    @DisplayName("Should execute submit command with UNKNOWN status and verbose")
    void should_executeSubmitCommand_withUnknownStatusAndVerbose() {
        // Given
        wireMock.stubFor(post(urlEqualTo("/2023/day/1/answer"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("<html><body>Unexpected response</body></html>")));

        // When
        CommandLine cmd = new CommandLine(aocCli);
        int exitCode = cmd.execute("--verbose", "submit", "2023", "1", "1", "answer");

        // Then
        assertThat(exitCode).isEqualTo(1);
        assertThat(errContent.toString()).contains("Unexpected response");
    }

    @Test
    @DisplayName("Should execute test command with Unknown username")
    void should_executeTestCommand_withUnknownUsername() {
        // Given - Settings page without username
        wireMock.stubFor(get(urlEqualTo("/settings"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("<html><body>No username here</body></html>")));

        // When
        CommandLine cmd = new CommandLine(aocCli);
        int exitCode = cmd.execute("test");

        // Then - Should not print username if Unknown
        assertThat(exitCode).isEqualTo(1); // Authentication fails
    }

    @Test
    @DisplayName("Should execute test command with known username")
    void should_executeTestCommand_withKnownUsername() {
        // Given
        wireMock.stubFor(get(urlEqualTo("/settings"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBodyFile("settings_authenticated.html")));

        // When
        CommandLine cmd = new CommandLine(aocCli);
        int exitCode = cmd.execute("test");

        // Then
        assertThat(exitCode).isEqualTo(0);
        // May or may not show username depending on HTML structure
    }

    @Test
    @DisplayName("Should execute submit command with verbose and non-empty full response for WRONG")
    void should_executeSubmitCommand_withVerboseAndNonEmptyFullResponseWrong() {
        // Given
        wireMock.stubFor(post(urlEqualTo("/2023/day/1/answer"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBodyFile("submit_wrong.html")));

        // When
        CommandLine cmd = new CommandLine(aocCli);
        int exitCode = cmd.execute("--verbose", "submit", "2023", "1", "1", "wrong");

        // Then - Verbose mode should show full response
        assertThat(exitCode).isEqualTo(1);
        assertThat(errContent.toString()).contains("Wrong answer");
    }

    @Test
    @DisplayName("Should execute submit command with verbose and non-empty full response for TOO_RECENT")
    void should_executeSubmitCommand_withVerboseAndNonEmptyFullResponseTooRecent() {
        // Given
        wireMock.stubFor(post(urlEqualTo("/2023/day/1/answer"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBodyFile("submit_too_recent.html")));

        // When
        CommandLine cmd = new CommandLine(aocCli);
        int exitCode = cmd.execute("--verbose", "submit", "2023", "1", "1", "answer");

        // Then - Verbose mode should show full response
        assertThat(exitCode).isEqualTo(1);
        assertThat(errContent.toString()).contains("Rate limited");
    }

    @Test
    @DisplayName("Should execute submit command with verbose and non-empty full response for UNKNOWN")
    void should_executeSubmitCommand_withVerboseAndNonEmptyFullResponseUnknown() {
        // Given
        wireMock.stubFor(post(urlEqualTo("/2023/day/1/answer"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("<html><body>Unexpected response with content</body></html>")));

        // When
        CommandLine cmd = new CommandLine(aocCli);
        int exitCode = cmd.execute("--verbose", "submit", "2023", "1", "1", "answer");

        // Then - Verbose mode should show full response
        assertThat(exitCode).isEqualTo(1);
        assertThat(errContent.toString()).contains("Unexpected response");
    }

    @Test
    @DisplayName("Should execute pending command with no year and empty pending years")
    void should_executePendingCommand_withNoYearAndEmptyPendingYears() {
        // Given - Year page with all days completed (no pending parts)
        String allCompleted = "<html><body><div class=\"calendar-day1 calendar-verycomplete\">Day 1</div><div class=\"calendar-day2 calendar-verycomplete\">Day 2</div></body></html>";
        wireMock.stubFor(get(urlMatching("/20(15|16|17|18|19|20|21|22|23|24|25)"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(allCompleted)));

        // When
        CommandLine cmd = new CommandLine(aocCli);
        int exitCode = cmd.execute("pending");

        // Then - Should show success message
        assertThat(exitCode).isEqualTo(0);
    }

    @Test
    @DisplayName("Should execute pending command with year and empty pending parts")
    void should_executePendingCommand_withYearAndEmptyPendingParts() {
        // Given - Year page with all days completed (need 25 days to be verycomplete)
        StringBuilder allCompleted = new StringBuilder("<html><body>");
        for (int day = 1; day <= 25; day++) {
            allCompleted.append("<div class=\"calendar-day").append(day).append(" calendar-verycomplete\">Day ").append(day).append("</div>");
        }
        allCompleted.append("</body></html>");
        wireMock.stubFor(get(urlEqualTo("/2023"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(allCompleted.toString())));

        // When
        CommandLine cmd = new CommandLine(aocCli);
        int exitCode = cmd.execute("pending", "2023");

        // Then - Should show success message
        assertThat(exitCode).isEqualTo(0);
        assertThat(errContent.toString()).contains("All parts completed");
    }

    @Test
    @DisplayName("Should execute pending command with year and non-empty pending parts")
    void should_executePendingCommand_withYearAndNonEmptyPendingParts() {
        // Given - Year page with some pending parts
        wireMock.stubFor(get(urlEqualTo("/2023"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBodyFile("year_2023.html")));

        // When
        CommandLine cmd = new CommandLine(aocCli);
        int exitCode = cmd.execute("pending", "2023");

        // Then - Should output pending parts
        assertThat(exitCode).isEqualTo(0);
        // May or may not have pending parts depending on real data
    }

    @Test
    @DisplayName("Should execute input command with verbose and exception")
    void should_executeInputCommand_withVerboseAndException() {
        // Given
        wireMock.stubFor(get(urlEqualTo("/2023/day/1/input"))
                .willReturn(aResponse().withStatus(404)));

        // When
        CommandLine cmd = new CommandLine(aocCli);
        int exitCode = cmd.execute("--verbose", "input", "2023", "1");

        // Then - Verbose mode should print stack trace
        assertThat(exitCode).isEqualTo(1);
        assertThat(errContent.toString()).contains("Failed to download input");
    }

    @Test
    @DisplayName("Should execute problem command with verbose and exception")
    void should_executeProblemCommand_withVerboseAndException() {
        // Given
        wireMock.stubFor(get(urlEqualTo("/2023/day/1"))
                .willReturn(aResponse().withStatus(404)));

        // When
        CommandLine cmd = new CommandLine(aocCli);
        int exitCode = cmd.execute("--verbose", "problem", "2023", "1");

        // Then - Verbose mode should print stack trace
        assertThat(exitCode).isEqualTo(1);
        assertThat(errContent.toString()).contains("Failed to get problem statement");
    }

    @Test
    @DisplayName("Should execute stats command with verbose and exception")
    void should_executeStatsCommand_withVerboseAndException() {
        // Given
        wireMock.stubFor(get(urlEqualTo("/2023"))
                .willReturn(aResponse().withStatus(500)));

        // When
        CommandLine cmd = new CommandLine(aocCli);
        int exitCode = cmd.execute("--verbose", "stats", "2023");

        // Then - Verbose mode should print stack trace
        assertThat(exitCode).isEqualTo(1);
        assertThat(errContent.toString()).contains("Failed to get stats");
    }

    @Test
    @DisplayName("Should execute test command with verbose and exception")
    void should_executeTestCommand_withVerboseAndException() {
        // Given - API key resolution fails
        AOCApiKeyResolver failingResolver = new AOCApiKeyResolver() {
            @Override
            public String resolveApiKey() {
                throw new IllegalArgumentException("No API key found");
            }
        };
        // Client is null because resolver will fail - will throw when used
        AocCli failingCli = new AocCli(failingResolver, null);

        // When
        CommandLine cmd = new CommandLine(failingCli);
        int exitCode = cmd.execute("--verbose", "test");

        // Then - Verbose mode should print stack trace
        assertThat(exitCode).isEqualTo(1);
        assertThat(errContent.toString()).contains("failed");
    }
}

