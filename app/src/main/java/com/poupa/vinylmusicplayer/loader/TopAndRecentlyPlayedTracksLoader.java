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
import android.database.sqlite.SQLiteException;

import androidx.annotation.NonNull;

import com.poupa.vinylmusicplayer.discog.Discography;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.provider.HistoryStore;
import com.poupa.vinylmusicplayer.provider.SongPlayCountStore;
import com.poupa.vinylmusicplayer.provider.StoreLoader;
import com.poupa.vinylmusicplayer.sort.SongSortOrder;
import com.poupa.vinylmusicplayer.util.OopsHandler;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;

public class TopAndRecentlyPlayedTracksLoader {
    @NonNull
    public static ArrayList<Song> getRecentlyPlayedTracks(@NonNull final Context context) {
        final long cutoff = PreferenceUtil.getInstance().getRecentlyPlayedCutoffTimeMillis();
        if (cutoff == 0) {return new ArrayList<>();}

        HistoryStore historyStore = HistoryStore.getInstance(context);
        ArrayList<Long> songIds = historyStore.getRecentIds(cutoff);

        return Discography.getInstance().getSongsFromIdsAndCleanupOrphans(songIds, historyStore::removeSongIds);
    }

    @NonNull
    public static ArrayList<Song> getNotRecentlyPlayedTracks(@NonNull final Context context) {
        final long cutoff = PreferenceUtil.getInstance().getNotRecentlyPlayedCutoffTimeMillis();
        if (cutoff == 0) {return new ArrayList<>();}

        HistoryStore historyStore = HistoryStore.getInstance(context);
        ArrayList<Long> songIds = new ArrayList<>();

        // Collect not played songs
        Discography discography = Discography.getInstance();
        HashSet<Long> playedSongIds = new HashSet<>(historyStore.getRecentIds(0));
        @NonNull final String sortOrderStr = PreferenceUtil.getInstance().getNotRecentlyPlayedSortOrder();
        Comparator<Song> sortOrder = sortOrderStr.equals(PreferenceUtil.ALBUM_SORT_ORDER) ? SongSortOrder.BY_ALBUM_DATE_ADDED : SongSortOrder.BY_DATE_ADDED;
        ArrayList<Song> allSongs = discography.getAllSongs(sortOrder);

        for (Song song : allSongs) {
            if (!playedSongIds.contains(song.id)) {
                songIds.add(song.id);
                playedSongIds.remove(song.id);
            }
        }

        // Collect not recently played songs
        ArrayList<Long> notRecentSongIds = historyStore.getRecentIds(-1 * cutoff);
        songIds.addAll(notRecentSongIds);

        return discography.getSongsFromIdsAndCleanupOrphans(songIds, historyStore::removeSongIds);
    }

    @NonNull
    public static ArrayList<Song> getTopTracks(@NonNull final Context context) {
        final boolean enabled = PreferenceUtil.getInstance().maintainTopTrackPlaylist();
        if (!enabled) {return new ArrayList<>();}

        final int NUMBER_OF_TOP_TRACKS = 100;
        try (Cursor cursor = SongPlayCountStore.getInstance(context).getTopPlayedResults(NUMBER_OF_TOP_TRACKS)) {
            ArrayList<Long> songIds = StoreLoader.getIdsFromCursor(cursor, SongPlayCountStore.SongPlayCountColumns.ID);
            Discography discography = Discography.getInstance();
            return discography.getSongsFromIdsAndCleanupOrphans(songIds, null);
        } catch (SQLiteException|IllegalStateException exception) {
            OopsHandler.collectStackTrace(exception);
            return new ArrayList<>();
        }
    }

}
