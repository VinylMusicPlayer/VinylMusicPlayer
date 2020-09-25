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
import java.util.function.Function;

/**
 * @author Karim Abou Zeid (kabouzeid)
 * @author SC (soncaokim)
 */
public class ArtistLoader {
    @NonNull
    public static ArrayList<Artist> getAllArtists(@NonNull final Context context) {
        Discography discog = Discography.getInstance();
        synchronized (discog) {
            ArrayList<Artist> artists = new ArrayList<>(discog.getAllArtists());
            Collections.sort(artists, getSortOrder());
            return artists;
        }
    }

    @NonNull
    public static ArrayList<Artist> getArtists(@NonNull final Context context, String query) {
        final String strippedQuery = StringUtil.stripAccent(query.toLowerCase());

        Discography discog = Discography.getInstance();
        synchronized (discog) {
            ArrayList<Artist> artists = new ArrayList<>();
            for (Artist artist : discog.getAllArtists()) {
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
    public static Artist getArtist(@NonNull final Context context, long artistId) {
        Discography discog = Discography.getInstance();
        synchronized (discog) {
            Artist artist = discog.getArtist(artistId);
            if (artist != null) {
                return artist;
            } else {
                return new Artist();
            }
        }
    }

    @NonNull
    private static Comparator<Artist> getSortOrder() {
        Function<Artist, String> getArtistName = (a) -> a.safeGetFirstAlbum().safeGetFirstSong().artistName;
        Comparator<Artist> byArtistName = (a1, a2) -> StringUtil.compareIgnoreAccent(
                getArtistName.apply(a1),
                getArtistName.apply(a2));

        switch (PreferenceUtil.getInstance().getArtistSortOrder()) {
            case SortOrder.ArtistSortOrder.ARTIST_Z_A:
                return ComparatorUtil.reverse(byArtistName);

            case SortOrder.ArtistSortOrder.ARTIST_A_Z:
            default:
                return byArtistName;
        }
    }
}
