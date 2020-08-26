package com.poupa.vinylmusicplayer.loader;

import android.content.Context;

import androidx.annotation.NonNull;

import com.poupa.vinylmusicplayer.helper.SortOrder;
import com.poupa.vinylmusicplayer.model.Album;
import com.poupa.vinylmusicplayer.provider.Discography;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class AlbumLoader {
    @NonNull
    public static ArrayList<Album> getAllAlbums(@NonNull final Context context) {
        Discography discog = Discography.getInstance();
        synchronized (discog) {
            ArrayList<Album> albums = new ArrayList<>(discog.getAllAlbums());
            Collections.sort(albums, getSortOrder());
            return albums;
        }
    }

    @NonNull
    public static ArrayList<Album> getAlbums(@NonNull final Context context, String query) {
        Discography discog = Discography.getInstance();
        synchronized (discog) {
            ArrayList<Album> albums = new ArrayList<>();
            for (Album album : discog.getAllAlbums()) {
                // TODO Case/accent insensitive comparison
                if (album.getTitle().contains(query)) {
                    albums.add(album);
                }
            }
            Collections.sort(albums, getSortOrder());
            return albums;
        }
    }

    public static Album getOrCreateAlbum(ArrayList<Album> albums, long albumId) {
        // TODO Avoid sequential search here
        for (Album album : albums) {
            if (!album.songs.isEmpty() && album.songs.get(0).albumId == albumId) {
                return album;
            }
        }
        return new Album();
    }

    @NonNull
    private static Comparator<Album> getSortOrder() {
        final Collator collator = Collator.getInstance();

        Comparator<Album> byAlbumName = (a1, a2) -> collator.compare(
                a1.safeGetFirstSong().albumName,
                a2.safeGetFirstSong().albumName);
        Comparator<Album> byArtistName = (a1, a2) -> collator.compare(
                a1.safeGetFirstSong().artistName,
                a2.safeGetFirstSong().artistName);
        Comparator<Album> byYear = (a1, a2) -> a1.getYear() - a2.getYear();
        Comparator<Album> byDateAdded = (a1, a2) -> {
            long diff = a1.getDateAdded() - a2.getDateAdded();

            if (diff < Integer.MIN_VALUE) {diff = Integer.MIN_VALUE;}
            if (diff > Integer.MAX_VALUE) {diff = Integer.MAX_VALUE;}
            return (int)diff;
        };

        BiFunction<Comparator<Album>, Comparator<Album>, Comparator<Album>> chain =
                (c1, c2) -> (Comparator<Album>) (a1, a2) -> {
                        final int diff = c1.compare(a1, a2);
                        return (diff != 0) ? diff : c2.compare(a1, a2);
                };
        Function<Comparator<Album>, Comparator<Album>> inverse =
                (c) -> (Comparator<Album>) (a1, a2) -> c.compare(a2, a1);

        switch (PreferenceUtil.getInstance().getAlbumSortOrder()) {
            case SortOrder.AlbumSortOrder.ALBUM_Z_A:
                return chain.apply(inverse.apply(byAlbumName), inverse.apply(byArtistName));
            case SortOrder.AlbumSortOrder.ALBUM_ARTIST:
                return chain.apply(byArtistName, byAlbumName);
            case SortOrder.AlbumSortOrder.ALBUM_YEAR:
                return chain.apply(byYear, byAlbumName);
            case SortOrder.AlbumSortOrder.ALBUM_DATE_ADDED:
                return chain.apply(inverse.apply(byDateAdded), byAlbumName);

            case SortOrder.AlbumSortOrder.ALBUM_A_Z:
            default:
                return chain.apply(byAlbumName, byArtistName);
        }
    }
}
