package com.ggiova.gallery.util;

import java.util.Optional;

/**
 * Utility class for {@link Double}s.
 */
public class Doubles {
    /**
     * Tries to parse a String {@code s} to double. If succeeds, returns an Optional with the value, if it fails, it
     * returns an empty Optional.
     *
     * @param s String that will be parsed.
     * @return An Optional that may contain the parsed value.
     */
    public static Optional<Double> parse(String s) {
        try {
            return s == null
                    ? Optional.empty()
                    : Optional.of(Double.parseDouble(s));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
