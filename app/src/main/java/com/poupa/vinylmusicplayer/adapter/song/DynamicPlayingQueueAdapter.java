package com.poupa.vinylmusicplayer.adapter.song;


import java.util.ArrayList;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.poupa.vinylmusicplayer.databinding.ItemGridBinding;
import com.poupa.vinylmusicplayer.databinding.ItemListBinding;

import com.poupa.vinylmusicplayer.interfaces.CabHolder;
import com.poupa.vinylmusicplayer.misc.queue.DynamicQueueItemAdapter;
import com.poupa.vinylmusicplayer.model.Song;

/**
 * Extension of StaticPlayingQueueAdapter that manage the list of song + an item showing the queue to be loaded at the end of the current one
 */

public class DynamicPlayingQueueAdapter extends StaticPlayingQueueAdapter {

    /** To differentiate next queue item */
    protected static final int OFFSET_ITEM = UP_NEXT+1;

    /** Provide next queue item management (menu & binding) */
    private DynamicQueueItemAdapter dynamicQueueItemAdapter;

    public DynamicPlayingQueueAdapter(AppCompatActivity activity, ArrayList<Song> dataSet, int current, boolean usePalette, @Nullable CabHolder cabHolder, DynamicQueueItemAdapter dynamicQueueItemAdapter) {
        super(activity, dataSet, current, usePalette, cabHolder);

        this.dynamicQueueItemAdapter = dynamicQueueItemAdapter;
    }

    /** reload dynamic element and ensure queue is visually updated */
    public void reloadDynamicElement() {
        if (dynamicQueueItemAdapter != null) {
            dynamicQueueItemAdapter.reloadDynamicElement();
            notifyDataSetChanged();
        }
    }

    @Override
    public void onBindViewHolder(@NonNull SongAdapter.ViewHolder holder, int position) {
        if (dynamicQueueItemAdapter != null && holder.getItemViewType() == OFFSET_ITEM) {
            dynamicQueueItemAdapter.onBindViewHolder(holder, activity);
        } else {
            super.onBindViewHolder(holder, position);
        }
    }

    @NonNull
    @Override
    public SongAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == OFFSET_ITEM) {
            ItemListBinding binding = ItemListBinding.inflate(LayoutInflater.from(activity), parent, false);
            return createViewHolder(binding);
        }
        return super.onCreateViewHolder(parent, viewType);
    }

    @Override
    public int getItemCount() {
        if (dataSet.size() == 0) // if song list is empty, then dynamic element cannot exist
            return 0;

        return dataSet.size()+1;
    }

    @Override
    public long getItemId(int position) {
        // dynamic element need unique value, thus made it negative and other element positive
        if (position >= dataSet.size())
            return -1;

        return Math.abs(super.getItemId(position));
    }

    @NonNull
    @Override
    protected SongAdapter.ViewHolder createViewHolder(@NonNull ItemListBinding binding) {
        return new ViewHolder(binding);
    }

    @Override
    public int getItemViewType(int position) {
        if (position >= dataSet.size())
            return OFFSET_ITEM;

        return super.getItemViewType(position);
    }

    public class ViewHolder extends StaticPlayingQueueAdapter.ViewHolder {

        public ViewHolder(@NonNull ItemListBinding binding) {
            super(binding);
        }

        public ViewHolder(@NonNull ItemGridBinding binding) {
            super(binding);
        }

        @Override
        protected int getSongMenuRes(int itemViewType) {
            if (dynamicQueueItemAdapter != null && itemViewType == OFFSET_ITEM)
                return dynamicQueueItemAdapter.getSongMenuRes(itemViewType);

            return super.getSongMenuRes(itemViewType);
        }

        @Override
        protected boolean onSongMenuItemClick(MenuItem item) {
            if (dynamicQueueItemAdapter != null && dynamicQueueItemAdapter.onSongMenuItemClick(item))
                return true;

            return super.onSongMenuItemClick(item);
        }
    }
}
