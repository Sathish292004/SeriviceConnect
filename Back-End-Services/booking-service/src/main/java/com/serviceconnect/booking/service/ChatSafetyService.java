package com.serviceconnect.booking.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ChatSafetyService provides authoritative server-side sanitization and filtering
 * to prevent exchange of personal contact information (phone numbers, emails)
 * and abusive/prohibited words.
 */
@Service
public class ChatSafetyService {

    private static final Logger log = LoggerFactory.getLogger(ChatSafetyService.class);

    // Email regex: standard RFC 5322 compatible regex
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b",
            Pattern.CASE_INSENSITIVE
    );

    // Phone number patterns:
    // Matches Indian 10-digit mobile numbers with optional country code (+91, 0, etc.)
    // and international formats with separators, while strictly preserving 1-6 digit numbers like
    // "iPhone 13", "₹3500", "3 km", "72%", "2 years old".
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "(?:(?:\\+?91|0)[\\s-]?)?(?:[6-9]\\d{9}|[6-9]\\d{4}[\\s-]\\d{5}|[6-9]\\d{2}[\\s-]\\d{3}[\\s-]\\d{4}|\\b\\d{5}[\\s-]\\d{5}\\b|\\b\\d{3}[\\s-]\\d{3}[\\s-]\\d{4}\\b)"
    );

    // Prohibited / abusive words list
    private static final List<String> PROHIBITED_WORDS = Arrays.asList(
            "badword",
            "abusive",
            "offensive",
            "scam",
            "fraud",
            "idiot",
            "abuse",
            "spam"
    );

    private static final Pattern PROHIBITED_PATTERN;

    static {
        StringBuilder sb = new StringBuilder("\\b(?i)(");
        for (int i = 0; i < PROHIBITED_WORDS.size(); i++) {
            if (i > 0) sb.append("|");
            sb.append(Pattern.quote(PROHIBITED_WORDS.get(i)));
        }
        sb.append(")\\b");
        PROHIBITED_PATTERN = Pattern.compile(sb.toString());
    }

    /**
     * Sanitizes input text before persistence.
     * Replaces phone numbers with **********, emails with ****@*****.***,
     * and prohibited words with ****.
     *
     * @param text raw input text
     * @return sanitized text safe for storage and display
     */
    public String sanitize(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }

        boolean wasModified = false;

        // 1. Mask Email Addresses
        Matcher emailMatcher = EMAIL_PATTERN.matcher(text);
        if (emailMatcher.find()) {
            text = emailMatcher.replaceAll("****@*****.***");
            wasModified = true;
        }

        // 2. Mask Phone Numbers
        Matcher phoneMatcher = PHONE_PATTERN.matcher(text);
        if (phoneMatcher.find()) {
            text = phoneMatcher.replaceAll("**********");
            wasModified = true;
        }

        // 3. Mask Prohibited / Abusive Words
        Matcher prohibitedMatcher = PROHIBITED_PATTERN.matcher(text);
        if (prohibitedMatcher.find()) {
            text = prohibitedMatcher.replaceAll("****");
            wasModified = true;
        }

        if (wasModified) {
            log.info("Message sanitized before persistence.");
        }

        return text;
    }
}
