/*
* Copyright (C) 2014 The CyanogenMod Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.poupa.vinylmusicplayer.loader;

import android.content.Context;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.poupa.vinylmusicplayer.discog.Discography;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.provider.HistoryStore;
import com.poupa.vinylmusicplayer.provider.SongPlayCountStore;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;

import java.util.ArrayList;

public class TopAndRecentlyPlayedTracksLoader {
    @NonNull
    public static ArrayList<Song> getRecentlyPlayedTracks(@NonNull Context context) {
        final long cutoff = PreferenceUtil.getInstance().getRecentlyPlayedCutoffTimeMillis();
        try (Cursor cursor = HistoryStore.getInstance(context).queryRecentIds(cutoff)) {
            ArrayList<Long> songIds = getIdsFromCursor(cursor, HistoryStore.RecentStoreColumns.ID);
            return getSongsFromIdsAndCleanupHistory(context, songIds);
        }
    }

    @NonNull
    public static ArrayList<Song> getNotRecentlyPlayedTracks(@NonNull Context context) {
        HistoryStore historyStore = HistoryStore.getInstance(context);
        ArrayList<Long> songIds = new ArrayList<>();

        // Collect not played songs
        try (Cursor cursor = historyStore.queryRecentIds(0)) {
            ArrayList<Long> playedSongIds = getIdsFromCursor(cursor, HistoryStore.RecentStoreColumns.ID);
            for (Song song : Discography.getInstance().getAllSongs()) {
                if (!playedSongIds.contains(song.id)) {
                    songIds.add(song.id);
                }
            }
        }

        // Collect not recently played songs
        final long cutoff = PreferenceUtil.getInstance().getRecentlyPlayedCutoffTimeMillis();
        try (Cursor cursor = historyStore.queryRecentIds(-1 * cutoff)) {
            ArrayList<Long> notRecentSongIds = getIdsFromCursor(cursor, HistoryStore.RecentStoreColumns.ID);
            songIds.addAll(notRecentSongIds);
        }

        return getSongsFromIdsAndCleanupHistory(context, songIds);
    }

    @NonNull
    public static ArrayList<Song> getTopTracks(@NonNull Context context) {
        final int NUMBER_OF_TOP_TRACKS = 100;
        try (Cursor cursor = SongPlayCountStore.getInstance(context).getTopPlayedResults(NUMBER_OF_TOP_TRACKS)){
            ArrayList<Long> songIds = getIdsFromCursor(cursor, SongPlayCountStore.SongPlayCountColumns.ID);
            return getSongsFromIdsAndCleanupHistory(context, songIds);
        }
    }

    @NonNull
    private static ArrayList<Long> getIdsFromCursor(@Nullable Cursor cursor, @NonNull final String columnName) {
        ArrayList<Long> ids = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()) {
            int idColumn = cursor.getColumnIndex(columnName);
            ids.add(cursor.getLong(idColumn));
            while (cursor.moveToNext()) {
                ids.add(cursor.getLong(idColumn));
            }
        }

        return ids;
    }

    @NonNull
    private static ArrayList<Song> getSongsFromIdsAndCleanupHistory(@NonNull Context context, @NonNull ArrayList<Long> songIds) {
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

        HistoryStore.getInstance(context).removeSongIds(orphanSongIds);
        return songs;
    }
}
