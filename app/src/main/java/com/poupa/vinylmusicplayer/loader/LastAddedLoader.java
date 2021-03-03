package com.poupa.vinylmusicplayer.loader;

import androidx.annotation.NonNull;

import com.poupa.vinylmusicplayer.discog.Discography;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;

import java.util.ArrayList;
import java.util.Collections;

public class LastAddedLoader {

    @NonNull
    public static ArrayList<Song> getLastAddedSongs() {
        long cutoff = PreferenceUtil.getInstance().getLastAddedCutoffTimeSecs();

        ArrayList<Song> lastAddedSongs = new ArrayList<>();
        for (Song song : Discography.getInstance().getAllSongs()) {
            if (song.dateAdded > cutoff) {lastAddedSongs.add(song);}
        }

        Collections.sort(lastAddedSongs, SongLoader.BY_DATE_ADDED_DESC);
        return lastAddedSongs;
    }
}
