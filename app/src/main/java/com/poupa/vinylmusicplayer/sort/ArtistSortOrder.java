package com.poupa.vinylmusicplayer.sort;

import android.provider.MediaStore;
import android.view.SubMenu;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.model.Artist;
import com.poupa.vinylmusicplayer.util.ComparatorUtil;
import com.poupa.vinylmusicplayer.util.StringUtil;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @author SC (soncaokim)
 */
public class ArtistSortOrder {
    private static final Comparator<Artist> BY_ARTIST = (a1, a2) -> StringUtil.compareIgnoreAccent(a1.name, a2.name);
    private static final Comparator<Artist> BY_DATE_MODIFIED = Comparator.comparingLong(Artist::getDateModified);

    private static final List<SortOrder<Artist>> SUPPORTED_ORDERS = Arrays.asList(
            Utils.build(
                    MediaStore.Audio.Artists.DEFAULT_SORT_ORDER,
                    artist -> Utils.getSectionName(artist.getName()),
                    BY_ARTIST,
                    R.id.action_artist_sort_order_name,
                    R.string.sort_order_name
            ),
            Utils.build(
                    MediaStore.Audio.Artists.DEFAULT_SORT_ORDER + " DESC",
                    artist -> Utils.getSectionName(artist.getName()),
                    ComparatorUtil.reverse(BY_ARTIST),
                    R.id.action_artist_sort_order_name_reverse,
                    R.string.sort_order_name_reverse
            ),
            Utils.build(
                    MediaStore.Audio.Media.DATE_MODIFIED,
                    artist -> Utils.getSectionName(artist.getDateModified()),
                    BY_DATE_MODIFIED,
                    R.id.action_artist_sort_order_date_modified,
                    R.string.sort_order_date_modified
            ),
            Utils.build(
                    MediaStore.Audio.Media.DATE_MODIFIED + " DESC",
                    artist -> Utils.getSectionName(artist.getDateModified()),
                    ComparatorUtil.reverse(BY_DATE_MODIFIED),
                    R.id.action_artist_sort_order_date_modified_reverse,
                    R.string.sort_order_date_modified_reverse
            )
    );

    public static @NonNull
    SortOrder<Artist> fromPreference(@NonNull String preferenceValue) {
        SortOrder<Artist> match = Utils.collectionSearch(SUPPORTED_ORDERS, preferenceValue, x -> x.preferenceValue);
        if (match == null) {
            match = SUPPORTED_ORDERS.get(0);
        }
        return match;
    }

    public static @Nullable
    SortOrder<Artist> fromMenuResourceId(@IdRes int id) {
        // Attn: Dont provide fallback default value
        // this function can be called with an alien menu res ID, and in such case it should return null value
        return Utils.collectionSearch(SUPPORTED_ORDERS, id, x -> x.menuResourceId);
    }

    public static void buildMenu(@NonNull SubMenu sortOrderMenu, String currentPreferenceValue) {
        Utils.buildMenu(SUPPORTED_ORDERS, sortOrderMenu, currentPreferenceValue);
    }
}
