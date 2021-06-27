package com.poupa.vinylmusicplayer.helper;

import android.provider.MediaStore;
import android.view.SubMenu;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.model.Artist;
import com.poupa.vinylmusicplayer.util.ComparatorUtil;
import com.poupa.vinylmusicplayer.util.MusicUtil;
import com.poupa.vinylmusicplayer.util.StringUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

import static android.view.Menu.NONE;

/**
 * @author SC (soncaokim)
 */
public final class SortOrder {
    // TODO Rename this class
    public static class Base<T> {
        /** The value saved in the preferences for this sort order */
        public String preferenceValue;

        /** The representative text for this item */
        public Function<T, String> sectionNameBuilder;

        /** The comparator used for this sort order*/
        public Comparator<T> comparator;

        /** the menu resource ID that is assigned to this sort order */
        public @IdRes int menuResourceId;

        /** the menu string ID that is assigned to this sort order */
        public int menuTextId;
    }

    private static class Impl {
        static Base<Artist> build(
                @NonNull String preferenceValue,
                @NonNull Function<Artist, String> sectionNameBuilder,
                @NonNull Comparator<Artist> comparatorFunc,
                @IdRes int menuResourceId,
                @StringRes int menuTextId)
        {
            Base<Artist> result = new Base<>();
            result.preferenceValue = preferenceValue;
            result.sectionNameBuilder = sectionNameBuilder;
            result.comparator = comparatorFunc;
            result.menuResourceId = menuResourceId;
            result.menuTextId = menuTextId;
            return result;
        }

        static @NonNull String getSectionName(long seconds) {
            final Date date = new Date(1000 * seconds);
            final String format = "y";
            return android.text.format.DateFormat.format(format, date).toString();
        }

        static @NonNull String getSectionName(@NonNull String name) {
            // TODO This is too much, can be simplified as: charAt[0].toUpper()
            return MusicUtil.getSectionName(name);
        }

        static <T, U> T collectionSearch(List<T> collection, U searchValue, Function<T, U> valueExtractor) {
            T match = null;
            for (T item : collection) {
                if (valueExtractor.apply(item).equals(searchValue)) {
                    match = item;
                    break;
                }
            }
            return match;
        }


        static <T> void buildMenu(List<Base<T>> collection, @NonNull SubMenu sortOrderMenu, String currentPreferenceValue) {
            for (Base<T> item : collection) {
                sortOrderMenu
                        .add(0, item.menuResourceId, NONE, item.menuTextId)
                        .setChecked(currentPreferenceValue.equals(item.preferenceValue));
            }
        }
    }

    public static abstract class ByArtist {
        private static final List<Base<Artist>> SUPPORTED_ORDERS = new ArrayList<>();
        static {
            SUPPORTED_ORDERS.add(Impl.build(
                    MediaStore.Audio.Artists.DEFAULT_SORT_ORDER,
                    artist -> Impl.getSectionName(artist.getName()),
                    (a1, a2) -> StringUtil.compareIgnoreAccent(a1.name, a2.name),
                    R.id.action_artist_sort_order_name,
                    R.string.sort_order_name
            ));
            SUPPORTED_ORDERS.add(Impl.build(
                    MediaStore.Audio.Artists.DEFAULT_SORT_ORDER + " DESC",
                    artist -> Impl.getSectionName(artist.getName()),
                    (a1, a2) -> StringUtil.compareIgnoreAccent(a2.name, a1.name),
                    R.id.action_artist_sort_order_name_reverse,
                    R.string.sort_order_name_reverse
            ));
            SUPPORTED_ORDERS.add(Impl.build(
                    MediaStore.Audio.Media.DATE_MODIFIED + " DESC",
                    artist -> Impl.getSectionName(artist.getDateModified()),
                    (a1, a2) -> ComparatorUtil.compareLongInts(a2.getDateModified(), a1.getDateModified()),
                    R.id.action_artist_sort_order_date_modified_reverse,
                    R.string.sort_order_date_modified_reverse
            ));
        }

        public static @NonNull Base<Artist> fromPreference(@NonNull String preferenceValue) {
            Base<Artist> match = Impl.collectionSearch(SUPPORTED_ORDERS, preferenceValue, x -> x.preferenceValue);
            if (match == null) {
                match = SUPPORTED_ORDERS.get(0);
            }
            return match;
        }

        public static @Nullable Base<Artist> fromMenuResourceId(@IdRes int id) {
            // Attn: Dont provide fallback default value, this function can be called with an alien menu res ID
            return Impl.collectionSearch(SUPPORTED_ORDERS, id, x -> x.menuResourceId);
        }

        public static void buildMenu(@NonNull SubMenu sortOrderMenu, String currentPreferenceValue) {
            Impl.buildMenu(SUPPORTED_ORDERS, sortOrderMenu, currentPreferenceValue);
        }
    }

    /**
     * Album sort order entries.
     */
    public interface AlbumSortOrder {
        /* Album sort order A-Z */
        String ALBUM_A_Z = MediaStore.Audio.Albums.DEFAULT_SORT_ORDER;

        /* Album sort order Z-A */
        String ALBUM_Z_A = ALBUM_A_Z + " DESC";

        /* Album sort order artist */
        String ALBUM_ARTIST = MediaStore.Audio.Artists.DEFAULT_SORT_ORDER
                + ", " + MediaStore.Audio.Albums.DEFAULT_SORT_ORDER;

        /* Album sort order year */
        String ALBUM_YEAR_REVERSE = MediaStore.Audio.Media.YEAR + " DESC";

        /* Album date added */
        String ALBUM_DATE_ADDED_REVERSE = MediaStore.Audio.Media.DATE_ADDED + " DESC";

        String ALBUM_DATE_MODIFIED_REVERSE = MediaStore.Audio.Media.DATE_MODIFIED + " DESC";
    }

    /**
     * Song sort order entries.
     */
    public interface SongSortOrder {
        /* Song sort order A-Z */
        String SONG_A_Z = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;

        /* Song sort order Z-A */
        String SONG_Z_A = SONG_A_Z + " DESC";

        /* Song sort order artist */
        String SONG_ARTIST = MediaStore.Audio.Artists.DEFAULT_SORT_ORDER;

        /* Song sort order album */
        String SONG_ALBUM = MediaStore.Audio.Albums.DEFAULT_SORT_ORDER;

        /* Song sort order year */
        String SONG_YEAR_REVERSE = MediaStore.Audio.Media.YEAR + " DESC";

        /* Song sort order date */
        String SONG_DATE_ADDED_REVERSE = MediaStore.Audio.Media.DATE_ADDED + " DESC";

        String SONG_DATE_MODIFIED_REVERSE = MediaStore.Audio.Media.DATE_MODIFIED + " DESC";
    }
}
