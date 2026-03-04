package com.vidara.tradecenter.common.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

public final class SlugUtils {

    // Private constructor - utility class, cannot be instantiated
    private SlugUtils() {
        throw new IllegalStateException("Utility class - cannot be instantiated");
    }

    // ================================
    // MAIN SLUG METHOD
    //
    // Converts any string to URL-friendly slug
    //
    //   "Fresh Vegetables"       → "fresh-vegetables"
    //   "Organic Cinnamon Sticks" → "organic-cinnamon-sticks"
    //   "100% Pure  Honey!!!"   → "100-pure-honey"
    //   "Café & Résumé"         → "cafe-resume"
    //   "  Hello   World  "     → "hello-world"
    //   null                    → ""
    //   ""                      → ""
    // ================================

    public static String toSlug(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }

        //Normalize unicode characters
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);

        //Remove non-ASCII characters (accent marks)
        String withoutAccents = Pattern.compile("[\\p{InCombiningDiacriticalMarks}]")
                .matcher(normalized)
                .replaceAll("");

        //Convert to lowercase
        String lowercase = withoutAccents.toLowerCase();

        //Replace any non-alphanumeric character with hyphen
        String withHyphens = lowercase.replaceAll("[^a-z0-9]+", "-");

        //Remove leading and trailing hyphens
        String trimmed = withHyphens.replaceAll("^-|-$", "");

        return trimmed;
    }

    // ================================
    // SLUG WITH UNIQUE SUFFIX
    //
    // Usage: When slug already exists in database,
    //        append a number to make it unique
    //
    // Examples:
    //   makeUnique("fresh-vegetables", 1) → "fresh-vegetables-1"
    //   makeUnique("fresh-vegetables", 2) → "fresh-vegetables-2"
    // ================================

    public static String makeUnique(String slug, int counter) {
        return slug + "-" + counter;
    }
}