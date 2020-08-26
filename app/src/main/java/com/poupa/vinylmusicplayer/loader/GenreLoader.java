package com.poupa.vinylmusicplayer.loader;

import android.content.Context;

import androidx.annotation.NonNull;

import com.poupa.vinylmusicplayer.model.Genre;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.provider.Discography;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class GenreLoader {

    @NonNull
    public static ArrayList<Genre> getAllGenres(@NonNull final Context context) {
        Discography discog = Discography.getInstance();
        synchronized (discog) {
            ArrayList<Genre> genres = new ArrayList<>(discog.getAllGenres());
            final Collator collator = Collator.getInstance();
            Collections.sort(genres,
                    (g1, g2) -> {
                        if (g1.name == null) {return g2.name == null ? 0 : -1;}
                        else if (g2.name == null) {return 1;}

                        return collator.compare(g1.name, g2.name);
                    });
            return genres;
        }
    }

    @NonNull
    public static ArrayList<Song> getSongs(@NonNull final Context context, final long genreId) {
        Discography discog = Discography.getInstance();
        synchronized (discog) {
            Collection<Song> genreSongs = discog.getSongsForGenre(genreId);
            if (genreSongs == null) {
                return new ArrayList<>();
            }
            else {
                ArrayList<Song> songs = new ArrayList<>(genreSongs);

                // TODO Do we need other sorting option here?
                final Collator collator = Collator.getInstance();
                Collections.sort(songs,
                        (s1, s2) -> {
                            if (s1.title == null) {return s2.title == null ? 0 : -1;}
                            else if (s2.title == null) {return 1;}

                            return collator.compare(s1.title, s2.title);
                        });

                return songs;
            }
        }
    }
}
