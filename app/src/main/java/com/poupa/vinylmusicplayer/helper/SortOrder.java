package com.poupa.vinylmusicplayer.helper;

import android.provider.MediaStore;
import android.view.SubMenu;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.discog.tagging.MultiValuesTagUtil;
import com.poupa.vinylmusicplayer.model.Album;
import com.poupa.vinylmusicplayer.model.Artist;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.util.ComparatorUtil;
import com.poupa.vinylmusicplayer.util.MusicUtil;
import com.poupa.vinylmusicplayer.util.StringUtil;

import java.util.Arrays;
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
        static <T> Base<T> build(
                @NonNull String preferenceValue,
                @NonNull Function<T, String> sectionNameBuilder,
                @NonNull Comparator<T> comparatorFunc,
                @IdRes int menuResourceId,
                @StringRes int menuTextId)
        {
            Base<T> result = new Base<>();
            result.preferenceValue = preferenceValue;
            result.sectionNameBuilder = sectionNameBuilder;
            result.comparator = comparatorFunc;
            result.menuResourceId = menuResourceId;
            result.menuTextId = menuTextId;
            return result;
        }

        static @NonNull String getSectionName(long seconds) {
            final Date date = new Date(1000 * seconds);
            final String format = "yyyy";
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

    public static class ByArtist {
        private static final Comparator<Artist> BY_ARTIST = (a1, a2) -> StringUtil.compareIgnoreAccent(a1.name, a2.name);
        private static final Comparator<Artist> BY_DATE_MODIFIED = (a1, a2) -> ComparatorUtil.compareLongInts(a1.getDateModified(), a2.getDateModified());

        private static final List<Base<Artist>> SUPPORTED_ORDERS = Arrays.asList(
                Impl.build(
                        MediaStore.Audio.Artists.DEFAULT_SORT_ORDER,
                        artist -> Impl.getSectionName(artist.getName()),
                        BY_ARTIST,
                        R.id.action_artist_sort_order_name,
                        R.string.sort_order_name
                ),
                Impl.build(
                        MediaStore.Audio.Artists.DEFAULT_SORT_ORDER + " DESC",
                        artist -> Impl.getSectionName(artist.getName()),
                        ComparatorUtil.reverse(BY_ARTIST),
                        R.id.action_artist_sort_order_name_reverse,
                        R.string.sort_order_name_reverse
                ),
                Impl.build(
                        MediaStore.Audio.Media.DATE_MODIFIED,
                        artist -> Impl.getSectionName(artist.getDateModified()),
                        BY_DATE_MODIFIED,
                        R.id.action_artist_sort_order_date_modified,
                        R.string.sort_order_date_modified
                ),
                Impl.build(
                        MediaStore.Audio.Media.DATE_MODIFIED + " DESC",
                        artist -> Impl.getSectionName(artist.getDateModified()),
                        ComparatorUtil.reverse(BY_DATE_MODIFIED),
                        R.id.action_artist_sort_order_date_modified_reverse,
                        R.string.sort_order_date_modified_reverse
                )
        );

        public static @NonNull Base<Artist> fromPreference(@NonNull String preferenceValue) {
            Base<Artist> match = Impl.collectionSearch(SUPPORTED_ORDERS, preferenceValue, x -> x.preferenceValue);
            if (match == null) {
                match = SUPPORTED_ORDERS.get(0);
            }
            return match;
        }

        public static @Nullable Base<Artist> fromMenuResourceId(@IdRes int id) {
            // Attn: Dont provide fallback default value
            // this function can be called with an alien menu res ID, and in such case it should return null value
            return Impl.collectionSearch(SUPPORTED_ORDERS, id, x -> x.menuResourceId);
        }

        public static void buildMenu(@NonNull SubMenu sortOrderMenu, String currentPreferenceValue) {
            Impl.buildMenu(SUPPORTED_ORDERS, sortOrderMenu, currentPreferenceValue);
        }
    }

    public static class ByAlbum {
        private static final Comparator<Album> _BY_ALBUM_NAME = (a1, a2) -> StringUtil.compareIgnoreAccent(
                a1.safeGetFirstSong().albumName,
                a2.safeGetFirstSong().albumName);
        private static final Comparator<Album> _BY_ARTIST_NAME = (a1, a2) -> StringUtil.compareIgnoreAccent(
                a1.getArtistName(),
                a2.getArtistName());
        private static final Comparator<Album> _BY_DATE_ADDED = (a1, a2) -> ComparatorUtil.compareLongInts(a1.getDateAdded(), a2.getDateAdded());
        private static final Comparator<Album> _BY_DATE_MODIFIED = (a1, a2) -> ComparatorUtil.compareLongInts(a1.getDateModified(), a2.getDateModified());
        private static final Comparator<Album> _BY_YEAR = (a1, a2) -> a1.getYear() - a2.getYear();

        private static final Comparator<Album> BY_ALBUM = ComparatorUtil.chain(_BY_ALBUM_NAME, _BY_ARTIST_NAME);
        private static final Comparator<Album> BY_ALBUM_DESC = ComparatorUtil.chain(ComparatorUtil.reverse(_BY_ALBUM_NAME), _BY_ARTIST_NAME);
        private static final Comparator<Album> BY_ARTIST = ComparatorUtil.chain(_BY_ARTIST_NAME, _BY_ALBUM_NAME);
        private static final Comparator<Album> BY_DATE_ADDED = ComparatorUtil.chain(_BY_DATE_ADDED, _BY_ALBUM_NAME);
        private static final Comparator<Album> BY_DATE_ADDED_DESC = ComparatorUtil.chain(ComparatorUtil.reverse(_BY_DATE_ADDED), _BY_ALBUM_NAME);
        private static final Comparator<Album> BY_DATE_MODIFIED = ComparatorUtil.chain(_BY_DATE_MODIFIED, _BY_ALBUM_NAME);
        private static final Comparator<Album> BY_DATE_MODIFIED_DESC = ComparatorUtil.chain(ComparatorUtil.reverse(_BY_DATE_MODIFIED), _BY_ALBUM_NAME);
        private static final Comparator<Album> BY_YEAR = ComparatorUtil.chain(_BY_YEAR, _BY_ALBUM_NAME);
        public static final Comparator<Album> BY_YEAR_DESC = ComparatorUtil.chain(ComparatorUtil.reverse(_BY_YEAR), _BY_ALBUM_NAME);

        private static final List<Base<Album>> SUPPORTED_ORDERS = Arrays.asList(
                Impl.build(
                        MediaStore.Audio.Artists.DEFAULT_SORT_ORDER + ", " + MediaStore.Audio.Albums.DEFAULT_SORT_ORDER,
                        album -> Impl.getSectionName(album.getArtistName()),
                        BY_ARTIST,
                        R.id.action_album_sort_order_artist,
                        R.string.sort_order_artist
                ),
                Impl.build(
                        MediaStore.Audio.Albums.DEFAULT_SORT_ORDER,
                        album -> Impl.getSectionName(album.getTitle()),
                        BY_ALBUM,
                        R.id.action_album_sort_order_name,
                        R.string.sort_order_name
                ),
                Impl.build(
                        MediaStore.Audio.Albums.DEFAULT_SORT_ORDER + " DESC",
                        album -> Impl.getSectionName(album.getTitle()),
                        BY_ALBUM_DESC,
                        R.id.action_album_sort_order_name_reverse,
                        R.string.sort_order_name_reverse
                ),
                Impl.build(
                        MediaStore.Audio.Media.YEAR,
                        album -> MusicUtil.getYearString(album.getYear()),
                        BY_YEAR,
                        R.id.action_album_sort_order_year,
                        R.string.sort_order_year
                ),
                Impl.build(
                        MediaStore.Audio.Media.YEAR + " DESC",
                        artist -> MusicUtil.getYearString(artist.getYear()),
                        BY_YEAR_DESC,
                        R.id.action_album_sort_order_year_reverse,
                        R.string.sort_order_year_reverse
                ),
                Impl.build(
                        MediaStore.Audio.Media.DATE_ADDED,
                        album -> Impl.getSectionName(album.getDateAdded()),
                        BY_DATE_ADDED,
                        R.id.action_album_sort_order_date_added,
                        R.string.sort_order_date_added
                ),
                Impl.build(
                        MediaStore.Audio.Media.DATE_ADDED + " DESC",
                        artist -> Impl.getSectionName(artist.getDateAdded()),
                        BY_DATE_ADDED_DESC,
                        R.id.action_album_sort_order_date_added_reverse,
                        R.string.sort_order_date_added_reverse
                ),
                Impl.build(
                        MediaStore.Audio.Media.DATE_MODIFIED,
                        album -> Impl.getSectionName(album.getDateModified()),
                        BY_DATE_MODIFIED,
                        R.id.action_album_sort_order_date_modified,
                        R.string.sort_order_date_modified
                ),
                Impl.build(
                        MediaStore.Audio.Media.DATE_MODIFIED + " DESC",
                        artist -> Impl.getSectionName(artist.getDateModified()),
                        BY_DATE_MODIFIED_DESC,
                        R.id.action_album_sort_order_date_modified_reverse,
                        R.string.sort_order_date_modified_reverse
                )
        );

        public static @NonNull Base<Album> fromPreference(@NonNull String preferenceValue) {
            Base<Album> match = Impl.collectionSearch(SUPPORTED_ORDERS, preferenceValue, x -> x.preferenceValue);
            if (match == null) {
                match = SUPPORTED_ORDERS.get(0);
            }
            return match;
        }

        public static @Nullable Base<Album> fromMenuResourceId(@IdRes int id) {
            // Attn: Dont provide fallback default value
            // this function can be called with an alien menu res ID, and in such case it should return null value
            return Impl.collectionSearch(SUPPORTED_ORDERS, id, x -> x.menuResourceId);
        }

        public static void buildMenu(@NonNull SubMenu sortOrderMenu, String currentPreferenceValue) {
            Impl.buildMenu(SUPPORTED_ORDERS, sortOrderMenu, currentPreferenceValue);
        }
    }

    public static class BySong {
        private static final Comparator<Song> _BY_TITLE = (s1, s2) -> StringUtil.compareIgnoreAccent(s1.title, s2.title);
        private static final Comparator<Song> _BY_ARTIST = (s1, s2) -> StringUtil.compareIgnoreAccent(
                MultiValuesTagUtil.infoString(s1.artistNames),
                MultiValuesTagUtil.infoString(s2.artistNames));
        private static final Comparator<Song> _BY_ALBUM = (s1, s2) -> StringUtil.compareIgnoreAccent(s1.albumName, s2.albumName);
        private static final Comparator<Song> _BY_YEAR = (s1, s2) -> s1.year - s2.year;
        public static final Comparator<Song> BY_DATE_ADDED = (s1, s2) -> ComparatorUtil.compareLongInts(s1.dateAdded, s2.dateAdded);
        public static final Comparator<Song> BY_DATE_ADDED_DESC = ComparatorUtil.reverse(BY_DATE_ADDED);
        private static final Comparator<Song> BY_DATE_MODIFIED = (s1, s2) -> ComparatorUtil.compareLongInts(s1.dateModified, s2.dateModified);
        private static final Comparator<Song> BY_DATE_MODIFIED_DESC = ComparatorUtil.reverse(BY_DATE_MODIFIED);
        public static final Comparator<Song> BY_DISC_TRACK = (s1, s2) -> (s1.discNumber != s2.discNumber)
                ? (s1.discNumber - s2.discNumber)
                : (s1.trackNumber - s2.trackNumber);

        private static final Comparator<Song> BY_TITLE = ComparatorUtil.chain(_BY_TITLE, _BY_ARTIST);
        private static final Comparator<Song> BY_TITLE_DESC = ComparatorUtil.chain(ComparatorUtil.reverse(_BY_TITLE), _BY_ARTIST);
        private static final Comparator<Song> BY_ARTIST = ComparatorUtil.chain(_BY_ARTIST, _BY_TITLE);
        public static final Comparator<Song> BY_ALBUM = ComparatorUtil.chain(_BY_ALBUM, BY_DISC_TRACK);
        private static final Comparator<Song> BY_YEAR = ComparatorUtil.chain(_BY_YEAR, _BY_TITLE);
        private static final Comparator<Song> BY_YEAR_DESC = ComparatorUtil.chain(ComparatorUtil.reverse(_BY_YEAR), _BY_TITLE);

        private static final List<Base<Song>> SUPPORTED_ORDERS = Arrays.asList(
                Impl.build(
                        MediaStore.Audio.Media.DEFAULT_SORT_ORDER,
                        song -> Impl.getSectionName(song.title),
                        BY_TITLE,
                        R.id.action_song_sort_order_name,
                        R.string.sort_order_name
                ),
                Impl.build(
                        MediaStore.Audio.Media.DEFAULT_SORT_ORDER + " DESC",
                        song -> Impl.getSectionName(song.title),
                        BY_TITLE_DESC,
                        R.id.action_song_sort_order_name_reverse,
                        R.string.sort_order_name_reverse
                ),
                Impl.build(
                        MediaStore.Audio.Artists.DEFAULT_SORT_ORDER,
                        song -> Impl.getSectionName(MultiValuesTagUtil.infoString(song.artistNames)),
                        BY_ARTIST,
                        R.id.action_song_sort_order_artist,
                        R.string.sort_order_artist
                ),
                Impl.build(
                        MediaStore.Audio.Albums.DEFAULT_SORT_ORDER,
                        song -> Impl.getSectionName(song.albumName),
                        BY_ALBUM,
                        R.id.action_song_sort_order_album,
                        R.string.sort_order_album
                ),
                Impl.build(
                        MediaStore.Audio.Media.YEAR,
                        song -> MusicUtil.getYearString(song.year),
                        BY_YEAR,
                        R.id.action_song_sort_order_year,
                        R.string.sort_order_year
                ),
                Impl.build(
                        MediaStore.Audio.Media.YEAR + " DESC",
                        song -> MusicUtil.getYearString(song.year),
                        BY_YEAR_DESC,
                        R.id.action_song_sort_order_year_reverse,
                        R.string.sort_order_year_reverse
                ),
                Impl.build(
                        MediaStore.Audio.Media.DATE_ADDED,
                        song -> Impl.getSectionName(song.dateAdded),
                        BY_DATE_ADDED,
                        R.id.action_song_sort_order_date_added,
                        R.string.sort_order_date_added
                ),
                Impl.build(
                        MediaStore.Audio.Media.DATE_ADDED + " DESC",
                        song -> Impl.getSectionName(song.dateAdded),
                        BY_DATE_ADDED_DESC,
                        R.id.action_song_sort_order_date_added_reverse,
                        R.string.sort_order_date_added_reverse
                ),
                Impl.build(
                        MediaStore.Audio.Media.DATE_MODIFIED,
                        song -> Impl.getSectionName(song.dateModified),
                        BY_DATE_MODIFIED,
                        R.id.action_song_sort_order_date_modified,
                        R.string.sort_order_date_modified
                ),
                Impl.build(
                        MediaStore.Audio.Media.DATE_MODIFIED + " DESC",
                        song -> Impl.getSectionName(song.dateModified),
                        BY_DATE_MODIFIED_DESC,
                        R.id.action_song_sort_order_date_modified_reverse,
                        R.string.sort_order_date_modified_reverse
                )
        );

        public static @NonNull Base<Song> fromPreference(@NonNull String preferenceValue) {
            Base<Song> match = Impl.collectionSearch(SUPPORTED_ORDERS, preferenceValue, x -> x.preferenceValue);
            if (match == null) {
                match = SUPPORTED_ORDERS.get(0);
            }
            return match;
        }

        public static @Nullable Base<Song> fromMenuResourceId(@IdRes int id) {
            // Attn: Dont provide fallback default value
            // this function can be called with an alien menu res ID, and in such case it should return null value
            return Impl.collectionSearch(SUPPORTED_ORDERS, id, x -> x.menuResourceId);
        }

        public static void buildMenu(@NonNull SubMenu sortOrderMenu, String currentPreferenceValue) {
            Impl.buildMenu(SUPPORTED_ORDERS, sortOrderMenu, currentPreferenceValue);
        }
    }
}
