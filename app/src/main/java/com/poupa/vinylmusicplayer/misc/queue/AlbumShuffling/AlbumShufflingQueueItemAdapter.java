package com.poupa.vinylmusicplayer.misc.queue.AlbumShuffling;


import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.util.ATHUtil;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.adapter.song.SongAdapter;
import com.poupa.vinylmusicplayer.glide.GlideApp;
import com.poupa.vinylmusicplayer.helper.MusicPlayerRemote;
import com.poupa.vinylmusicplayer.misc.queue.DynamicElement;
import com.poupa.vinylmusicplayer.misc.queue.DynamicQueueItemAdapter;
import com.poupa.vinylmusicplayer.misc.queue.DynamicQueueLoader;
import com.poupa.vinylmusicplayer.util.PlayingSongDecorationUtil;


public class AlbumShufflingQueueItemAdapter implements DynamicQueueItemAdapter {

    private DynamicElement dynamicElement;

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
        } else if (item.getItemId() == R.id.delete) {
            MusicPlayerRemote.setQueueToStaticQueue();
        }
        return false;
    }

    public void onBindViewHolder(@NonNull SongAdapter.ViewHolder holder, @NonNull final AppCompatActivity activity) {
        holder.title.setText(dynamicElement.firstLine);
        holder.text.setText(dynamicElement.secondLine);

        if (dynamicElement.icon == DynamicElement.INVALID_ICON) {
            holder.image.setVisibility(View.GONE);

            holder.imageText.setVisibility(View.VISIBLE);
            holder.imageText.setText(dynamicElement.iconText);
        } else {
            holder.imageText.setVisibility(View.GONE);
            holder.image.setVisibility(View.VISIBLE);

            PlayingSongDecorationUtil.addIconToItem(activity, holder.image, dynamicElement.icon);
        }
    }
}
