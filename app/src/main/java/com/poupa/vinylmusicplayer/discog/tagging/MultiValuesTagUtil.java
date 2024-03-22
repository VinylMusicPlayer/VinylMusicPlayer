package com.poupa.vinylmusicplayer.discog.tagging;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.poupa.vinylmusicplayer.model.Artist;
import com.poupa.vinylmusicplayer.util.MusicUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author SC (soncaokim)
 */

public class MultiValuesTagUtil {
    private static final String SINGLE_LINE_SEPARATOR = ";";
    private static final String MULTI_LINE_SEPARATOR = "\n";
    private static final String INFO_STRING_SEPARATOR = "; ";

    @NonNull
    public static List<String> split(@Nullable final String names) {
        return MultiValuesTagUtil.splitImpl(names, SINGLE_LINE_SEPARATOR);
    }

    @NonNull
    public static String merge(@NonNull final List<String> names) {
        return MultiValuesTagUtil.mergeImpl(names, SINGLE_LINE_SEPARATOR, "");
    }

    @NonNull
    public static List<String> splitIfNeeded(@NonNull final List<String> names) {
        if (names.isEmpty()) {return new ArrayList<>();}

        // If the argument has multiple elements, or empty, dont split further
        if (names.size() > 1) {return names;}

        return MultiValuesTagUtil.split(names.get(0));
    }

    @NonNull
    public static String infoString(@NonNull final List<String> names) {
        return MultiValuesTagUtil.mergeImpl(names, INFO_STRING_SEPARATOR, Artist.UNKNOWN_ARTIST_DISPLAY_NAME);
    }

    @NonNull
    public static List<String> tagEditorSplit(@Nullable final String names) {
        return MultiValuesTagUtil.splitIfNeeded(MultiValuesTagUtil.splitImpl(names, MULTI_LINE_SEPARATOR));
    }

    @NonNull
    public static String tagEditorMerge(@NonNull final List<String> names) {
        return MultiValuesTagUtil.mergeImpl(MultiValuesTagUtil.splitIfNeeded(names), MULTI_LINE_SEPARATOR, "");
    }

    @NonNull
    private static List<String> splitImpl(@Nullable final String names, @NonNull final String separator) {
        ArrayList<String> result = new ArrayList<>();
        if (!TextUtils.isEmpty(names)) {
            String[] namesSplit = names.split(separator);
            for (String name : namesSplit) {
                result.add(name.trim());
            }
        }
        return result;
    }

    @NonNull
    private static String mergeImpl(@NonNull final List<String> names, @NonNull final String separator, @NonNull final String defaultValue) {
        if (names.isEmpty()) {return defaultValue;}
        return MusicUtil.buildInfoString(separator,
                names.toArray(new String[0]));
    }

}
