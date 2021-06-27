package com.poupa.vinylmusicplayer.loader;

import androidx.annotation.NonNull;

import com.poupa.vinylmusicplayer.discog.Discography;
import com.poupa.vinylmusicplayer.util.StringUtil;
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
    @NonNull
    public static ArrayList<Album> getAllAlbums() {
        ArrayList<Album> albums = Discography.getInstance().getAllAlbums();
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
        SortOrder.Base<Album> sortOrder = SortOrder.ByAlbum.fromPreference(PreferenceUtil.getInstance().getAlbumSortOrder());
        return sortOrder.comparator;
    }
}
