package com.ggiova.gallery.util;

import java.util.Optional;

public class Doubles {
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
