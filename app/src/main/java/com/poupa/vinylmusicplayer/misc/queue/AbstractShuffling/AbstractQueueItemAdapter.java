package com.poupa.vinylmusicplayer.misc.queue.AbstractShuffling;


import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.poupa.vinylmusicplayer.adapter.song.SongAdapter;
import com.poupa.vinylmusicplayer.helper.MusicPlayerRemote;
import com.poupa.vinylmusicplayer.misc.queue.DynamicElement;
import com.poupa.vinylmusicplayer.misc.queue.DynamicQueueItemAdapter;
import com.poupa.vinylmusicplayer.util.PlayingSongDecorationUtil;


/** Abstract pseudo implementation of {@link com.poupa.vinylmusicplayer.misc.queue.DynamicQueueItemAdapter} to help creating multiple shuffling system */
public abstract class AbstractQueueItemAdapter implements DynamicQueueItemAdapter {

    private DynamicElement dynamicElement;

    public AbstractQueueItemAdapter() {
        this.dynamicElement = MusicPlayerRemote.getDynamicElement();
    }

    public void reloadDynamicElement() {
        this.dynamicElement = MusicPlayerRemote.getDynamicElement();
    }

    public void onBindViewHolder(@NonNull SongAdapter.ViewHolder holder, @NonNull final AppCompatActivity activity) {
        if (dynamicElement == null)
            return;

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
