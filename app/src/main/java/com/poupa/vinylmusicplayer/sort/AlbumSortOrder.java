package com.poupa.vinylmusicplayer.sort;

import android.provider.MediaStore;
import android.view.SubMenu;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.model.Album;
import com.poupa.vinylmusicplayer.util.ComparatorUtil;
import com.poupa.vinylmusicplayer.util.MusicUtil;
import com.poupa.vinylmusicplayer.util.StringUtil;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @author SC (soncaokim)
 */
public class AlbumSortOrder {
    private static final Comparator<Album> _BY_ALBUM_NAME = (a1, a2) -> StringUtil.compareIgnoreAccent(
            a1.safeGetFirstSong().albumName,
            a2.safeGetFirstSong().albumName);
    private static final Comparator<Album> _BY_ARTIST_NAME = (a1, a2) -> StringUtil.compareIgnoreAccent(
            a1.getArtistName(),
            a2.getArtistName());
    private static final Comparator<Album> _BY_DATE_ADDED = Comparator.comparingLong(Album::getDateAdded);
    private static final Comparator<Album> _BY_DATE_MODIFIED = Comparator.comparingLong(Album::getDateModified);
    private static final Comparator<Album> _BY_YEAR = Comparator.comparingInt(Album::getYear);

    private static final Comparator<Album> BY_ALBUM = ComparatorUtil.chain(_BY_ALBUM_NAME, _BY_ARTIST_NAME);
    private static final Comparator<Album> BY_ALBUM_DESC = ComparatorUtil.chain(ComparatorUtil.reverse(_BY_ALBUM_NAME), _BY_ARTIST_NAME);
    private static final Comparator<Album> BY_ARTIST = ComparatorUtil.chain(_BY_ARTIST_NAME, _BY_ALBUM_NAME);
    private static final Comparator<Album> BY_DATE_ADDED = ComparatorUtil.chain(_BY_DATE_ADDED, _BY_ALBUM_NAME);
    private static final Comparator<Album> BY_DATE_ADDED_DESC = ComparatorUtil.chain(ComparatorUtil.reverse(_BY_DATE_ADDED), _BY_ALBUM_NAME);
    private static final Comparator<Album> BY_DATE_MODIFIED = ComparatorUtil.chain(_BY_DATE_MODIFIED, _BY_ALBUM_NAME);
    private static final Comparator<Album> BY_DATE_MODIFIED_DESC = ComparatorUtil.chain(ComparatorUtil.reverse(_BY_DATE_MODIFIED), _BY_ALBUM_NAME);
    private static final Comparator<Album> BY_YEAR = ComparatorUtil.chain(_BY_YEAR, _BY_ALBUM_NAME);
    public static final Comparator<Album> BY_YEAR_DESC = ComparatorUtil.chain(ComparatorUtil.reverse(_BY_YEAR), _BY_ALBUM_NAME);

    private static final List<SortOrder<Album>> SUPPORTED_ORDERS = Arrays.asList(
            Utils.build(
                    MediaStore.Audio.Artists.DEFAULT_SORT_ORDER + ", " + MediaStore.Audio.Albums.DEFAULT_SORT_ORDER,
                    album -> Utils.getSectionName(album.getArtistName()),
                    BY_ARTIST,
                    R.id.action_album_sort_order_artist,
                    R.string.sort_order_artist
            ),
            Utils.build(
                    MediaStore.Audio.Albums.DEFAULT_SORT_ORDER,
                    album -> Utils.getSectionName(album.getTitle()),
                    BY_ALBUM,
                    R.id.action_album_sort_order_name,
                    R.string.sort_order_name
            ),
            Utils.build(
                    MediaStore.Audio.Albums.DEFAULT_SORT_ORDER + " DESC",
                    album -> Utils.getSectionName(album.getTitle()),
                    BY_ALBUM_DESC,
                    R.id.action_album_sort_order_name_reverse,
                    R.string.sort_order_name_reverse
            ),
            Utils.build(
                    MediaStore.Audio.Media.YEAR,
                    album -> MusicUtil.getYearString(album.getYear()),
                    BY_YEAR,
                    R.id.action_album_sort_order_year,
                    R.string.sort_order_year
            ),
            Utils.build(
                    MediaStore.Audio.Media.YEAR + " DESC",
                    artist -> MusicUtil.getYearString(artist.getYear()),
                    BY_YEAR_DESC,
                    R.id.action_album_sort_order_year_reverse,
                    R.string.sort_order_year_reverse
            ),
            Utils.build(
                    MediaStore.Audio.Media.DATE_ADDED,
                    album -> Utils.getSectionName(album.getDateAdded()),
                    BY_DATE_ADDED,
                    R.id.action_album_sort_order_date_added,
                    R.string.sort_order_date_added
            ),
            Utils.build(
                    MediaStore.Audio.Media.DATE_ADDED + " DESC",
                    artist -> Utils.getSectionName(artist.getDateAdded()),
                    BY_DATE_ADDED_DESC,
                    R.id.action_album_sort_order_date_added_reverse,
                    R.string.sort_order_date_added_reverse
            ),
            Utils.build(
                    MediaStore.Audio.Media.DATE_MODIFIED,
                    album -> Utils.getSectionName(album.getDateModified()),
                    BY_DATE_MODIFIED,
                    R.id.action_album_sort_order_date_modified,
                    R.string.sort_order_date_modified
            ),
            Utils.build(
                    MediaStore.Audio.Media.DATE_MODIFIED + " DESC",
                    artist -> Utils.getSectionName(artist.getDateModified()),
                    BY_DATE_MODIFIED_DESC,
                    R.id.action_album_sort_order_date_modified_reverse,
                    R.string.sort_order_date_modified_reverse
            )
    );

    public static @NonNull
    SortOrder<Album> fromPreference(@NonNull String preferenceValue) {
        SortOrder<Album> match = Utils.collectionSearch(SUPPORTED_ORDERS, preferenceValue, x -> x.preferenceValue);
        if (match == null) {
            match = SUPPORTED_ORDERS.get(0);
        }
        return match;
    }

    public static @Nullable
    SortOrder<Album> fromMenuResourceId(@IdRes int id) {
        // Attn: Dont provide fallback default value
        // this function can be called with an alien menu res ID, and in such case it should return null value
        return Utils.collectionSearch(SUPPORTED_ORDERS, id, x -> x.menuResourceId);
    }

    public static void buildMenu(@NonNull SubMenu sortOrderMenu, String currentPreferenceValue) {
        Utils.buildMenu(SUPPORTED_ORDERS, sortOrderMenu, currentPreferenceValue);
    }
}
