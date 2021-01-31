package com.poupa.vinylmusicplayer.discog;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.poupa.vinylmusicplayer.discog.tagging.MultiValuesTagUtil;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.provider.BlacklistStore;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid)
 * @author SC (soncaokim)
 */

public class MediaStoreBridge {
    private static final String BASE_SELECTION = MediaStore.Audio.AudioColumns.IS_MUSIC + "=1" + " AND " + MediaStore.Audio.AudioColumns.TITLE + " != ''";
    private static final String[] BASE_PROJECTION = new String[]{
            BaseColumns._ID,// 0
            MediaStore.Audio.AudioColumns.TITLE,// 1
            MediaStore.Audio.AudioColumns.TRACK,// 2
            MediaStore.Audio.AudioColumns.YEAR,// 3
            MediaStore.Audio.AudioColumns.DURATION,// 4
            MediaStore.Audio.AudioColumns.DATA,// 5
            MediaStore.Audio.AudioColumns.DATE_ADDED,// 6
            MediaStore.Audio.AudioColumns.DATE_MODIFIED,// 7
            MediaStore.Audio.AudioColumns.ALBUM_ID,// 8
            MediaStore.Audio.AudioColumns.ALBUM,// 9
            MediaStore.Audio.AudioColumns.ARTIST_ID,// 10
            MediaStore.Audio.AudioColumns.ARTIST,// 11
    };

    @NonNull
    public static ArrayList<Song> getAllSongs(@NonNull Context context) {
        try (Cursor cursor = makeSongCursor(context)) {
            return getSongs(cursor);
        }
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

        return new Song(id, title, trackNumber, year, duration, data, dateAdded, dateModified, albumId, albumName, artistId, artistNames);
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
        newSelection.append(MediaStore.Audio.AudioColumns.DATA + " NOT LIKE ?");
        for (int i = 1; i < pathCount; i++) {
            newSelection.append(" AND " + MediaStore.Audio.AudioColumns.DATA + " NOT LIKE ?");
        }
        return newSelection.toString();
    }

    @Nullable
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
