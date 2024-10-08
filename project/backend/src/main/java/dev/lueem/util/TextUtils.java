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
    private static final Pattern TOTAL_PATTERN = Pattern.compile("Total CHF (\\d+\\.\\d{2})");
    private static final Pattern DATE_PATTERN = Pattern.compile("\\b\\d{2}\\.\\d{2}\\.\\d{2}\\b");

    // Prefix used to locate the total in the receipt
    private static final String TOTAL_PREFIX = "Total CHF";

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
     * Extracts the articles section from the receipt, stopping before the total amount.
     *
     * @param receipt the full receipt text
     * @return the substring of the receipt containing the articles
     */
    public String extractArticlesUntilTotal(String receipt) {
        int index = receipt.indexOf(TOTAL_PREFIX);
        return index != -1 ? receipt.substring(0, index).trim() : receipt;
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
