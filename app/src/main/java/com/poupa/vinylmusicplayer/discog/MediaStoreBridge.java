package com.poupa.vinylmusicplayer.discog;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.poupa.vinylmusicplayer.discog.tagging.MultiValuesTagUtil;
import com.poupa.vinylmusicplayer.model.Song;
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
        return songs;
    }

    @NonNull
    private static Song getSongFromCursorImpl(@NonNull Cursor cursor) {
        // Most of the time data imported from these columns are overriden by those extracted ourselves from ID3 tags
        // However, in the case extraction fails (unsupported track format), they serve as fallback values
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

        final Song song = new Song(id, title, trackNumber, year, duration, data, dateAdded, dateModified, albumId, albumName, artistId, artistNames);

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
        } catch (SecurityException e) {
            e.printStackTrace();
            return null;
        }
    }
}
