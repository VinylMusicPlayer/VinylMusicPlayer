package com.poupa.vinylmusicplayer.util;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PrefKey {
    // The value of the preference key
    final String value;

    // Wherether the annotated value should be exportable
    final boolean isExportImportable;

    // Wherether the annotated field value is just a preference key prefix, i.e. not the whole key
    final boolean isPrefixed;

    // Collection of all declared keys
    private static final Set<PrefKey> declaredKeys = new HashSet<>();

    private PrefKey(@NonNull final String theValue, final boolean exportable, final boolean prefix) {
        value = theValue;
        isExportImportable = exportable;
        isPrefixed = prefix;

        synchronized (declaredKeys) {
            declaredKeys.add(this);
        }
    }

    boolean isMatchingKey(@NonNull final String key) {
        if (isPrefixed) {
            return key.startsWith(value);
        } else {
            return TextUtils.equals(value, key);
        }
    }

    // Declare a pref key
    @NonNull
    public static String nonExportableKey(@NonNull final String value) {
        return new PrefKey(value, false, false).value;
    }

    // Declare a prefixed pref key
    @NonNull
    public static String nonExportablePrefixedKey(@NonNull final String value) {
        return new PrefKey(value, false, true).value;
    }

    // Declare am exportable/importable pref key
    @NonNull
    public static String exportableKey(@NonNull final String value) {
        return new PrefKey(value, true, false).value;
    }

    // Declare am exportable/importable prefixed pref key
    @NonNull
    public static String exportablePrefixedKey(@NonNull final String value) {
        return new PrefKey(value, true, true).value;
    }

    // Get the list of all declared keys, with an optional filter
    @NonNull
    static Set<PrefKey> getKeys(@Nullable final Predicate<? super PrefKey> filter) {
        synchronized (declaredKeys) {
            if (filter == null) {
                return Collections.unmodifiableSet(declaredKeys);
            } else {
                return declaredKeys.stream().filter(filter).collect(Collectors.toUnmodifiableSet());
            }
        }
    }
}
