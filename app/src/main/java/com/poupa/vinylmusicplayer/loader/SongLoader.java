package com.poupa.vinylmusicplayer.loader;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.AudioColumns;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.poupa.vinylmusicplayer.discog.Discography;
import com.poupa.vinylmusicplayer.discog.tagging.MultiValuesTagUtil;
import com.poupa.vinylmusicplayer.helper.SortOrder;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.provider.BlacklistStore;
import com.poupa.vinylmusicplayer.util.ComparatorUtil;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;
import com.poupa.vinylmusicplayer.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class SongLoader {
    public static final Comparator<Song> BY_TITLE = (s1, s2) -> StringUtil.compareIgnoreAccent(s1.title, s2.title);
    public static final Comparator<Song> BY_ARTIST = (s1, s2) -> StringUtil.compareIgnoreAccent(MultiValuesTagUtil.infoString(s1.artistNames), MultiValuesTagUtil.infoString(s2.artistNames));
    public static final Comparator<Song> BY_ALBUM = (s1, s2) -> StringUtil.compareIgnoreAccent(s1.albumName, s2.albumName);
    public static final Comparator<Song> BY_YEAR_DESC = (s1, s2) -> s2.year - s1.year;
    public static final Comparator<Song> BY_DATE_ADDED = (s1, s2) -> ComparatorUtil.compareLongInts(s1.dateAdded, s2.dateAdded);
    public static final Comparator<Song> BY_DATE_ADDED_DESC = ComparatorUtil.reverse(BY_DATE_ADDED);
    public static final Comparator<Song> BY_DISC_TRACK = (s1, s2) -> (s1.discNumber != s2.discNumber)
            ? (s1.discNumber - s2.discNumber)
            : (s1.trackNumber - s2.trackNumber);

    private final static Discography discography = Discography.getInstance();

    protected static final String BASE_SELECTION = AudioColumns.IS_MUSIC + "=1" + " AND " + AudioColumns.TITLE + " != ''";
    protected static final String[] BASE_PROJECTION = new String[]{
            BaseColumns._ID,// 0
            AudioColumns.TITLE,// 1
            AudioColumns.TRACK,// 2
            AudioColumns.YEAR,// 3
            AudioColumns.DURATION,// 4
            AudioColumns.DATA,// 5
            AudioColumns.DATE_ADDED,// 6
            AudioColumns.DATE_MODIFIED,// 7
            AudioColumns.ALBUM_ID,// 8
            AudioColumns.ALBUM,// 9
            AudioColumns.ARTIST_ID,// 10
            AudioColumns.ARTIST,// 11
    };

    @NonNull
    public static ArrayList<Song> getAllSongs(@NonNull Context context) {
        try (Cursor cursor = makeSongCursor(context)) {
            return getSongs(cursor);
        }
    }

    @NonNull
    public static ArrayList<Song> getSongs(@NonNull final String query) {
        final String strippedQuery = StringUtil.stripAccent(query.toLowerCase());

        ArrayList<Song> songs = new ArrayList<>();
        for (Song song : Discography.getInstance().getAllSongs()) {
            final String strippedTitle = StringUtil.stripAccent(song.title.toLowerCase());
            if (strippedTitle.contains(strippedQuery)) {
                songs.add(song);
            }
        }
        Collections.sort(songs, getSortOrder());
        return songs;
    }

    @NonNull
    public static Comparator<Song> getSortOrder() {
        switch (PreferenceUtil.getInstance().getSongSortOrder()) {
            case SortOrder.SongSortOrder.SONG_Z_A:
                return ComparatorUtil.chain(ComparatorUtil.reverse(BY_TITLE), ComparatorUtil.reverse(BY_ARTIST));
            case SortOrder.SongSortOrder.SONG_ARTIST:
                return ComparatorUtil.chain(BY_ARTIST, BY_ALBUM);
            case SortOrder.SongSortOrder.SONG_ALBUM:
                return ComparatorUtil.chain(BY_ALBUM, BY_ARTIST);
            case SortOrder.SongSortOrder.SONG_YEAR_REVERSE:
                return ComparatorUtil.chain(BY_YEAR_DESC, BY_ARTIST);
            case SortOrder.SongSortOrder.SONG_DATE_ADDED_REVERSE:
                return ComparatorUtil.chain(BY_DATE_ADDED_DESC, BY_ARTIST);

            case SortOrder.SongSortOrder.SONG_A_Z:
            default:
                return ComparatorUtil.chain(BY_TITLE, BY_ARTIST);
        }
    }

    @NonNull
    public static ArrayList<Long> getIdsFromCursor(@Nullable Cursor cursor, @NonNull final String columnName) {
        ArrayList<Long> ids = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()) {
            int idColumn = cursor.getColumnIndex(columnName);
            do {
                ids.add(cursor.getLong(idColumn));
            } while (cursor.moveToNext());
        }

        return ids;
    }

    @NonNull
    public static ArrayList<Song> getSongsFromIdsAndCleanupOrphans(@NonNull ArrayList<Long> songIds, @Nullable Consumer<ArrayList<Long>> orphanIdsCleaner) {
        Discography discography = Discography.getInstance();
        ArrayList<Long> orphanSongIds = new ArrayList<>();

        ArrayList<Song> songs = new ArrayList<>();
        for (Long id : songIds) {
            Song song = discography.getSong(id);
            if (song.id == Song.EMPTY_SONG.id) {
                orphanSongIds.add(id);
            } else {
                songs.add(song);
            }
        }

        if (orphanIdsCleaner != null) {
            orphanIdsCleaner.accept(orphanSongIds);
        }
        return songs;
    }

    @NonNull
    private static ArrayList<Song> getSongs(@Nullable final Cursor cursor) {
        ArrayList<Song> songs = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                songs.add(getSongFromCursorImpl(cursor));
            } while (cursor.moveToNext());
        }

        if (cursor != null)
            cursor.close();
        return songs;
    }

    @NonNull
    private static Song getSongFromCursorImpl(@NonNull Cursor cursor) {
        final long id = cursor.getLong(0);
        final String title = cursor.getString(1);
        final int trackNumber = cursor.getInt(2);
        final int year = cursor.getInt(3);
        final long duration = cursor.getLong(4);
        final String data = cursor.getString(5);
        final long dateAdded = cursor.getLong(6);
        final long dateModified = cursor.getLong(7);
        final long albumId = cursor.getLong(8);
        final String albumName = cursor.getString(9);
        final long artistId = cursor.getLong(10);
        final List<String> artistNames = MultiValuesTagUtil.split(cursor.getString(11));

        Song song = new Song(id, title, trackNumber, year, duration, data, dateAdded, dateModified, albumId, albumName, artistId, artistNames);

        Discography discog = Discography.getInstance();
        return discog.getOrAddSong(song);
    }

    @Nullable
    private static Cursor makeSongCursor(@NonNull final Context context) {
        // Blacklist
        // Note: There is a SQLite limit on the number of ?argument.
        // Being 999, it is unlikely that we reach that limit for the number of black-listed paths
        final ArrayList<String> paths = BlacklistStore.getInstance(context).getPaths();
        try {
            return context.getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    BASE_PROJECTION,
                    generateBlacklistSelection(paths.size()),
                    addBlacklistSelectionValues(paths),
                    PreferenceUtil.getInstance().getSongSortOrder()
            );
        } catch (SecurityException ignored) {
            return null;
        }
    }

    private static String generateBlacklistSelection(int pathCount) {
        if (pathCount <= 0) {
            return BASE_SELECTION;
        }

        StringBuilder newSelection = new StringBuilder(BASE_SELECTION + " AND ");
        newSelection.append(AudioColumns.DATA + " NOT LIKE ?");
        for (int i = 1; i < pathCount; i++) {
            newSelection.append(" AND " + AudioColumns.DATA + " NOT LIKE ?");
        }
        return newSelection.toString();
    }

    private static String[] addBlacklistSelectionValues(@NonNull final List<String> paths) {
        if (paths.isEmpty()) {
            return null;
        }

        ArrayList<String> newSelectionValues;
        newSelectionValues = new ArrayList<>(paths.size());

        for (int i = 0; i < paths.size(); i++) {
            newSelectionValues.add(paths.get(i) + "%");
        }
        return newSelectionValues.toArray(new String[0]);
    }
}
