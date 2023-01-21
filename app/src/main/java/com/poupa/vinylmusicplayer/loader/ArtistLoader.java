package com.poupa.vinylmusicplayer.loader;

import androidx.annotation.NonNull;

import com.poupa.vinylmusicplayer.discog.Discography;
import com.poupa.vinylmusicplayer.model.Artist;
import com.poupa.vinylmusicplayer.sort.ArtistSortOrder;
import com.poupa.vinylmusicplayer.sort.SortOrder;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;
import com.poupa.vinylmusicplayer.util.StringUtil;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * @author Karim Abou Zeid (kabouzeid)
 * @author SC (soncaokim)
 */
public class ArtistLoader {
    @NonNull
    public static ArrayList<Artist> getAllArtists() {
        return Discography.getInstance().getAllArtists(getSortOrder());
    }

    @NonNull
    public static ArrayList<Artist> getArtists(String query) {
        final String strippedQuery = StringUtil.stripAccent(query.toLowerCase());

        ArrayList<Artist> artists = new ArrayList<>();
        for (Artist artist : Discography.getInstance().getAllArtists(getSortOrder())) {
            final String strippedArtist = StringUtil.stripAccent(artist.getName().toLowerCase());
            if (strippedArtist.contains(strippedQuery)) {
                artists.add(artist);
            }
        }
        return artists;
    }

    @NonNull
    public static Artist getArtist(long artistId) {
        Artist artist = Discography.getInstance().getArtist(artistId);
        if (artist != null) {
            return artist;
        } else {
            return Artist.EMPTY;
        }
    }

    @NonNull
    private static Comparator<Artist> getSortOrder() {
        SortOrder<Artist> sortOrder = ArtistSortOrder.fromPreference(PreferenceUtil.getInstance().getArtistSortOrder());
        return sortOrder.comparator;
    }
}
