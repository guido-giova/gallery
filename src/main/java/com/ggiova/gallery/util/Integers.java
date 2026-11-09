package com.ggiova.gallery.util;

import java.util.Optional;

/**
 * Utility class for {@link Integer}s.
 */
public class Integers {
    private Integers() {
        throw new UnsupportedOperationException("Don't instantiate!");
    }

    /**
     * Tries to parse a String {@code s} to int. If succeeds, returns an Optional with the value, if it fails, it
     * returns an empty Optional.
     *
     * @param s String that will be parsed.
     * @return An Optional that may contain the parsed value.
     */
    public static Optional<Integer> parse(String s) {
        try {
            return s == null
                      ? Optional.empty()
                      : Optional.of(Integer.parseInt(s));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
