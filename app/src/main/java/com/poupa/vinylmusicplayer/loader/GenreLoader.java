package com.poupa.vinylmusicplayer.loader;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

    /**
     * Gets all songs contained in the *closestMatch* genre to contain the genreNameSearchTerm.
     *
     * Genre name is checked to contain the search term in a case insensitive way.
     *
     * Match closeness defined by StringUtil.closestOfMatches
     *
     * For instance "Punk" might return songs from the genre named "punk rock", but would prefer
     * to use a genre named "punk" if it exists.
     * @param genreNameSearchTerm A partial genre name.
     * @return song list from the found genre by search term
     */
    @NonNull
    public static ArrayList<Song> getGenreSongsByName(final String genreNameSearchTerm) {
        final Genre genre = getGenreByName(genreNameSearchTerm);
        if (genre == null) {
            return new ArrayList<>();
        }
        return getSongs(genre.id);
    }

    @Nullable
    private static Genre getGenreByName(final String genreNameSearchTerm) {
        final String lowercaseSearchTerm = genreNameSearchTerm.toLowerCase();
        final ArrayList<Genre> genres = getAllGenres();
        Genre match = null;
        for(Genre genre : genres) {
            if (genre.name.toLowerCase().contains(lowercaseSearchTerm)) {
                if (match == null) {
                    match = genre;
                } else {
                    match = closerMatch(lowercaseSearchTerm, match, genre);
                }
            }
        }
        return match;
    }

    /**
     * This can be sped up by passing in indexOfs and lowerCaseOfs.
     * Users probably wont complain though, should be fast enough as is.
     */
    @NonNull private static Genre closerMatch(
            @NonNull final String genreNameSearchTerm,
            @NonNull final Genre first,
            @NonNull final Genre second) {
        final StringUtil.ClosestMatch match = StringUtil.closestOfMatches(
                genreNameSearchTerm,
                first.name.toLowerCase(),
                second.name.toLowerCase());
        // if equal, go with first, respect pre established order.
        if (match != StringUtil.ClosestMatch.SECOND) {
            return first;
        } else {
            return second;
        }
    }

}
