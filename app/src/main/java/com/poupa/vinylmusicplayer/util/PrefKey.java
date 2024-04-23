// TODO Move this class closer to PreferenceUtil
package com.poupa.vinylmusicplayer.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PrefKey {
    private final String value;

    // Wherether the annotated value should be exportable
    private final boolean isExportImportable;

    // Wherether the annotated field value is just a preference key prefix, i.e. not the whole key
    private final boolean isPrefix;

    private static final Set<PrefKey> declaredKeys = new HashSet<>();

    private PrefKey(@NonNull final String theValue, final boolean exportable, final boolean prefix) {
        value = theValue;
        isExportImportable = exportable;
        isPrefix = prefix;

        synchronized (declaredKeys) {
            declaredKeys.add(this);
        }
    }

    @NonNull
    public String value() {return value;}
    public boolean IsPrefix() {return isPrefix;}
    public boolean ExportImportable() {return isExportImportable;}

    @NonNull
    public static String key(@NonNull final String value) {
        return new PrefKey(value, false, false).value;
    }

    @NonNull
    public static String prefixedKey(@NonNull final String value) {
        return new PrefKey(value, false, true).value;
    }

    @NonNull
    public static String exportableKey(@NonNull final String value) {
        return new PrefKey(value, true, false).value;
    }

    @NonNull
    public static String exportablePrefixedKey(@NonNull final String value) {
        return new PrefKey(value, true, true).value;
    }

    @NonNull
    public static Set<PrefKey> getKeys(@Nullable final Predicate<PrefKey> fieldFilter) {
        synchronized (declaredKeys) {
            if (fieldFilter == null) {
                return Collections.unmodifiableSet(declaredKeys);
            } else {
                return declaredKeys.stream().filter(fieldFilter).collect(Collectors.toUnmodifiableSet());
            }
        }
    }
}
