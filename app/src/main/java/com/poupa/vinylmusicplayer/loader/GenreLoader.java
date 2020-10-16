package com.poupa.vinylmusicplayer.loader;

import android.content.Context;

import androidx.annotation.NonNull;

import com.poupa.vinylmusicplayer.discog.Discography;
import com.poupa.vinylmusicplayer.discog.StringUtil;
import com.poupa.vinylmusicplayer.model.Genre;
import com.poupa.vinylmusicplayer.model.Song;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class GenreLoader {
    private final static Discography discography = Discography.getInstance();

    @NonNull
    public static ArrayList<Genre> getAllGenres(@NonNull final Context context) {
        synchronized (discography) {
            ArrayList<Genre> genres = new ArrayList<>(discography.getAllGenres());
            Collections.sort(genres, (g1, g2) -> StringUtil.compareIgnoreAccent(g1.name, g2.name));
            return genres;
        }
    }

    @NonNull
    public static ArrayList<Song> getSongs(@NonNull final Context context, final long genreId) {
        synchronized (discography) {
            Collection<Song> genreSongs = discography.getSongsForGenre(genreId);
            if (genreSongs == null) {
                return new ArrayList<>();
            }
            else {
                ArrayList<Song> songs = new ArrayList<>(genreSongs);
                Collections.sort(songs, (s1, s2) -> StringUtil.compareIgnoreAccent(s1.title, s2.title));
                return songs;
            }
        }
    }
}
