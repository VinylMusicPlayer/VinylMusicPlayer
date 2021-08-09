package com.poupa.vinylmusicplayer.adapter.song;


import java.util.ArrayList;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.databinding.ItemGridBinding;
import com.poupa.vinylmusicplayer.databinding.ItemListBinding;
import com.poupa.vinylmusicplayer.interfaces.CabHolder;
import com.poupa.vinylmusicplayer.model.Song;

public class DynamicPlayingQueueAdapter extends PlayingQueueAdapter {

    protected static final int OFFSET_ITEM = UP_NEXT+1;
    private Song pseudoSong; // should be replace by an interface that give data to show
    // new private interface to get menutype and menu action

    public DynamicPlayingQueueAdapter(AppCompatActivity activity, ArrayList<Song> dataSet, int current, boolean usePalette, @Nullable CabHolder cabHolder, Song pseudoSong) {
        super(activity, dataSet, current, usePalette, cabHolder);

        this.pseudoSong = pseudoSong;
    }

    @NonNull
    @Override
    public SongAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == OFFSET_ITEM) {
            ItemListBinding binding =  ItemListBinding.inflate(LayoutInflater.from(activity), parent, false);
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

    @Override
    public void onBindViewHolder(@NonNull SongAdapter.ViewHolder holder, int position) {
        if (holder.getItemViewType() == OFFSET_ITEM) {
            holder.title.setText(pseudoSong.title);
            holder.text.setText(pseudoSong.albumName);
            // should add icon
        } else {
            super.onBindViewHolder(holder, position);
        }
    }

    @NonNull
    @Override
    protected SongAdapter.ViewHolder createViewHolder(@NonNull ItemListBinding binding) {
        return new ViewHolder(binding);
    }

    @NonNull
    @Override
    protected SongAdapter.ViewHolder createViewHolder(@NonNull ItemGridBinding binding) {
        return new ViewHolder(binding);
    }

    @Override
    public int getItemViewType(int position) {
        if (position >= dataSet.size())
            return OFFSET_ITEM;

        return super.getItemViewType(position);
    }

    public void swapDynamicElement(Song dynamicElement) {
        this.pseudoSong = dynamicElement;
        notifyDataSetChanged();
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
                return R.menu.menu_item_playing_queue_album_shuffling;
            } else {
                return super.getSongMenuRes(itemViewType);
            }
        }
    }
}
