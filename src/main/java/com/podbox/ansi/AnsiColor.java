package com.podbox.ansi;

import static com.google.common.base.StandardSystemProperty.OS_NAME;
import static java.lang.System.console;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;

public final class AnsiColor {

    public static final String RESET;
    public static final String BLACK;
    public static final String RED;
    public static final String GREEN;
    public static final String YELLOW;
    public static final String BLUE;
    public static final String MAGENTA;
    public static final String CYAN;
    public static final String WHITE;

    static {
        if (isAvailable()) {
            RESET = "\u001b[0m";
            BLACK = "\u001b[30m";
            RED = "\u001b[31m";
            GREEN = "\u001b[32m";
            YELLOW = "\u001b[33m";
            BLUE = "\u001b[34m";
            MAGENTA = "\u001b[35m";
            CYAN = "\u001b[36m";
            WHITE = "\u001b[37m";
        }
        else {
            RESET = BLACK = RED = GREEN = YELLOW = BLUE = MAGENTA = CYAN = WHITE = EMPTY;
        }
    }

    private AnsiColor() {
    }

    private static boolean isAvailable() {
        try {
            return console() != null && !containsIgnoreCase(OS_NAME.value(), "win");
        } catch (final Exception e) {
            return false;
        }
    }

}
