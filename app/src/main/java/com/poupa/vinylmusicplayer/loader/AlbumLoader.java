package com.poupa.vinylmusicplayer.loader;

import android.content.Context;

import androidx.annotation.NonNull;

import com.poupa.vinylmusicplayer.discog.Discography;
import com.poupa.vinylmusicplayer.discog.ComparatorUtil;
import com.poupa.vinylmusicplayer.discog.StringUtil;
import com.poupa.vinylmusicplayer.helper.SortOrder;
import com.poupa.vinylmusicplayer.model.Album;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.function.Function;

/**
 * @author Karim Abou Zeid (kabouzeid)
 * @author SC (soncaokim)
 */
public class AlbumLoader {
    private final static Discography discography = Discography.getInstance();

    @NonNull
    public static ArrayList<Album> getAllAlbums(@NonNull final Context context) {
        synchronized (discography) {
            ArrayList<Album> albums = new ArrayList<>(discography.getAllAlbums());
            Collections.sort(albums, getSortOrder());
            return albums;
        }
    }

    @NonNull
    public static ArrayList<Album> getAlbums(@NonNull final Context context, String query) {
        final String strippedQuery = StringUtil.stripAccent(query.toLowerCase());

        synchronized (discography) {
            ArrayList<Album> albums = new ArrayList<>();
            for (Album album : discography.getAllAlbums()) {
                final String strippedAlbum = StringUtil.stripAccent(album.getTitle().toLowerCase());
                if (strippedAlbum.contains(strippedQuery)) {
                    albums.add(album);
                }
            }
            Collections.sort(albums, getSortOrder());
            return albums;
        }
    }

    @NonNull
    public static Album getAlbum(@NonNull final Context context, long albumId) {
        synchronized (discography) {
            Album album = discography.getAlbum(albumId);
            if (album != null) {
                return album;
            } else {
                return new Album();
            }
        }
    }

    @NonNull
    private static Comparator<Album> getSortOrder() {
        Function<Album, String> getArtistName = (a) -> a.safeGetFirstSong().artistName;
        Function<Album, String> getAlbumName = (a) -> a.safeGetFirstSong().albumName;

        Comparator<Album> byAlbumName = (a1, a2) -> StringUtil.compareIgnoreAccent(
                getAlbumName.apply(a1),
                getAlbumName.apply(a2));
        Comparator<Album> byArtistName = (a1, a2) -> StringUtil.compareIgnoreAccent(
                getArtistName.apply(a1),
                getArtistName.apply(a2));
        Comparator<Album> byYear = (a1, a2) -> a1.getYear() - a2.getYear();
        Comparator<Album> byDateAdded = (a1, a2) -> ComparatorUtil.compareLongInts(a1.getDateAdded(), a2.getDateAdded());

        switch (PreferenceUtil.getInstance().getAlbumSortOrder()) {
            case SortOrder.AlbumSortOrder.ALBUM_Z_A:
                return ComparatorUtil.chain(ComparatorUtil.reverse(byAlbumName), ComparatorUtil.reverse(byArtistName));
            case SortOrder.AlbumSortOrder.ALBUM_ARTIST:
                return ComparatorUtil.chain(byArtistName, byAlbumName);
            case SortOrder.AlbumSortOrder.ALBUM_YEAR:
                return ComparatorUtil.chain(byYear, byAlbumName);
            case SortOrder.AlbumSortOrder.ALBUM_DATE_ADDED:
                return ComparatorUtil.chain(ComparatorUtil.reverse(byDateAdded), byAlbumName);

            case SortOrder.AlbumSortOrder.ALBUM_A_Z:
            default:
                return ComparatorUtil.chain(byAlbumName, byArtistName);
        }
    }
}
