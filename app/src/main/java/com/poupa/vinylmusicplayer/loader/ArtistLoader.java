package com.poupa.vinylmusicplayer.loader;

import android.content.Context;

import androidx.annotation.NonNull;

import com.poupa.vinylmusicplayer.helper.SortOrder;
import com.poupa.vinylmusicplayer.model.Artist;
import com.poupa.vinylmusicplayer.provider.Discography;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.function.Function;

/**
 * @author Karim Abou Zeid (kabouzeid)
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
        Discography discog = Discography.getInstance();
        synchronized (discog) {
            ArrayList<Artist> artists = new ArrayList<>();
            for (Artist artist : discog.getAllArtists()) {
                // TODO Case/accent insensitive comparison
                if (artist.getName().contains(query)) {
                    artists.add(artist);
                }
            }
            Collections.sort(artists, getSortOrder());
            return artists;
        }
    }

    @NonNull
    public static Artist getArtist(@NonNull final Context context, int artistId) {
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
        final Collator collator = Collator.getInstance();

        Comparator<Artist> byArtistName = (a1, a2) -> collator.compare(
                a1.safeGetFirstAlbum().safeGetFirstSong().artistName,
                a2.safeGetFirstAlbum().safeGetFirstSong().artistName);

        Function<Comparator<Artist>, Comparator<Artist>> inverse =
                (c) -> (Comparator<Artist>) (a1, a2) -> c.compare(a2, a1);

        switch (PreferenceUtil.getInstance().getArtistSortOrder()) {
            case SortOrder.ArtistSortOrder.ARTIST_Z_A:
                return inverse.apply(byArtistName);

            case SortOrder.ArtistSortOrder.ARTIST_A_Z:
            default:
                return byArtistName;
        }
    }
}
