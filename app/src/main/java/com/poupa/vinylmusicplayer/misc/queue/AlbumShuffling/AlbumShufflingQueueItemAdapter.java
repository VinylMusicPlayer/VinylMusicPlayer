package com.poupa.vinylmusicplayer.misc.queue.AlbumShuffling;


import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.adapter.song.SongAdapter;
import com.poupa.vinylmusicplayer.helper.MusicPlayerRemote;
import com.poupa.vinylmusicplayer.misc.queue.DynamicElement;
import com.poupa.vinylmusicplayer.misc.queue.DynamicQueueItemAdapter;
import com.poupa.vinylmusicplayer.misc.queue.DynamicQueueLoader;


public class AlbumShufflingQueueItemAdapter implements DynamicQueueItemAdapter {

    private DynamicElement dynamicElement; // should be replace by an interface that give data to show

    public AlbumShufflingQueueItemAdapter() {
        this.dynamicElement = MusicPlayerRemote.getDynamicElement();
    }

    public void swapDynamicElement() {
        this.dynamicElement = MusicPlayerRemote.getDynamicElement();
    }

    public int getSongMenuRes(int itemViewType) {
        return R.menu.menu_item_playing_queue_album_shuffling;
    }

    public boolean onSongMenuItemClick(MenuItem item) {
        Bundle bundle = new Bundle();
        if (item.getItemId() == R.id.action_shuffle_random_album) {
            // Refresh proposed next random album with one completely random
            bundle.putInt(AlbumShufflingQueueLoader.SEARCH_TYPE, AlbumShufflingQueueLoader.RANDOM_SEARCH);
            MusicPlayerRemote.setNextDynamicQueue(bundle);
            return true;
        } else if (item.getItemId() == R.id.action_shuffle_artist_album) {
            // Refresh proposed next random album with one with the same artist than the one I listen to right now
            bundle.putInt(AlbumShufflingQueueLoader.SEARCH_TYPE, AlbumShufflingQueueLoader.ARTIST_SEARCH);
            MusicPlayerRemote.setNextDynamicQueue(bundle);
            return true;
        } else if (item.getItemId() == R.id.action_shuffle_genre_album) {
            // Refresh proposed next random album with one with the same genre than the one I listen to right now
            bundle.putInt(AlbumShufflingQueueLoader.SEARCH_TYPE, AlbumShufflingQueueLoader.GENRE_SEARCH);
            MusicPlayerRemote.setNextDynamicQueue(bundle);
            return true;
        }
        return false;
    }

    public void onBindViewHolder(@NonNull SongAdapter.ViewHolder holder) {
        holder.title.setText(dynamicElement.firstLine);
        holder.text.setText(dynamicElement.secondLine);
        holder.imageText.setText(dynamicElement.icon);
        holder.image.setVisibility(View.GONE);
        holder.imageText.setVisibility(View.VISIBLE);
    }
}
