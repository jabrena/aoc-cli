package info.jab.aoc.client;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Client for interacting with Advent of Code website
 */
public class AocClient {
    private static final String AOC_BASE_URL = "https://adventofcode.com";
    private static final String USER_AGENT = "Mozilla/5.0 (compatible; AoC-Script/1.0)";

    private final HttpClient httpClient;
    private final String sessionCookie;

    public AocClient(String sessionCookie) {
        if (sessionCookie == null || sessionCookie.trim().isEmpty()) {
            throw new IllegalArgumentException("Session cookie cannot be null or empty");
        }
        this.httpClient = HttpClient.newBuilder().build();
        this.sessionCookie = sessionCookie.trim();
    }

    public boolean testAuthentication() throws IOException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AOC_BASE_URL + "/settings"))
                .header("Cookie", "session=" + sessionCookie)
                .header("User-Agent", USER_AGENT)
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300 && response.body() != null) {
                String body = response.body();
                return body.toLowerCase().contains("log out");
            }
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request interrupted", e);
        }
    }

    public String getUsername() throws IOException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AOC_BASE_URL + "/settings"))
                .header("Cookie", "session=" + sessionCookie)
                .header("User-Agent", USER_AGENT)
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300 && response.body() != null) {
                String body = response.body();
                Pattern pattern = Pattern.compile("<span class=\"user\">([^<]+)</span>");
                Matcher matcher = pattern.matcher(body);
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }
            return "Unknown";
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request interrupted", e);
        }
    }

    public String downloadInput(int year, int day) throws IOException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("%s/%d/day/%d/input", AOC_BASE_URL, year, day)))
                .header("Cookie", "session=" + sessionCookie)
                .header("User-Agent", USER_AGENT)
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.body() == null) {
                throw new IOException("Empty response body");
            }

            String body = response.body();

            switch (response.statusCode()) {
                case 200:
                    return body;
                case 400:
                    throw new IOException("Please log in to get your input");
                case 404:
                    throw new IOException("Day " + day + " not available yet or doesn't exist");
                case 500:
                    throw new IOException("Server error");
                default:
                    throw new IOException("Unexpected response code: " + response.statusCode());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request interrupted", e);
        }
    }

    public SubmissionResult submitAnswer(int year, int day, int part, String answer) throws IOException {
        String formData = "level=" + part + "&answer=" + java.net.URLEncoder.encode(answer, java.nio.charset.StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("%s/%d/day/%d/answer", AOC_BASE_URL, year, day)))
                .header("Cookie", "session=" + sessionCookie)
                .header("User-Agent", USER_AGENT)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formData))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IOException("Failed to submit answer (HTTP " + response.statusCode() + ")");
            }

            if (response.body() == null) {
                throw new IOException("Empty response body");
            }

            String body = response.body();

            if (body.contains("That's the right answer")) {
                return SubmissionResult.CORRECT;
            } else if (body.contains("That's not the right answer")) {
                String message = extractMessage(body, "That's not the right answer");
                return new SubmissionResult(SubmissionResult.Status.WRONG, message, body);
            } else if (body.contains("You gave an answer too recently")) {
                String message = extractMessage(body, "You gave an answer too recently");
                return new SubmissionResult(SubmissionResult.Status.TOO_RECENT, message, body);
            } else if (body.contains("already complete")) {
                return SubmissionResult.ALREADY_COMPLETE;
            } else {
                return new SubmissionResult(SubmissionResult.Status.UNKNOWN, "Unknown response", body);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request interrupted", e);
        }
    }

    private String extractMessage(String html, String startMarker) {
        // AOC response messages are in <article><p> tags within <main>
        // First, try to extract from article paragraph
        Pattern articlePattern = Pattern.compile("<article[^>]*>\\s*<p[^>]*>(.*?)</p>\\s*</article>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher articleMatcher = articlePattern.matcher(html);

        if (articleMatcher.find()) {
            String articleContent = articleMatcher.group(1);
            if (articleContent.contains(startMarker)) {
                return cleanAndExtractRelevantMessage(articleContent, startMarker);
            }
        }

        // Fallback: look for the message in any paragraph
        Pattern paragraphPattern = Pattern.compile("<p[^>]*>(.*?" + Pattern.quote(startMarker) + ".*?)</p>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher paragraphMatcher = paragraphPattern.matcher(html);

        if (paragraphMatcher.find()) {
            return cleanAndExtractRelevantMessage(paragraphMatcher.group(1), startMarker);
        }

        // Last resort: simple text extraction
        Pattern simplePattern = Pattern.compile("([^<]*" + Pattern.quote(startMarker) + "[^<]*)", Pattern.CASE_INSENSITIVE);
        Matcher simpleMatcher = simplePattern.matcher(html);

        if (simpleMatcher.find()) {
            return cleanHtmlEntities(simpleMatcher.group(1).trim());
        }

        return startMarker; // Fallback to just the start marker
    }

    private String cleanAndExtractRelevantMessage(String content, String startMarker) {
        // Clean HTML tags and entities
        String cleaned = content.replaceAll("<[^>]+>", " ") // Remove HTML tags
                               .replaceAll("\\s+", " ")     // Normalize whitespace
                               .trim();

        cleaned = cleanHtmlEntities(cleaned);

        // For wrong answers, extract the core message and timing info
        if (startMarker.contains("not the right answer")) {
            // Look for specific patterns and extract only the essential parts
            StringBuilder result = new StringBuilder();
            String[] sentences = cleaned.split("\\.");

            for (String sentence : sentences) {
                sentence = sentence.trim();
                if (sentence.isEmpty()) continue;

                // Skip sentences that contain navigation/help text
                if (sentence.toLowerCase().contains("subreddit") ||
                    sentence.toLowerCase().contains("about page") ||
                    sentence.toLowerCase().contains("general tips") ||
                    sentence.toLowerCase().contains("return to")) {
                    continue;
                }

                result.append(sentence);

                // Stop at the first sentence that mentions waiting/timing
                if (sentence.toLowerCase().contains("wait") ||
                    sentence.toLowerCase().contains("minute") ||
                    sentence.toLowerCase().contains("recently")) {
                    result.append(".");
                    break;
                }
                result.append(". ");
            }

            String finalResult = result.toString().trim();

            // If we didn't get a good result, try a simpler approach
            if (finalResult.isEmpty() || !finalResult.contains("not the right answer")) {
                // Extract just the first sentence and any timing sentence
                for (String sentence : sentences) {
                    sentence = sentence.trim();
                    if (sentence.contains("not the right answer")) {
                        finalResult = sentence + ".";
                        break;
                    }
                }

                // Add timing info if available
                for (String sentence : sentences) {
                    sentence = sentence.trim();
                    if ((sentence.toLowerCase().contains("wait") ||
                         sentence.toLowerCase().contains("minute")) &&
                        !sentence.toLowerCase().contains("subreddit")) {
                        finalResult += " " + sentence + ".";
                        break;
                    }
                }
            }

            return finalResult;
        }

        // For rate limiting, extract timing information
        if (startMarker.contains("too recently")) {
            // Look for timing patterns like "5m 23s" or "1 minute"
            Pattern timePattern = Pattern.compile("(\\d+m\\s*\\d*s?|\\d+\\s*minutes?|\\d+\\s*seconds?)", Pattern.CASE_INSENSITIVE);
            Matcher timeMatcher = timePattern.matcher(cleaned);

            if (timeMatcher.find()) {
                String timeInfo = timeMatcher.group(1);
                return "You gave an answer too recently. You have " + timeInfo + " left to wait.";
            }

            // Fallback: return first sentence with timing info
            String[] sentences = cleaned.split("\\.");
            for (String sentence : sentences) {
                if (sentence.toLowerCase().contains("wait") || sentence.toLowerCase().contains("recently")) {
                    return sentence.trim() + ".";
                }
            }
        }

        return cleaned;
    }

    private String cleanHtmlEntities(String text) {
        return text.replaceAll("&lt;", "<")
                  .replaceAll("&gt;", ">")
                  .replaceAll("&amp;", "&")
                  .replaceAll("&quot;", "\"")
                  .replaceAll("&#39;", "'")
                  .replaceAll("&nbsp;", " ");
    }

    /**
     * Get the total count of completed days for a year.
     * This includes both partially completed days (Part 1 only) and fully completed days (both parts).
     *
     * In Advent of Code:
     * - 'complete' = Part 1 completed
     * - 'verycomplete' = Both Part 1 and Part 2 completed
     *
     * @param year The year to check
     * @return Total number of days with any completion status
     * @throws IOException if the request fails
     */
    public int getCompletedDaysCount(int year) throws IOException {
        String body = getYearPage(year);
        Pattern pattern = Pattern.compile("calendar-(verycomplete|complete)");
        Matcher matcher = pattern.matcher(body);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    /**
     * Get pending parts that still need to be completed.
     * Returns a list of strings in format "day_part" (e.g., "9_2", "14_1", "14_2").
     *
     * Logic:
     * - Days with no progress: return "day_1"
     * - Days with only Part 1 completed: return "day_2"
     * - Days with both parts completed: return nothing
     *
     * @param year The year to check
     * @return List of pending parts in "day_part" format
     * @throws IOException if the request fails
     */
    public List<String> getPendingParts(int year) throws IOException {
        String body = getYearPage(year);

        // Extract all completed days (any completion status)
        Set<Integer> anyCompletedDays = new TreeSet<>();
        Pattern anyCompletePattern = Pattern.compile("calendar-day(\\d+) calendar-[^\"]*complete");
        Matcher anyCompleteMatcher = anyCompletePattern.matcher(body);
        while (anyCompleteMatcher.find()) {
            anyCompletedDays.add(Integer.parseInt(anyCompleteMatcher.group(1)));
        }

        // Extract fully completed days (both parts solved - verycomplete status)
        Set<Integer> fullyCompletedDays = new TreeSet<>();
        Pattern veryCompletePattern = Pattern.compile("calendar-day(\\d+) calendar-[^\"]*verycomplete");
        Matcher veryCompleteMatcher = veryCompletePattern.matcher(body);
        while (veryCompleteMatcher.find()) {
            fullyCompletedDays.add(Integer.parseInt(veryCompleteMatcher.group(1)));
        }

        // Build list of pending parts
        List<String> pendingParts = new ArrayList<>();
        for (int day = 1; day <= 25; day++) {
            if (fullyCompletedDays.contains(day)) {
                // Both parts completed - nothing pending
                continue;
            } else if (anyCompletedDays.contains(day)) {
                // Only Part 1 completed - need Part 2
                pendingParts.add(day + "_2");
            } else {
                // No progress - need Part 1
                pendingParts.add(day + "_1");
            }
        }

        return pendingParts;
    }

    /**
     * Get list of years that have pending work (not all 50 stars completed).
     * Checks years from 2015 (first AoC year) to current year.
     *
     * @return List of years with pending work
     * @throws IOException if any request fails
     */
    public List<Integer> getPendingYears() throws IOException {
        List<Integer> pendingYears = new ArrayList<>();
        int currentYear = java.time.Year.now().getValue();

        // Check years from 2015 (first AoC year) to current year
        for (int year = 2015; year <= currentYear; year++) {
            try {
                List<String> pendingParts = getPendingParts(year);
                if (!pendingParts.isEmpty()) {
                    pendingYears.add(year);
                }
            } catch (IOException e) {
                // If we can't access a year (e.g., it doesn't exist yet), skip it
                // This handles cases where the current year's AoC hasn't started yet
                continue;
            }
        }

        return pendingYears;
    }


    public String getProblemStatement(int year, int day) throws IOException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("%s/%d/day/%d", AOC_BASE_URL, year, day)))
                .header("Cookie", "session=" + sessionCookie)
                .header("User-Agent", USER_AGENT)
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                switch (response.statusCode()) {
                    case 400:
                        throw new IOException("Please log in to get the problem statement");
                    case 404:
                        throw new IOException("Day " + day + " not available yet or doesn't exist");
                    case 500:
                        throw new IOException("Server error");
                    default:
                        throw new IOException("Failed to get problem statement (HTTP " + response.statusCode() + ")");
                }
            }

            if (response.body() == null) {
                throw new IOException("Empty response body");
            }

            String body = response.body();

            // Extract the problem description from the HTML
            return extractProblemDescription(body);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request interrupted", e);
        }
    }

    private String extractProblemDescription(String html) {
        // Extract the main problem description from the HTML
        // The problem content is typically within <article class="day-desc"> tags
        Pattern pattern = Pattern.compile("<article class=\"day-desc\">(.*?)</article>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(html);

        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String articleContent = matcher.group(1);
            // Clean up HTML tags and convert to readable text
            String cleanContent = cleanHtmlContent(articleContent);
            result.append(cleanContent).append("\n\n");
        }

        if (result.length() == 0) {
            return "Problem statement not found or not available yet.";
        }

        return result.toString().trim();
    }

    private String cleanHtmlContent(String html) {
        // Remove HTML tags and convert common entities
        return html
                .replaceAll("<h2[^>]*>", "\n=== ")
                .replaceAll("</h2>", " ===\n")
                .replaceAll("<p[^>]*>", "\n")
                .replaceAll("</p>", "\n")
                .replaceAll("<pre[^>]*><code[^>]*>", "\n```\n")
                .replaceAll("</code></pre>", "\n```\n")
                .replaceAll("<code[^>]*>", "`")
                .replaceAll("</code>", "`")
                .replaceAll("<em[^>]*>", "*")
                .replaceAll("</em>", "*")
                .replaceAll("<strong[^>]*>", "**")
                .replaceAll("</strong>", "**")
                .replaceAll("<ul[^>]*>", "\n")
                .replaceAll("</ul>", "\n")
                .replaceAll("<li[^>]*>", "  - ")
                .replaceAll("</li>", "\n")
                .replaceAll("<[^>]+>", "")  // Remove remaining HTML tags
                .replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">")
                .replaceAll("&amp;", "&")
                .replaceAll("&quot;", "\"")
                .replaceAll("&#39;", "'")
                .replaceAll("\\n\\s*\\n\\s*\\n", "\n\n")  // Reduce multiple newlines
                .trim();
    }

    private String getYearPage(int year) throws IOException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("%s/%d", AOC_BASE_URL, year)))
                .header("Cookie", "session=" + sessionCookie)
                .header("User-Agent", USER_AGENT)
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IOException("Failed to get year page (HTTP " + response.statusCode() + ")");
            }

            if (response.body() == null) {
                throw new IOException("Empty response body");
            }

            return response.body();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request interrupted", e);
        }
    }

    public static class SubmissionResult {
        private final Status status;
        private final String message;
        private final String fullResponse;

        public SubmissionResult(Status status, String message, String fullResponse) {
            this.status = status;
            this.message = message;
            this.fullResponse = fullResponse;
        }

        public Status getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public String getFullResponse() {
            return fullResponse;
        }

        // Backward compatibility methods
        public static final SubmissionResult CORRECT = new SubmissionResult(Status.CORRECT, "That's the right answer!", "");
        public static final SubmissionResult ALREADY_COMPLETE = new SubmissionResult(Status.ALREADY_COMPLETE, "Already complete", "");

        @Override
        public String toString() {
            return status + (message.isEmpty() ? "" : ": " + message);
        }

        public enum Status {
            CORRECT,
            WRONG,
            TOO_RECENT,
            ALREADY_COMPLETE,
            UNKNOWN
        }
    }
}

