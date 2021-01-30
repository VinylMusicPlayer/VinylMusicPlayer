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

/**
 * @author Karim Abou Zeid (kabouzeid)
 * @author SC (soncaokim)
 */
public class AlbumLoader {
    public final static Comparator<Album> BY_ALBUM_NAME = (a1, a2) -> StringUtil.compareIgnoreAccent(
            a1.safeGetFirstSong().albumName,
            a2.safeGetFirstSong().albumName);
    public final static Comparator<Album> BY_ARTIST_NAME = (a1, a2) -> StringUtil.compareIgnoreAccent(
            a1.getArtistName(),
            a2.getArtistName());
    public final static Comparator<Album> BY_YEAR_DESC = (a1, a2) -> a2.getYear() - a1.getYear();
    public final static Comparator<Album> BY_DATE_ADDED_DESC = (a1, a2) -> ComparatorUtil.compareLongInts(a2.getDateAdded(), a1.getDateAdded());

    private final static Discography discography = Discography.getInstance();

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
        switch (PreferenceUtil.getInstance().getAlbumSortOrder()) {
            case SortOrder.AlbumSortOrder.ALBUM_Z_A:
                return ComparatorUtil.chain(ComparatorUtil.reverse(BY_ALBUM_NAME), ComparatorUtil.reverse(BY_ARTIST_NAME));
            case SortOrder.AlbumSortOrder.ALBUM_ARTIST:
                return ComparatorUtil.chain(BY_ARTIST_NAME, BY_ALBUM_NAME);
            case SortOrder.AlbumSortOrder.ALBUM_YEAR_REVERSE:
                return ComparatorUtil.chain(BY_YEAR_DESC, BY_ALBUM_NAME);
            case SortOrder.AlbumSortOrder.ALBUM_DATE_ADDED_REVERSE:
                return ComparatorUtil.chain(BY_DATE_ADDED_DESC, BY_ALBUM_NAME);

            case SortOrder.AlbumSortOrder.ALBUM_A_Z:
            default:
                return ComparatorUtil.chain(BY_ALBUM_NAME, BY_ARTIST_NAME);
        }
    }
}
