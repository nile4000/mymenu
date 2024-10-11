package dev.lueem.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Utility class for processing and extracting information from text content,
 * typically used for parsing receipts.
 */
@ApplicationScoped
public class TextUtils {

    // Precompiled regex patterns for efficiency
    private static final Pattern COOP_PATTERN = Pattern.compile("Coop", Pattern.CASE_INSENSITIVE);
    private static final Pattern MIGROS_PATTERN = Pattern.compile("Migros", Pattern.CASE_INSENSITIVE);
    private static final Pattern TOTAL_PATTERN = Pattern.compile("Total CHF (\\d+\\.\\d{2})", Pattern.CASE_INSENSITIVE);
    private static final Pattern DATE_PATTERN = Pattern.compile("\\b\\d{2}\\.\\d{2}\\.\\d{2}\\b");
    private static final Pattern RABATT_PATTERN = Pattern.compile("^Rabatt\\s+.*", Pattern.CASE_INSENSITIVE);
    private static final Pattern BON_PATTERN = Pattern.compile("^Bon\\s+.*", Pattern.CASE_INSENSITIVE);

    // Prefix used to locate the total in the receipt
    private static final String TOTAL_PREFIX = "Total CHF";
    private static final String RABATT_PREFIX = "Rabatt ";
    private static final String BON_PREFIX = "Bon ";

    // Formatter for generating timestamps
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * Cleans up the provided content by removing empty lines and trimming whitespace.
     *
     * @param content the raw text content from a receipt
     * @return a cleaned-up version of the text with no empty lines
     */
    public String cleanUpContent(String content) {
        return Arrays.stream(content.split(System.lineSeparator()))
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .collect(Collectors.joining(System.lineSeparator()))
                .trim();
    }

    /**
     * Extracts the corporation name ("Coop" or "Migros") from the provided text.
     *
     * @param text the text to search for corporation names
     * @return the name of the corporation found in the text
     * @throws IllegalArgumentException if neither "Coop" nor "Migros" is found
     */
    public String extractCorp(String text) {
        if (COOP_PATTERN.matcher(text).find()) {
            return "Coop";
        } else if (MIGROS_PATTERN.matcher(text).find()) {
            return "Migros";
        } else {
            throw new IllegalArgumentException("Neither Coop nor Migros found in text.");
        }
    }

    /**
     * Extracts the total amount from the receipt.
     *
     * @param receipt the receipt text containing the total
     * @return the total amount as a string (e.g., "123.45"), or "0.00" if not found
     */
    public String extractTotal(String receipt) {
        Matcher matcher = TOTAL_PATTERN.matcher(receipt);
        return matcher.find() ? matcher.group(1) : "0.00";
    }

    /**
     * Extracts the date from the text. If no date is found, returns the current timestamp.
     *
     * @param text the text to search for a date
     * @return the extracted date in the format "dd.MM.yy" or the current timestamp if not found
     */
    public String extractDate(String text) {
        Matcher matcher = DATE_PATTERN.matcher(text);
        return matcher.find() ? matcher.group() : getCurrentTimestamp();
    }

    /**
     * Extracts the articles section from the receipt, stopping before 'Total CHF', 'Rabatt', or 'Bon'.
     *
     * @param receipt the full receipt text
     * @return the substring of the receipt containing the articles
     */
    public String extractArticlesUntilTotal(String receipt) {
        // Define patterns to search for 'Total CHF', 'Rabatt', and 'Bon'
        Matcher totalMatcher = Pattern.compile(Pattern.quote(TOTAL_PREFIX), Pattern.CASE_INSENSITIVE).matcher(receipt);
        Matcher rabattMatcher = RABATT_PATTERN.matcher(receipt);
        Matcher bonMatcher = BON_PATTERN.matcher(receipt);

        int totalIndex = -1;
        int rabattIndex = -1;
        int bonIndex = -1;

        if (totalMatcher.find()) {
            totalIndex = totalMatcher.start();
        }

        if (rabattMatcher.find()) {
            rabattIndex = rabattMatcher.start();
        }

        if (bonMatcher.find()) {
            bonIndex = bonMatcher.start();
        }

        // Determine the earliest index among 'Total CHF', 'Rabatt', and 'Bon'
        int earliestIndex = receipt.length(); // Default to end of string

        if (totalIndex != -1 && totalIndex < earliestIndex) {
            earliestIndex = totalIndex;
        }

        if (rabattIndex != -1 && rabattIndex < earliestIndex) {
            earliestIndex = rabattIndex;
        }

        if (bonIndex != -1 && bonIndex < earliestIndex) {
            earliestIndex = bonIndex;
        }

        // Extract the substring up to the earliest keyword
        if (earliestIndex != receipt.length()) {
            return receipt.substring(0, earliestIndex).trim();
        } else {
            // If none of the keywords are found, return the entire receipt
            return receipt;
        }
    }

    /**
     * Generates a current timestamp in the format "yyyyMMddHHmmss".
     *
     * @return the formatted current timestamp
     */
    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(TIMESTAMP_FORMATTER);
    }
}
