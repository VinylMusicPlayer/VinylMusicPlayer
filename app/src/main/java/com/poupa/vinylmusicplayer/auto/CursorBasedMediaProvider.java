package com.poupa.vinylmusicplayer.auto;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.ContentResolverCompat;

import com.poupa.vinylmusicplayer.loader.SongLoader;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;

import java.util.ArrayList;
import java.util.List;

public class CursorBasedMediaProvider {

    private Cursor cursor;

    CursorBasedMediaProvider(Context applicationContext) {
        cursor = SongLoader.makeSongCursor(applicationContext, null, null, PreferenceUtil.getInstance().getSongSortOrder());
    }

    public int getMediaSize() {
        return cursor.getCount();
    }

    public Song getSongAtPosition(int position) {
        //DatabaseUtils.dumpCursorToString(cursor);
        if (!cursor.moveToPosition(position))
            return null;

        return SongLoader.getSongFromCursorImpl(cursor);
    }

    public List<Song> getSongsAtRange(int startPosition, int endPosition) {
        List<Song> songs = new ArrayList<>();
        for (int position = startPosition; position < endPosition; ++position) {
            Song song = getSongAtPosition(position);
            if (song != null)
                songs.add(song);
        }

        return songs;
    }
}
