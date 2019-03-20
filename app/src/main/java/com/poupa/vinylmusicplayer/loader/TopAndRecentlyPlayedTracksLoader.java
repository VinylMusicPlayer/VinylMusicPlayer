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
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.provider.HistoryStore;
import com.poupa.vinylmusicplayer.provider.SongPlayCountStore;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;

import java.util.ArrayList;

public class TopAndRecentlyPlayedTracksLoader {
    public static final int NUMBER_OF_TOP_TRACKS = 100;

    @NonNull
    public static ArrayList<Song> getRecentlyPlayedTracks(@NonNull Context context) {
        return SongLoader.getSongs(makeRecentTracksCursorAndClearUpDatabase(context));
    }

    @NonNull
    public static ArrayList<Song> getNotRecentlyPlayedTracks(@NonNull Context context) {
        ArrayList<Song> allSongs = SongLoader.getSongs(
            SongLoader.makeSongCursor(
                context,
                null, null,
                MediaStore.Audio.Media.DATE_ADDED + " ASC"));

        ArrayList<Song> playedSongs = SongLoader.getSongs(
            makePlayedTracksCursorAndClearUpDatabase(context));

        ArrayList<Song> notRecentlyPlayedSongs = SongLoader.getSongs(
            makeNotRecentTracksCursorAndClearUpDatabase(context));

        ArrayList<Song> result = allSongs;
        result.removeAll(playedSongs);
        result.addAll(notRecentlyPlayedSongs);

        return result;
    }

    @NonNull
    public static ArrayList<Song> getTopTracks(@NonNull Context context) {
        return SongLoader.getSongs(makeTopTracksCursorAndClearUpDatabase(context));
    }

    @Nullable
    public static Cursor makeRecentTracksCursorAndClearUpDatabase(@NonNull final Context context) {
        return makeRecentTracksCursorAndClearUpDatabaseImpl(context, false, false);
    }

     @Nullable
    public static Cursor makePlayedTracksCursorAndClearUpDatabase(@NonNull final Context context) {
        return makeRecentTracksCursorAndClearUpDatabaseImpl(context, true, false);
    }

     @Nullable
    public static Cursor makeNotRecentTracksCursorAndClearUpDatabase(@NonNull final Context context) {
        return makeRecentTracksCursorAndClearUpDatabaseImpl(context, false, true);
    }

     @Nullable
    private static Cursor makeRecentTracksCursorAndClearUpDatabaseImpl(@NonNull final Context context, boolean ignoreCutoffTime, boolean reverseOrder) {
        SortedLongCursor retCursor = makeRecentTracksCursorImpl(context, ignoreCutoffTime, reverseOrder);

        // clean up the databases with any ids not found
        if (retCursor != null) {
            ArrayList<Long> missingIds = retCursor.getMissingIds();
            if (missingIds != null && missingIds.size() > 0) {
                for (long id : missingIds) {
                    HistoryStore.getInstance(context).removeSongId(id);
                }
            }
        }
        return retCursor;
    }

    @Nullable
    public static Cursor makeTopTracksCursorAndClearUpDatabase(@NonNull final Context context) {
        SortedLongCursor retCursor = makeTopTracksCursorImpl(context);

        // clean up the databases with any ids not found
        if (retCursor != null) {
            ArrayList<Long> missingIds = retCursor.getMissingIds();
            if (missingIds != null && missingIds.size() > 0) {
                for (long id : missingIds) {
                    SongPlayCountStore.getInstance(context).removeItem(id);
                }
            }
        }
        return retCursor;
    }

    @Nullable
    private static SortedLongCursor makeRecentTracksCursorImpl(@NonNull final Context context, boolean ignoreCutoffTime, boolean reverseOrder) {
        final long cutoff = (ignoreCutoffTime ? 0 : PreferenceUtil.getInstance().getRecentlyPlayedCutoffTimeMillis());
        Cursor songs = HistoryStore.getInstance(context).queryRecentIds(cutoff * (reverseOrder ? -1 : 1));

        try {
            return makeSortedCursor(context, songs,
                    songs.getColumnIndex(HistoryStore.RecentStoreColumns.ID));
        } finally {
            if (songs != null) {
                songs.close();
            }
        }
    }

    @Nullable
    private static SortedLongCursor makeTopTracksCursorImpl(@NonNull final Context context) {
        // first get the top results ids from the internal database
        Cursor songs = SongPlayCountStore.getInstance(context).getTopPlayedResults(NUMBER_OF_TOP_TRACKS);

        try {
            return makeSortedCursor(context, songs,
                    songs.getColumnIndex(SongPlayCountStore.SongPlayCountColumns.ID));
        } finally {
            if (songs != null) {
                songs.close();
            }
        }
    }

    @Nullable
    private static SortedLongCursor makeSortedCursor(@NonNull final Context context, @Nullable final Cursor cursor, final int idColumn) {
        if (cursor != null && cursor.moveToFirst()) {
            // create the list of ids to select against
            StringBuilder selection = new StringBuilder();
            selection.append(BaseColumns._ID);
            selection.append(" IN (");

            // this tracks the order of the ids
            long[] order = new long[cursor.getCount()];

            long id = cursor.getLong(idColumn);
            selection.append(id);
            order[cursor.getPosition()] = id;

            while (cursor.moveToNext()) {
                selection.append(",");

                id = cursor.getLong(idColumn);
                order[cursor.getPosition()] = id;
                selection.append(String.valueOf(id));
            }

            selection.append(")");

            // get a list of songs with the data given the selection statement
            Cursor songCursor = SongLoader.makeSongCursor(context, selection.toString(), null);
            if (songCursor != null) {
                // now return the wrapped TopTracksCursor to handle sorting given order
                return new SortedLongCursor(songCursor, order, BaseColumns._ID);
            }
        }

        return null;
    }
}
