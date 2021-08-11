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
import com.poupa.vinylmusicplayer.misc.queue.AlbumShuffling.AlbumShufflingQueueItemAdapter;
import com.poupa.vinylmusicplayer.misc.queue.DynamicQueueItemAdapter;
import com.poupa.vinylmusicplayer.model.Song;

public class DynamicPlayingQueueAdapter extends PlayingQueueAdapter {

    protected static final int OFFSET_ITEM = UP_NEXT+1;

    private DynamicQueueItemAdapter dynamicQueueItemAdapter;

    public DynamicPlayingQueueAdapter(AppCompatActivity activity, ArrayList<Song> dataSet, int current, boolean usePalette, @Nullable CabHolder cabHolder) {
        super(activity, dataSet, current, usePalette, cabHolder);

        this.dynamicQueueItemAdapter = new AlbumShufflingQueueItemAdapter();
    }

    public void swapDynamicElement() {
        dynamicQueueItemAdapter.swapDynamicElement();
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull SongAdapter.ViewHolder holder, int position) {
        if (holder.getItemViewType() == OFFSET_ITEM) {
            dynamicQueueItemAdapter.onBindViewHolder(holder);
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
        return dataSet.size()+1;
    }

    @Override
    public long getItemId(int position) {
        if (position < dataSet.size())
            return dataSet.get(position).id;
        return -1;
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

    public class ViewHolder extends PlayingQueueAdapter.ViewHolder {

        public ViewHolder(@NonNull ItemListBinding binding) {
            super(binding);
        }

        public ViewHolder(@NonNull ItemGridBinding binding) {
            super(binding);
        }

        @Override
        protected int getSongMenuRes(int itemViewType) {
            if (itemViewType == OFFSET_ITEM) {
                return dynamicQueueItemAdapter.getSongMenuRes(itemViewType);
            } else {
                return super.getSongMenuRes(itemViewType);
            }
        }

        @Override
        protected boolean onSongMenuItemClick(MenuItem item) {
            if (dynamicQueueItemAdapter.onSongMenuItemClick(item))
                return true;

            return super.onSongMenuItemClick(item);
        }
    }
}
