package com.poupa.vinylmusicplayer.provider;

import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.poupa.vinylmusicplayer.discog.Discography;
import com.poupa.vinylmusicplayer.model.Song;

import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * @author SC (soncaokim)
 */

public class StoreLoader {
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
}
