package com.poupa.vinylmusicplayer.helper;

import android.app.SearchManager;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.poupa.vinylmusicplayer.discog.Discography;
import com.poupa.vinylmusicplayer.loader.GenreLoader;
import com.poupa.vinylmusicplayer.loader.PlaylistSongLoader;
import com.poupa.vinylmusicplayer.loader.SongLoader;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author Karim Abou Zeid (kabouzeid)
 * @author SC (soncaokim)
 */
public class SearchQueryHelper {
    @NonNull
    public static ArrayList<Song> getSongs(@NonNull final Bundle extras) {
        Function<String, String> normalize = (s) -> StringUtil.stripAccent(s).toLowerCase();

        final String query = normalize.apply(extras.getString(SearchManager.QUERY, ""));
        final String artistName = normalize.apply(extras.getString(MediaStore.EXTRA_MEDIA_ARTIST, ""));
        final String albumName = normalize.apply(extras.getString(MediaStore.EXTRA_MEDIA_ALBUM, ""));
        final String title = normalize.apply(extras.getString(MediaStore.EXTRA_MEDIA_TITLE, ""));

        Predicate<Song> isMatchingArtist = (s) -> {
            BiPredicate<String, List<String>> isXinY = (x, y) -> {
                if (x.isEmpty()) return false;

                for (String yIterator : y) {
                    if (normalize.apply(yIterator).contains(x)) return true;
                }
                return false;
            };
            return isXinY.test(artistName, s.albumArtistNames) || isXinY.test(artistName, s.artistNames);
        };
        Predicate<Song> isMatchingAlbum = (s) -> (!albumName.isEmpty() && normalize.apply(s.albumName).contains(albumName));
        Predicate<Song> isMatchingTitle = (s) -> (!title.isEmpty() && normalize.apply(s.title).contains(title));
        Predicate<Song> isMatchingQuery= (s) -> (!query.isEmpty() && normalize.apply(s.title).contains(query));

        final ArrayList<Song> allSongs = Discography.getInstance().getAllSongs(SongLoader.getSortOrder());
        // Match empty intent to all songs
        if(query.isEmpty() && artistName.isEmpty() && albumName.isEmpty() && title.isEmpty()) {
            return allSongs;
        }

        ArrayList<Song> matchingSongs = new ArrayList<>();
        for (Song song : allSongs) {
            if (isMatchingTitle.test(song) || isMatchingAlbum.test(song) || isMatchingArtist.test(song) || isMatchingQuery.test(song)) {
                matchingSongs.add(song);
            }
        }
        return matchingSongs;
    }

    @NonNull
    public static List<? extends Song> getSongs(
            @Nullable final String focus,
            @NonNull final Bundle extras) {
        // First try known search metrics Genre and Playlist
        if (focus != null) {
            if (focus.equals(MediaStore.Audio.Playlists.ENTRY_CONTENT_TYPE)) {
                // Ignore Android L deprecation by using direct constant as recommended by
                // https://developer.android.com/guide/components/intents-common
                final String playlist = extras
                        .getString("android.intent.extra.playlist");
                if (playlist != null) {
                    return PlaylistSongLoader.getPlaylistSongList(playlist);
                }
            } else if (focus.equals(MediaStore.Audio.Genres.ENTRY_CONTENT_TYPE)) {
                // Ignore Android L deprecation by using direct constant as recommended by
                // https://developer.android.com/guide/components/intents-common
                final String genre = extras
                        .getString("android.intent.extra.genre");
                if (genre != null) {
                    return GenreLoader.getGenreSongsByName(genre);
                }
            }
        }
        // Otherwise do a generic match on all known songs on the phone
        return getSongs(extras);
    }
}
