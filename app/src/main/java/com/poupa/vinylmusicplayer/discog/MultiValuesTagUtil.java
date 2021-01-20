package com.poupa.vinylmusicplayer.discog;

import androidx.annotation.NonNull;

import com.poupa.vinylmusicplayer.model.Artist;
import com.poupa.vinylmusicplayer.util.MusicUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author SC (soncaokim)
 */

public class MultiValuesTagUtil {
    private static final String SINGLE_LINE_SEPARATOR = ";";
    private static final String INFO_STRING_SEPARATOR = "; ";

    @NonNull
    public static List<String> split(@NonNull final String names) {
        List<String> namesSplit = Arrays.asList(names.split(SINGLE_LINE_SEPARATOR));
        ArrayList<String> result = new ArrayList<>();
        for (String name : namesSplit) {
            result.add(name.trim());
        }
        return result;
    }

    @NonNull
    public static String merge(@NonNull final List<String> names) {
        if (names.size() == 0) {return "";}
        return MusicUtil.buildInfoString(SINGLE_LINE_SEPARATOR,
                names.toArray(new String[0]));
    }

    @NonNull
    public static List<String> splitIfNeeded(@NonNull final List<String> names) {
        // If the argument has multiple elements, or empty, dont split further
        if (names.size() != 1) {return names;}

        return MultiValuesTagUtil.split(names.get(0));
    }

    @NonNull
    public static String infoString(@NonNull final List<String> names) {
        if (names.size() == 0) {return Artist.UNKNOWN_ARTIST_DISPLAY_NAME;}
        return MusicUtil.buildInfoString(INFO_STRING_SEPARATOR,
                names.toArray(new String[0]));
    }
}
