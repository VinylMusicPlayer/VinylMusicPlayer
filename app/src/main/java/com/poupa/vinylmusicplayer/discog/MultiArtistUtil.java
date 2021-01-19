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

public class MultiArtistUtil {
    private static final String SINGLE_LINE_SEPARATOR = ";";
    // TODO private static final String MULTI_LINE_SEPARATOR = "\n";

    @NonNull
    public static List<String> artistNamesSplit(@NonNull final String names) {
        // TODO Proceed to extract multiple values from a tag instead of text parsing here
        List<String> namesSplit = Arrays.asList(names.split(SINGLE_LINE_SEPARATOR));
        ArrayList<String> result = new ArrayList<>();
        for (String name : namesSplit) {
            result.add(name.trim());
        }
        return result;
    }

    @NonNull
    public static String artistNamesMerge(@NonNull final List<String> names) {
        if (names.size() == 0) {return Artist.UNKNOWN_ARTIST_DISPLAY_NAME;}
        return MusicUtil.buildInfoString(
                SINGLE_LINE_SEPARATOR + " ",
                names.toArray(new String[0]));
    }

    @NonNull
    public static Set<String> artistNamesMerge(@NonNull final List<String> names1, @NonNull final List<String> names2) {
        Set<String> names = new HashSet<>();
        names.addAll(names1);
        names.addAll(names2);
        if (names.size() > 1) {
            // after merging two artists list, one may be empty
            // and we end up with a list containing empty element
            // remove it if that's the case
            names.remove("");
        }
        return names;
    }
}
