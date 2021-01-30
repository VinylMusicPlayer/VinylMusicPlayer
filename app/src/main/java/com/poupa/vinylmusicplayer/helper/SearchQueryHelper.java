package com.poupa.vinylmusicplayer.helper;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.annotation.NonNull;

import com.poupa.vinylmusicplayer.discog.Discography;
import com.poupa.vinylmusicplayer.discog.StringUtil;
import com.poupa.vinylmusicplayer.loader.SongLoader;
import com.poupa.vinylmusicplayer.model.Song;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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

        ArrayList<Song> matchingSongs = new ArrayList<>();
        Collection<Song> allSongs = Discography.getInstance().getAllSongs();

        for (Song song : allSongs) {
            if (isMatchingTitle.test(song) || isMatchingAlbum.test(song) || isMatchingArtist.test(song) || isMatchingQuery.test(song)) {
                matchingSongs.add(song);
            }
        }
        Collections.sort(matchingSongs, SongLoader.getSortOrder());
        return matchingSongs;
    }
}
