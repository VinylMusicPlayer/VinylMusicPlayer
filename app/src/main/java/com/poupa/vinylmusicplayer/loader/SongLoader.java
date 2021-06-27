package com.poupa.vinylmusicplayer.loader;

import androidx.annotation.NonNull;

import com.poupa.vinylmusicplayer.discog.Discography;
import com.poupa.vinylmusicplayer.helper.SortOrder;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;
import com.poupa.vinylmusicplayer.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * @author Karim Abou Zeid (kabouzeid)
 * @author SC (soncaokim)
 */
public class SongLoader {
    @NonNull
    public static ArrayList<Song> getAllSongs() {
        ArrayList<Song> songs = Discography.getInstance().getAllSongs();
        Collections.sort(songs, getSortOrder());
        return songs;
    }

    @NonNull
    public static ArrayList<Song> getSongs(@NonNull final String query) {
        final String strippedQuery = StringUtil.stripAccent(query.toLowerCase());

        ArrayList<Song> songs = new ArrayList<>();
        for (Song song : Discography.getInstance().getAllSongs()) {
            final String strippedTitle = StringUtil.stripAccent(song.title.toLowerCase());
            if (strippedTitle.contains(strippedQuery)) {
                songs.add(song);
            }
        }
        Collections.sort(songs, getSortOrder());
        return songs;
    }

    @NonNull
    public static Comparator<Song> getSortOrder() {
        SortOrder.Base<Song> sortOrder = SortOrder.BySong.fromPreference(PreferenceUtil.getInstance().getSongSortOrder());
        return sortOrder.comparator;
    }
}
