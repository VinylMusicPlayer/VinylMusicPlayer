package com.poupa.vinylmusicplayer.loader;

import android.content.Context;

import androidx.annotation.NonNull;

import com.poupa.vinylmusicplayer.helper.SortOrder;
import com.poupa.vinylmusicplayer.model.Artist;
import com.poupa.vinylmusicplayer.discog.Discography;
import com.poupa.vinylmusicplayer.discog.ComparatorUtil;
import com.poupa.vinylmusicplayer.discog.StringUtil;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * @author Karim Abou Zeid (kabouzeid)
 * @author SC (soncaokim)
 */
public class ArtistLoader {
    private final static Discography discography = Discography.getInstance();

    @NonNull
    public static ArrayList<Artist> getAllArtists() {
        synchronized (discography) {
            ArrayList<Artist> artists = new ArrayList<>(discography.getAllArtists());
            Collections.sort(artists, getSortOrder());
            return artists;
        }
    }

    @NonNull
    public static ArrayList<Artist> getArtists(String query) {
        final String strippedQuery = StringUtil.stripAccent(query.toLowerCase());

        synchronized (discography) {
            ArrayList<Artist> artists = new ArrayList<>();
            for (Artist artist : discography.getAllArtists()) {
                final String strippedArtist = StringUtil.stripAccent(artist.getName().toLowerCase());
                if (strippedArtist.contains(strippedQuery)) {
                    artists.add(artist);
                }
            }
            Collections.sort(artists, getSortOrder());
            return artists;
        }
    }

    @NonNull
    public static Artist getArtist(long artistId) {
        synchronized (discography) {
            Artist artist = discography.getArtist(artistId);
            if (artist != null) {
                return artist;
            } else {
                return Artist.EMPTY;
            }
        }
    }

    @NonNull
    private static Comparator<Artist> getSortOrder() {
        Comparator<Artist> byArtistName = (a1, a2) -> StringUtil.compareIgnoreAccent(a1.name, a2.name);

        switch (PreferenceUtil.getInstance().getArtistSortOrder()) {
            case SortOrder.ArtistSortOrder.ARTIST_Z_A:
                return ComparatorUtil.reverse(byArtistName);

            case SortOrder.ArtistSortOrder.ARTIST_A_Z:
            default:
                return byArtistName;
        }
    }
}
