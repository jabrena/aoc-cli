package info.jab.aoc.util;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class to record real AOC API responses for use in integration tests.
 * Run with: mvn exec:java -Pe2e -Dexec.mainClass="info.jab.aoc.util.AocResponseRecorder"
 */
public class AocResponseRecorder {

    private static final String AOC_BASE_URL = "https://adventofcode.com";
    private static final String USER_AGENT = "Mozilla/5.0 (compatible; AoC-Script/1.0)";
    private static final String TARGET_DIR = "src/test/resources/__files";

    private final String sessionCookie;
    private final HttpClient httpClient;
    private final Path targetDir;

    public AocResponseRecorder() throws IOException {
        AOCApiKeyResolver resolver = new AOCApiKeyResolver();
        this.sessionCookie = resolver.resolveApiKey();
        this.httpClient = HttpClient.newBuilder().build();
        this.targetDir = Paths.get(TARGET_DIR);

        // Ensure target directory exists
        Files.createDirectories(targetDir);

        System.out.println("AOC Response Recorder initialized");
        System.out.println("Target directory: " + targetDir.toAbsolutePath());
    }

    public static void main(String[] args) {
        try {
            AocResponseRecorder recorder = new AocResponseRecorder();
            recorder.recordAll();
            System.out.println("\n✅ Recording completed successfully!");
        } catch (Exception e) {
            System.err.println("❌ Error during recording: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void recordAll() throws IOException {
        System.out.println("\n📝 Recording AOC API responses...\n");

        // Record settings page (authenticated)
        recordSettings();

        // Record input for a specific day
        recordInput(2023, 1);

        // Record problem statement
        recordProblemStatement(2023, 1);

        // Record year page
        recordYearPage(2023);

        // Record submission responses (these are static HTML responses)
        recordSubmissionResponses();
    }

    private void recordSettings() throws IOException {
        System.out.println("Recording settings page...");
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(AOC_BASE_URL + "/settings"))
                    .header("Cookie", "session=" + sessionCookie)
                    .header("User-Agent", USER_AGENT)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200 && response.body() != null) {
                writeFile("settings_authenticated.html", response.body());
                System.out.println("  ✓ Saved settings_authenticated.html");
            } else {
                System.err.println("  ✗ Failed to get settings (HTTP " + response.statusCode() + ")");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("  ✗ Failed to record settings: Request interrupted");
            throw new IOException("Failed to record settings", e);
        } catch (Exception e) {
            System.err.println("  ✗ Failed to record settings: " + e.getMessage());
            throw new IOException("Failed to record settings", e);
        }
    }

    private void recordInput(int year, int day) {
        System.out.println("Recording input for " + year + " day " + day + "...");
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format("%s/%d/day/%d/input", AOC_BASE_URL, year, day)))
                    .header("Cookie", "session=" + sessionCookie)
                    .header("User-Agent", USER_AGENT)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200 && response.body() != null) {
                writeFile(String.format("input_%d_day%d.txt", year, day), response.body());
                System.out.println("  ✓ Saved input_" + year + "_day" + day + ".txt");
            } else {
                System.err.println("  ✗ Failed to get input (HTTP " + response.statusCode() + ")");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("  ✗ Failed to record input: Request interrupted");
        } catch (Exception e) {
            System.err.println("  ✗ Failed to record input: " + e.getMessage());
            // Don't throw - input might not be available
        }
    }

    private void recordProblemStatement(int year, int day) {
        System.out.println("Recording problem statement for " + year + " day " + day + "...");
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format("%s/%d/day/%d", AOC_BASE_URL, year, day)))
                    .header("Cookie", "session=" + sessionCookie)
                    .header("User-Agent", USER_AGENT)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200 && response.body() != null) {
                writeFile(String.format("problem_statement_%d_day%d.html", year, day), response.body());
                System.out.println("  ✓ Saved problem_statement_" + year + "_day" + day + ".html");
            } else {
                System.err.println("  ✗ Failed to get problem statement (HTTP " + response.statusCode() + ")");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("  ✗ Failed to record problem statement: Request interrupted");
        } catch (Exception e) {
            System.err.println("  ✗ Failed to record problem statement: " + e.getMessage());
            // Don't throw - problem might not be available
        }
    }

    private void recordYearPage(int year) {
        System.out.println("Recording year page for " + year + "...");
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format("%s/%d", AOC_BASE_URL, year)))
                    .header("Cookie", "session=" + sessionCookie)
                    .header("User-Agent", USER_AGENT)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200 && response.body() != null) {
                writeFile(String.format("year_%d.html", year), response.body());
                System.out.println("  ✓ Saved year_" + year + ".html");
            } else {
                System.err.println("  ✗ Failed to get year page (HTTP " + response.statusCode() + ")");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("  ✗ Failed to record year page: Request interrupted");
        } catch (Exception e) {
            System.err.println("  ✗ Failed to record year page: " + e.getMessage());
            // Don't throw - year might not be available
        }
    }

    private void recordSubmissionResponses() {
        System.out.println("Recording submission response templates...");
        System.out.println("  ⚠️  Note: Submission responses cannot be recorded without actually submitting answers.");
        System.out.println("  ⚠️  Using realistic templates based on AOC response patterns.");

        // These are static HTML templates that AOC returns
        // We can't record them without actually submitting (which would be wasteful)
        // So we use realistic templates
        writeFile("submit_correct.html", generateSubmitCorrectHtml());
        System.out.println("  ✓ Saved submit_correct.html");

        writeFile("submit_wrong.html", generateSubmitWrongHtml());
        System.out.println("  ✓ Saved submit_wrong.html");

        writeFile("submit_too_recent.html", generateSubmitTooRecentHtml());
        System.out.println("  ✓ Saved submit_too_recent.html");

        writeFile("submit_already_complete.html", generateSubmitAlreadyCompleteHtml());
        System.out.println("  ✓ Saved submit_already_complete.html");

        writeFile("settings_unauthenticated.html", generateSettingsUnauthenticatedHtml());
        System.out.println("  ✓ Saved settings_unauthenticated.html");
    }

    private void writeFile(String filename, String content) {
        try {
            Path filePath = targetDir.resolve(filename);
            Files.writeString(filePath, content);
        } catch (IOException e) {
            System.err.println("  ✗ Failed to write " + filename + ": " + e.getMessage());
        }
    }


    private String generateSubmitCorrectHtml() {
        return """
<!DOCTYPE html>
<html>
<body>
    <main>
        <article>
            <p>That's the right answer! You are one gold star closer to collecting enough stars for your <em>free</em> hot chocolate.</p>
        </article>
    </main>
</body>
</html>
""";
    }

    private String generateSubmitWrongHtml() {
        return """
<!DOCTYPE html>
<html>
<body>
    <main>
        <article>
            <p>That's not the right answer. If you're stuck, make sure you're using the full input data; there are also some general tips on the <a href="/about">about page</a>, or you can ask for hints on the <a href="https://www.reddit.com/r/adventofcode" target="_blank">subreddit</a>. Please wait one minute before trying again.</p>
        </article>
    </main>
</body>
</html>
""";
    }

    private String generateSubmitTooRecentHtml() {
        return """
<!DOCTYPE html>
<html>
<body>
    <main>
        <article>
            <p>You gave an answer too recently; you have to wait 5m 23s before submitting another answer.</p>
        </article>
    </main>
</body>
</html>
""";
    }

    private String generateSubmitAlreadyCompleteHtml() {
        return """
<!DOCTYPE html>
<html>
<body>
    <main>
        <article>
            <p>You don't seem to be solving the right level. Did you already complete it? <a href="/2023/day/1">[Return to Day 1]</a></p>
        </article>
    </main>
</body>
</html>
""";
    }

    private String generateSettingsUnauthenticatedHtml() {
        return """
<!DOCTYPE html>
<html>
<head>
    <title>Settings - Advent of Code</title>
</head>
<body>
    <p>Please log in</p>
</body>
</html>
""";
    }
}

