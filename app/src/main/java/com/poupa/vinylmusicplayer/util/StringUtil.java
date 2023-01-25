package com.poupa.vinylmusicplayer.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.Collator;
import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * @author SC (soncaokim)
 */

public class StringUtil {
    public enum ClosestMatch {
        FIRST,
        SECOND,
        EQUAL
    }
    private static final Collator collator = Collator.getInstance();
    private static final Pattern accentStripRegex = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    public static int compareIgnoreAccent(@Nullable final String s1, @Nullable final String s2) {
        // Null-proof comparison
        if (s1 == null) {
            return s2 == null ? 0 : -1;
        } else if (s2 == null) {
            return 1;
        }

        return collator.compare(s1, s2);
    }

    /**
     * Returns which string contains a closer match for compareTo.
     *
     * Shortest string > string with compareTo closer to front
     * or equal if neither of those two cases satisfied
     *
     * @param compareTo string being searched for
     * @param first string that contains compareTo
     * @param second string that contains compareTo
     * @return ClosestMatch
     */
    @NonNull public static ClosestMatch closestOfMatches(
            @NonNull final String compareTo,
            @NonNull final String first,
            @NonNull final String second) {
        // shortest name is a closer match
        if (first.length() < second.length()) {
            return ClosestMatch.FIRST;
        } else if (second.length() < first.length()) {
            return ClosestMatch.SECOND;
        }

        // lengths equal :(

        // name with search term closer to front is a closer match
        final int indexFirst = first.indexOf(compareTo);
        final int indexSecond = second.indexOf(compareTo);
        if (indexFirst < indexSecond) {
            return ClosestMatch.FIRST;
        } else if (indexSecond < indexFirst) {
            return ClosestMatch.SECOND;
        }

        // indexes equal

        return ClosestMatch.EQUAL;
    }

    public static String unicodeNormalize(@NonNull final String text) {
        try {
            return Normalizer.normalize(text, Normalizer.Form.NFD);
        } catch (Exception ignored) {
            return text;
        }
    }

    @NonNull
    public static String stripAccent(@NonNull final String text) {
        return accentStripRegex.matcher(text).replaceAll("");
    }

    @NonNull
    public static String join(@NonNull String... s) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < s.length; i++) {
            stringBuilder = stringBuilder.append(s[i]);
        }
        return stringBuilder.toString();
    }
}
