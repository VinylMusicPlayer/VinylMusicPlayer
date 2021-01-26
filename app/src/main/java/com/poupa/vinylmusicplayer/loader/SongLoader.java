package com.poupa.vinylmusicplayer.loader;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.AudioColumns;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.poupa.vinylmusicplayer.discog.Discography;
import com.poupa.vinylmusicplayer.discog.ComparatorUtil;
import com.poupa.vinylmusicplayer.discog.MultiValuesTagUtil;
import com.poupa.vinylmusicplayer.discog.StringUtil;
import com.poupa.vinylmusicplayer.helper.SortOrder;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.provider.BlacklistStore;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class SongLoader {
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
        // TODO Use Discog.getAllSongs instead, to benefit from the zombie elimination
        Cursor cursor = makeSongCursor(context, null, null);
        return getSongs(cursor);
    }

    @NonNull
    public static ArrayList<Song> getSongs(@NonNull final String query) {
        final String strippedQuery = StringUtil.stripAccent(query.toLowerCase());

        synchronized (discography) {
            ArrayList<Song> songs = new ArrayList<>();
            for (Song song : discography.getAllSongs()) {
                final String strippedTitle = StringUtil.stripAccent(song.title.toLowerCase());
                if (strippedTitle.contains(strippedQuery)) {
                    songs.add(song);
                }
            }
            Collections.sort(songs, getSortOrder());
            return songs;
        }
    }

    @NonNull
    private static Comparator<Song> getSortOrder() {
        Comparator<Song> byTitle = (a1, a2) -> StringUtil.compareIgnoreAccent(a1.title, a2.title);
        Comparator<Song> byArtist = (a1, a2) -> StringUtil.compareIgnoreAccent(MultiValuesTagUtil.infoString(a1.artistNames), MultiValuesTagUtil.infoString(a2.artistNames));
        Comparator<Song> byAlbum = (a1, a2) -> StringUtil.compareIgnoreAccent(a1.albumName, a2.albumName);
        Comparator<Song> byYearDesc = (a1, a2) -> a2.year - a1.year;
        Comparator<Song> byDateAddedDesc = (a1, a2) -> ComparatorUtil.compareLongInts(a2.dateAdded, a1.dateAdded);

        switch (PreferenceUtil.getInstance().getSongSortOrder()) {
            case SortOrder.SongSortOrder.SONG_Z_A:
                return ComparatorUtil.chain(ComparatorUtil.reverse(byTitle), ComparatorUtil.reverse(byArtist));
            case SortOrder.SongSortOrder.SONG_ARTIST:
                return ComparatorUtil.chain(byArtist, byAlbum);
            case SortOrder.SongSortOrder.SONG_ALBUM:
                return ComparatorUtil.chain(byAlbum, byArtist);
            case SortOrder.SongSortOrder.SONG_YEAR_REVERSE:
                return ComparatorUtil.chain(byYearDesc, byArtist);
            case SortOrder.SongSortOrder.SONG_DATE_ADDED_REVERSE:
                return ComparatorUtil.chain(byDateAddedDesc, byArtist);

            case SortOrder.SongSortOrder.SONG_A_Z:
            default:
                return ComparatorUtil.chain(byTitle, byArtist);
        }
    }

    @NonNull
    public static ArrayList<Song> getSongs(@Nullable final Cursor cursor) {
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
    public static Cursor makeSongCursor(@NonNull final Context context, @Nullable final String selection, final String[] selectionValues) {
        return makeSongCursor(context, selection, selectionValues, PreferenceUtil.getInstance().getSongSortOrder());
    }

    @Nullable
    public static Cursor makeSongCursor(@NonNull final Context context, @Nullable String selection, String[] selectionValues, final String sortOrder) {
        if (selection != null && !selection.trim().equals("")) {
            selection = BASE_SELECTION + " AND " + selection;
        } else {
            selection = BASE_SELECTION;
        }

        // Blacklist
        // Note: There is a SQLite limit on the number of ?argument.
        // Being 999, it is unlikely that we reach that limit for the number of black-listed paths
        final ArrayList<String> paths = BlacklistStore.getInstance(context).getPaths();
        try {
            return context.getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    BASE_PROJECTION,
                    generateBlacklistSelection(selection, paths.size()),
                    addBlacklistSelectionValues(selectionValues, paths),
                    sortOrder
            );
        } catch (SecurityException ignored) {
            return null;
        }
    }

    private static String generateBlacklistSelection(String selection, int pathCount) {
        if (pathCount <= 0) {
            return selection;
        }

        StringBuilder newSelection = new StringBuilder(selection != null && !selection.trim().equals("") ? selection + " AND " : "");
        newSelection.append(AudioColumns.DATA + " NOT LIKE ?");
        for (int i = 1; i < pathCount; i++) {
            newSelection.append(" AND " + AudioColumns.DATA + " NOT LIKE ?");
        }
        return newSelection.toString();
    }

    private static String[] addBlacklistSelectionValues(String[] selectionValues, @NonNull final List<String> paths) {
        if (paths.isEmpty()) {
            return selectionValues;
        }

        ArrayList<String> newSelectionValues;
        if (selectionValues == null) {
            newSelectionValues = new ArrayList<>(paths.size());
        }
        else {
            newSelectionValues = new ArrayList<>(selectionValues.length + paths.size());
            newSelectionValues.addAll(Arrays.asList(selectionValues));
        }

        for (int i = 0; i < paths.size(); i++) {
            newSelectionValues.add(paths.get(i) + "%");
        }
        return newSelectionValues.toArray(new String[0]);
    }
}
