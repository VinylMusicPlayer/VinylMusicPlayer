package com.poupa.vinylmusicplayer.sort;

import android.provider.MediaStore;
import android.view.SubMenu;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.discog.Discography;
import com.poupa.vinylmusicplayer.discog.tagging.MultiValuesTagUtil;
import com.poupa.vinylmusicplayer.model.Album;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.util.ComparatorUtil;
import com.poupa.vinylmusicplayer.util.MusicUtil;
import com.poupa.vinylmusicplayer.util.StringUtil;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * @author SC (soncaokim)
 */
public class SongSortOrder {
    private static final Comparator<Song> _BY_TITLE = (s1, s2) -> StringUtil.compareIgnoreAccent(s1.title, s2.title);
    private static final Comparator<Song> _BY_ARTIST = (s1, s2) -> StringUtil.compareIgnoreAccent(
            MultiValuesTagUtil.merge(s1.artistNames),
            MultiValuesTagUtil.merge(s2.artistNames));
    private static final Comparator<Song> _BY_ALBUM = (s1, s2) -> StringUtil.compareIgnoreAccent(s1.albumName, s2.albumName);
    private static final Comparator<Song> _BY_ALBUM_ID = Comparator.comparingLong(s -> s.albumId); // to combine with comparison by name to make the sorting deterministic in the case the names are identical
    private static final Comparator<Song> _BY_YEAR = Comparator.comparingInt(s -> s.year);
    public static final Comparator<Song> BY_DATE_ADDED = Comparator.comparingLong(s -> s.dateAdded);
    public static final Comparator<Song> BY_DATE_ADDED_DESC = ComparatorUtil.reverse(BY_DATE_ADDED);

    private static @NonNull final HashMap<Long, Long> albumId2DateAddedCache = new HashMap();
    private static final Comparator<Song> _BY_ALBUM_DATE_ADDED = Comparator.comparingLong(s -> {
        // Disco.getAlbum and Album.getAdded are costly operations -> cache the result
        synchronized (albumId2DateAddedCache) {
            final Long boxedDate = albumId2DateAddedCache.get(s.albumId);
            if ((boxedDate != null) && (boxedDate <= s.dateAdded)) {
                return boxedDate;
            }

            // either not cached or the cache is stale (i.e song has been added/removed from the album)
            final Album album = Discography.getInstance().getAlbum(s.albumId);
            final long date = (album == null) ? Song.EMPTY_SONG.dateAdded : album.getDateAdded();
            albumId2DateAddedCache.put(s.albumId, date);
            return date;
        }
    });
    private static final Comparator<Song> _BY_ALBUM_DATE_ADDED_DESC = ComparatorUtil.reverse(_BY_ALBUM_DATE_ADDED);

    private static final Comparator<Song> BY_DATE_MODIFIED = Comparator.comparingLong(s -> s.dateModified);
    private static final Comparator<Song> BY_DATE_MODIFIED_DESC = ComparatorUtil.reverse(BY_DATE_MODIFIED);
    private static final Comparator<Song> _BY_DISC_TRACK = (s1, s2) -> (s1.discNumber != s2.discNumber)
            ? (s1.discNumber - s2.discNumber)
            : (s1.trackNumber - s2.trackNumber);
    public static final Comparator<Song> BY_DISC_TRACK = ComparatorUtil.chain(_BY_DISC_TRACK, _BY_TITLE);
    public static final Comparator<Song> BY_ALBUM_DATE_ADDED = ComparatorUtil.chain(_BY_ALBUM_DATE_ADDED, _BY_ALBUM_ID, _BY_DISC_TRACK, _BY_TITLE);
    public static final Comparator<Song> BY_ALBUM_DATE_ADDED_DESC = ComparatorUtil.chain(_BY_ALBUM_DATE_ADDED_DESC, _BY_ALBUM_ID, _BY_DISC_TRACK, _BY_TITLE);
    public static final Comparator<Song> BY_ALBUM = ComparatorUtil.chain(_BY_ALBUM, _BY_ALBUM_ID, BY_DISC_TRACK);
    private static final Comparator<Song> BY_TITLE = ComparatorUtil.chain(_BY_TITLE, _BY_ARTIST, BY_ALBUM);
    private static final Comparator<Song> BY_TITLE_DESC = ComparatorUtil.chain(ComparatorUtil.reverse(_BY_TITLE), _BY_ARTIST, BY_ALBUM);
    private static final Comparator<Song> BY_ARTIST = ComparatorUtil.chain(_BY_ARTIST, _BY_TITLE, BY_ALBUM);
    private static final Comparator<Song> BY_YEAR = ComparatorUtil.chain(_BY_YEAR, BY_TITLE);
    private static final Comparator<Song> BY_YEAR_DESC = ComparatorUtil.chain(ComparatorUtil.reverse(_BY_YEAR), BY_TITLE);

    private static final List<SortOrder<Song>> SUPPORTED_ORDERS = Arrays.asList(
            Utils.build(
                    MediaStore.Audio.Media.DEFAULT_SORT_ORDER,
                    song -> Utils.getSectionName(song.getTitle()),
                    BY_TITLE,
                    R.id.action_song_sort_order_name,
                    R.string.sort_order_name
            ),
            Utils.build(
                    MediaStore.Audio.Media.DEFAULT_SORT_ORDER + " DESC",
                    song -> Utils.getSectionName(song.getTitle()),
                    BY_TITLE_DESC,
                    R.id.action_song_sort_order_name_reverse,
                    R.string.sort_order_name_reverse
            ),
            Utils.build(
                    MediaStore.Audio.Artists.DEFAULT_SORT_ORDER,
                    song -> Utils.getSectionName(MultiValuesTagUtil.merge(song.artistNames)),
                    BY_ARTIST,
                    R.id.action_song_sort_order_artist,
                    R.string.sort_order_artist
            ),
            Utils.build(
                    MediaStore.Audio.Albums.DEFAULT_SORT_ORDER,
                    song -> Utils.getSectionName(Album.getTitle(song.albumName)),
                    BY_ALBUM,
                    R.id.action_song_sort_order_album,
                    R.string.sort_order_album
            ),
            Utils.build(
                    MediaStore.Audio.Media.YEAR,
                    song -> MusicUtil.getYearString(song.year),
                    BY_YEAR,
                    R.id.action_song_sort_order_year,
                    R.string.sort_order_year
            ),
            Utils.build(
                    MediaStore.Audio.Media.YEAR + " DESC",
                    song -> MusicUtil.getYearString(song.year),
                    BY_YEAR_DESC,
                    R.id.action_song_sort_order_year_reverse,
                    R.string.sort_order_year_reverse
            ),
            Utils.build(
                    MediaStore.Audio.Media.DATE_ADDED,
                    song -> Utils.getSectionName(song.dateAdded),
                    BY_DATE_ADDED,
                    R.id.action_song_sort_order_date_added,
                    R.string.sort_order_date_added
            ),
            Utils.build(
                    MediaStore.Audio.Media.DATE_ADDED + " DESC",
                    song -> Utils.getSectionName(song.dateAdded),
                    BY_DATE_ADDED_DESC,
                    R.id.action_song_sort_order_date_added_reverse,
                    R.string.sort_order_date_added_reverse
            ),
            Utils.build(
                    MediaStore.Audio.Media.DATE_MODIFIED,
                    song -> Utils.getSectionName(song.dateModified),
                    BY_DATE_MODIFIED,
                    R.id.action_song_sort_order_date_modified,
                    R.string.sort_order_date_modified
            ),
            Utils.build(
                    MediaStore.Audio.Media.DATE_MODIFIED + " DESC",
                    song -> Utils.getSectionName(song.dateModified),
                    BY_DATE_MODIFIED_DESC,
                    R.id.action_song_sort_order_date_modified_reverse,
                    R.string.sort_order_date_modified_reverse
            )
    );

    public static @NonNull
    SortOrder<Song> fromPreference(@NonNull String preferenceValue) {
        SortOrder<Song> match = Utils.collectionSearch(SUPPORTED_ORDERS, preferenceValue, x -> x.preferenceValue);
        if (match == null) {
            match = SUPPORTED_ORDERS.get(0);
        }
        return match;
    }

    public static @Nullable
    SortOrder<Song> fromMenuResourceId(@IdRes int id) {
        // Attn: Dont provide fallback default value
        // this function can be called with an alien menu res ID, and in such case it should return null value
        return Utils.collectionSearch(SUPPORTED_ORDERS, id, x -> x.menuResourceId);
    }

    public static void buildMenu(@NonNull SubMenu sortOrderMenu, String currentPreferenceValue) {
        Utils.buildMenu(SUPPORTED_ORDERS, sortOrderMenu, currentPreferenceValue);
    }
}
