package dev.lueem.extract;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TextUtils {

    public String cleanUpContent(String content) {
        return Arrays.stream(content.split(System.lineSeparator()))
                .filter(line -> !line.trim().isEmpty())
                .collect(Collectors.joining(System.lineSeparator())).trim();
    }

    public String extractCorp(String text) {
        Pattern coopPattern = Pattern.compile("Coop", Pattern.CASE_INSENSITIVE);
        Matcher coopMatcher = coopPattern.matcher(text);

        Pattern migrosPattern = Pattern.compile("Migros", Pattern.CASE_INSENSITIVE);
        Matcher migrosMatcher = migrosPattern.matcher(text);

        if (coopMatcher.find()) {
            return "Coop";
        } else if (migrosMatcher.find()) {
            return "Migros";
        } else {
            throw new IllegalArgumentException("Neither Coop nor Migros found in text.");
        }
    }

    public String extractTotal(String receipt) {
        String pattern = "Total CHF (\\d+\\.\\d{2})";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(receipt);

        if (m.find()) {
            return m.group(1);
        } else {
            return "0.00";
        }
    }

    public String extractDate(String text) {
        String datumRegex = "\\b\\d{2}\\.\\d{2}\\.\\d{2}\\b";
        Pattern pattern = Pattern.compile(datumRegex);
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return matcher.group(0);
        }
        return getTimestamp();
    }

    public String extractArticlesUntilTotal(String receipt) {
        int indexOfTotal = receipt.indexOf("Total CHF");
        if (indexOfTotal != -1) {
            return receipt.substring(0, indexOfTotal).trim();
        }
        return receipt;
    }

    private static String getTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(formatter);
        return timestamp;
    }

}
