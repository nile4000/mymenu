package dev.lueem.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Utility class for processing and extracting information from text content,
 * typically used for parsing receipts.
 */
@ApplicationScoped
public class TextUtils {

    private static final Logger LOGGER = Logger.getLogger(TextUtils.class.getName());

    // Precompiled regex patterns for efficiency
    private static final Pattern COOP_PATTERN = Pattern.compile("Coop", Pattern.CASE_INSENSITIVE);
    private static final Pattern MIGROS_PATTERN = Pattern.compile("Migros", Pattern.CASE_INSENSITIVE);
    private static final Pattern TOTAL_PATTERN = Pattern.compile("(?m)^\\bTotal CHF\\b.*", Pattern.CASE_INSENSITIVE);
    private static final Pattern DATE_PATTERN = Pattern.compile("\\b\\d{2}\\.\\d{2}\\.\\d{2}\\b");
    private static final Pattern RABATT_PATTERN = Pattern.compile("(?m)^\\bRabatt\\b.*", Pattern.CASE_INSENSITIVE);
    private static final Pattern BON_PATTERN = Pattern.compile("(?m)^\\bBon\\b.*", Pattern.CASE_INSENSITIVE);
    private static final Pattern HEADER_PATTERN = Pattern
            .compile("Artikel\\s+Menge\\s+Preis\\s+Aktion\\s+Total\\s+Zusatz", Pattern.CASE_INSENSITIVE);

    private static final Pattern TERMINATOR_PATTERN = Pattern.compile("(?m)^\\b(?:Total CHF|Rabatt|Bon)\\b.*",
            Pattern.CASE_INSENSITIVE);

    // Prefix used to locate the total in the receipt
    private static final String TOTAL_PREFIX = "Total CHF";
    private static final String RABATT_PREFIX = "Rabatt ";
    private static final String BON_PREFIX = "Bon ";

    // Formatter for generating timestamps
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * Cleans up the provided content by removing empty lines and trimming
     * whitespace.
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
        if (matcher.find()) {
            // Extrahiere den Betrag aus der Zeile "Total CHF X.XX"
            String totalLine = matcher.group();
            Matcher amountMatcher = Pattern.compile("\\d+\\.\\d{2}").matcher(totalLine);
            if (amountMatcher.find()) {
                return amountMatcher.group();
            }
        }
        return "0.00";
    }

    /**
     * Extracts the date from the text. If no date is found, returns the current
     * timestamp.
     *
     * @param text the text to search for a date
     * @return the extracted date in the format "dd.MM.yy" oder den aktuellen
     *         Zeitstempel, wenn kein Datum gefunden wurde
     */
    public String extractDate(String text) {
        Matcher matcher = DATE_PATTERN.matcher(text);
        return matcher.find() ? matcher.group() : getCurrentTimestamp();
    }

    /**
     * Extracts the articles section from the receipt, stopping before 'Total CHF',
     * 'Rabatt', or 'Bon'.
     *
     * @param receipt the full receipt text
     * @return the substring of the receipt containing the articles
     */
    public String extractArticlesUntilTotal(String receipt) {
        Matcher terminatorMatcher = TERMINATOR_PATTERN.matcher(receipt);

        int terminatorIndex = -1;

        if (terminatorMatcher.find()) {
            terminatorIndex = terminatorMatcher.start();
            String matchedLine = terminatorMatcher.group();
            LOGGER.info("Terminierende Zeile gefunden: \"" + matchedLine + "\" bei Index: " + terminatorIndex);
        } else {
            LOGGER.info("Keine terminierenden Schlüsselwörter gefunden.");
        }

        // extracts substring until terminator
        if (terminatorIndex != -1) {
            String extracted = receipt.substring(0, terminatorIndex).trim();
            return extracted;
        } else {
            // Wenn keines der Schlüsselwörter gefunden wurde, gib den gesamten Beleg zurück
            LOGGER.info("No Keyword found. Returning entire receipt.");
            return receipt;
        }
    }

    public int findTotalRowNumber(String receipt) {
        String[] lines = receipt.split(System.lineSeparator());

        // Finde den Start der Artikelzeilen
        int firstArticleLine = findFirstArticleStart(receipt);
        if (firstArticleLine == -1) {
            LOGGER.warning("Kopfzeile für Artikelzeilen nicht gefunden. 'Total' kann nicht bestimmt werden.");
            return -1;
        }

        for (int i = firstArticleLine; i < lines.length; i++) {
            Matcher matcher = TOTAL_PATTERN.matcher(lines[i]);
            if (matcher.find()) {
                LOGGER.info("\"Total CHF\" Zeile gefunden bei Zeile " + (i + 1));
                return i + 1;
            }
        }

        LOGGER.warning("\"Total CHF\" Zeile nicht gefunden.");
        return -1;
    }

    private int findFirstArticleStart(String receipt) {
        String[] lines = receipt.split(System.lineSeparator());

        for (int i = 0; i < lines.length; i++) {
            Matcher matcher = HEADER_PATTERN.matcher(lines[i]);
            if (matcher.find()) {
                LOGGER.info("Kopfzeile gefunden bei Zeile: " + (i + 1));
                return i + 1;
            }
        }

        LOGGER.warning("Kopfzeile nicht gefunden.");
        return -1;
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
