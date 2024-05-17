package com.poupa.vinylmusicplayer.discog;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.poupa.vinylmusicplayer.discog.tagging.MultiValuesTagUtil;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.util.OopsHandler;
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
            BaseColumns._ID,
            MediaStore.Audio.AudioColumns.TITLE,
            MediaStore.Audio.AudioColumns.TRACK,
            MediaStore.Audio.AudioColumns.YEAR,
            MediaStore.Audio.AudioColumns.DURATION,
            MediaStore.Audio.AudioColumns.DATA,
            MediaStore.Audio.AudioColumns.DATE_ADDED,
            MediaStore.Audio.AudioColumns.DATE_MODIFIED,
            MediaStore.Audio.AudioColumns.ALBUM_ID,
            MediaStore.Audio.AudioColumns.ALBUM,
            MediaStore.Audio.AudioColumns.ARTIST,
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
        return songs;
    }

    @NonNull
    private static Song getSongFromCursorImpl(@NonNull Cursor cursor) {
        // Most of the time data imported from these columns are overriden by those extracted ourselves from ID3 tags
        // However, in the case extraction fails (unsupported track format), they serve as fallback values
        int columnIndex = -1;
        final long id = cursor.getLong(++columnIndex);
        final String title = cursor.getString(++columnIndex);
        final int trackNumber = cursor.getInt(++columnIndex);
        final int year = cursor.getInt(++columnIndex);
        final long duration = cursor.getLong(++columnIndex);
        final String data = cursor.getString(++columnIndex);
        final long dateAdded = cursor.getLong(++columnIndex);
        final long dateModified = cursor.getLong(++columnIndex);
        final long albumId = cursor.getLong(++columnIndex);
        final String albumName = cursor.getString(++columnIndex);
        final List<String> artistNames = MultiValuesTagUtil.split(cursor.getString(++columnIndex));

        final Song song = new Song(id, title, trackNumber, year, duration, data, dateAdded, dateModified, albumId, albumName, artistNames);

        // MediaStore compat: Split track number into disc + track number
        // See documentation for MediaStore.Audio.AudioColumns.TRACK
        song.discNumber = trackNumber / 1000;
        song.trackNumber = trackNumber % 1000;

        return song;
    }

    @Nullable
    private static Cursor makeSongCursor(@NonNull final Context context) {
        try {
            return context.getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    BASE_PROJECTION,
                    BASE_SELECTION,
                    null,
                    PreferenceUtil.getInstance().getSongSortOrder()
            );
        } catch (final RuntimeException e) {
            OopsHandler.collectStackTrace(e);
            return null;
        }
    }
}
