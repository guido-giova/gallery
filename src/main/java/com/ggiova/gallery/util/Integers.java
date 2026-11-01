package com.ggiova.gallery.util;

import java.util.Optional;

public class Integers {
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
