package com.poupa.vinylmusicplayer.misc.queue;


import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.poupa.vinylmusicplayer.adapter.song.SongAdapter;

/** Provide all methods needed by DynamicPlayingQueueAdapter to show the {@link com.poupa.vinylmusicplayer.misc.queue.DynamicElement} at the end of the playing queue with the correct menu */
public interface DynamicQueueItemAdapter {
    int getSongMenuRes(int itemViewType);
    boolean onSongMenuItemClick(MenuItem item);
    void onBindViewHolder(@NonNull SongAdapter.ViewHolder holder, @NonNull final AppCompatActivity activity);

    void reloadDynamicElement();
}
