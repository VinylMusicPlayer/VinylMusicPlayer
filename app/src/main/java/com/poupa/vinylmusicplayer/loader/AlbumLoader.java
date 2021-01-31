package com.poupa.vinylmusicplayer.loader;

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
    @NonNull
    public static ArrayList<Album> getAllAlbums() {
        ArrayList<Album> albums = new ArrayList<>(Discography.getInstance().getAllAlbums());
        Collections.sort(albums, getSortOrder());
        return albums;
    }

    @NonNull
    public static ArrayList<Album> getAlbums(String query) {
        final String strippedQuery = StringUtil.stripAccent(query.toLowerCase());

        ArrayList<Album> albums = new ArrayList<>();
        for (Album album : Discography.getInstance().getAllAlbums()) {
            final String strippedAlbum = StringUtil.stripAccent(album.getTitle().toLowerCase());
            if (strippedAlbum.contains(strippedQuery)) {
                albums.add(album);
            }
        }
        Collections.sort(albums, getSortOrder());
        return albums;
    }

    @NonNull
    public static Album getAlbum(long albumId) {
        Album album = Discography.getInstance().getAlbum(albumId);
        if (album != null) {
            return album;
        } else {
            return new Album();
        }
    }

    @NonNull
    private static Comparator<Album> getSortOrder() {
        Function<Album, String> getAlbumName = (a) -> a.safeGetFirstSong().albumName;

        Comparator<Album> byAlbumName = (a1, a2) -> StringUtil.compareIgnoreAccent(
                getAlbumName.apply(a1),
                getAlbumName.apply(a2));
        Comparator<Album> byArtistName = (a1, a2) -> StringUtil.compareIgnoreAccent(
                a1.getArtistName(),
                a2.getArtistName());
        Comparator<Album> byYearDesc = (a1, a2) -> a2.getYear() - a1.getYear();
        Comparator<Album> byDateAddedDesc = (a1, a2) -> ComparatorUtil.compareLongInts(a2.getDateAdded(), a1.getDateAdded());

        switch (PreferenceUtil.getInstance().getAlbumSortOrder()) {
            case SortOrder.AlbumSortOrder.ALBUM_Z_A:
                return ComparatorUtil.chain(ComparatorUtil.reverse(byAlbumName), ComparatorUtil.reverse(byArtistName));
            case SortOrder.AlbumSortOrder.ALBUM_ARTIST:
                return ComparatorUtil.chain(byArtistName, byAlbumName);
            case SortOrder.AlbumSortOrder.ALBUM_YEAR_REVERSE:
                return ComparatorUtil.chain(byYearDesc, byAlbumName);
            case SortOrder.AlbumSortOrder.ALBUM_DATE_ADDED_REVERSE:
                return ComparatorUtil.chain(byDateAddedDesc, byAlbumName);

            case SortOrder.AlbumSortOrder.ALBUM_A_Z:
            default:
                return ComparatorUtil.chain(byAlbumName, byArtistName);
        }
    }
}
