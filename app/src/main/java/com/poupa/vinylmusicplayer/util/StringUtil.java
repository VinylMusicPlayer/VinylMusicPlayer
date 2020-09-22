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
    private static final Collator collator = Collator.getInstance();
    private static Pattern accentStripRegex = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    public static int compareIgnoreAccent(@Nullable final String s1, @Nullable final String s2) {
        // Null-proof comparison
        if (s1 == null) {
            return s2 == null ? 0 : -1;
        } else if (s2 == null) {
            return 1;
        }

        return collator.compare(s1, s2);
    }

    public static String unicodeNormalize(@NonNull final String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFD);
    }

    @NonNull
    public static String stripAccent(@NonNull final String text) {
        return accentStripRegex.matcher(text).replaceAll("");
    }
}
