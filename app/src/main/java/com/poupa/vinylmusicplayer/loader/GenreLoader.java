package com.poupa.vinylmusicplayer.loader;

import androidx.annotation.NonNull;

import com.poupa.vinylmusicplayer.discog.Discography;
import com.poupa.vinylmusicplayer.model.Genre;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.sort.SongSortOrder;
import com.poupa.vinylmusicplayer.util.StringUtil;

import java.util.ArrayList;

public class GenreLoader {
    @NonNull
    public static ArrayList<Genre> getAllGenres() {
        return Discography.getInstance().getAllGenres(
                (g1, g2) -> StringUtil.compareIgnoreAccent(g1.name, g2.name)
        );
    }

    @NonNull
    public static ArrayList<Song> getSongs(final long genreId) {
        ArrayList<Song> songs = Discography.getInstance().getSongsForGenre(genreId, SongSortOrder.BY_ALBUM);
        if (songs == null) {return new ArrayList<>();}
        return songs;
    }
}
