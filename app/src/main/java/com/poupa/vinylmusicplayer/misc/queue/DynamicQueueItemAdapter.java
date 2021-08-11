package com.poupa.vinylmusicplayer.misc.queue;


import android.view.MenuItem;

import androidx.annotation.NonNull;
import com.poupa.vinylmusicplayer.adapter.song.SongAdapter;


public interface DynamicQueueItemAdapter {
    int getSongMenuRes(int itemViewType);
    boolean onSongMenuItemClick(MenuItem item);
    void onBindViewHolder(@NonNull SongAdapter.ViewHolder holder);

    void swapDynamicElement();
}
