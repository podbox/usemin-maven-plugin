package com.podbox.parsers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @since 18/08/15.
 */
public class ThymeleafUrlParser {

    public static class InvalidThymeleafUrl extends RuntimeException {

        public InvalidThymeleafUrl(Exception e) {
            super(e);
        }

        public InvalidThymeleafUrl(String s) {
            super(s);
        }
    }

    public static final String REGEX = "@\\{~?(\\/(?=\\/))?([^#\\(\\)\\{\\};]*)(\\})?";
    public static final Pattern PATTERN = Pattern.compile(REGEX);

    /**
     * Extract url out of a thymeleaf URL like @{http://www.podbox.com}
     *
     * @param thymeleafUrl
     * @return extracted URL
     * @throws com.podbox.parsers.ThymeleafUrlParser.InvalidThymeleafUrl if url is not a thymeleaf formatted one
     */
    public static String extractUrl(String thymeleafUrl) {

        thymeleafUrl = thymeleafUrl.replaceAll("\\(.*\\)", "");
        thymeleafUrl = thymeleafUrl.replaceAll("#.*\\}", "");
        String extracted;
        try {
            Matcher matcher = PATTERN.matcher(thymeleafUrl);
            matcher.matches();
            extracted = matcher.group(2);
            if (extracted.isEmpty()) {
                throw new InvalidThymeleafUrl("Impossible to resolve an url");
            }
        }
        catch (Exception e) {
            throw new InvalidThymeleafUrl(e);
        }
        return extracted;
    }
}
