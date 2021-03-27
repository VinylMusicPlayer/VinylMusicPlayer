package com.poupa.vinylmusicplayer.adapter.song;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.kabouzeid.appthemehelper.ThemeStore;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.databinding.ItemGridBinding;
import com.poupa.vinylmusicplayer.databinding.ItemListBinding;
import com.poupa.vinylmusicplayer.databinding.ItemListSingleRowBinding;
import com.poupa.vinylmusicplayer.helper.MusicPlayerRemote;
import com.poupa.vinylmusicplayer.interfaces.CabHolder;
import com.poupa.vinylmusicplayer.model.Song;

import java.util.ArrayList;

/**
 * @author Eugene Cheung (arkon)
 */
public abstract class AbsOffsetSongAdapter extends SongAdapter {

    protected static final int OFFSET_ITEM = 0;
    protected static final int SONG = 1;

    public AbsOffsetSongAdapter(AppCompatActivity activity, ArrayList<Song> dataSet, @LayoutRes int itemLayoutRes, boolean usePalette, @Nullable CabHolder cabHolder) {
        super(activity, dataSet, itemLayoutRes, usePalette, cabHolder);
    }

    public AbsOffsetSongAdapter(AppCompatActivity activity, ArrayList<Song> dataSet, boolean usePalette, @Nullable CabHolder cabHolder, boolean showSectionName) {
        super(activity, dataSet, R.layout.item_list, usePalette, cabHolder, showSectionName);
    }

    @NonNull
    @Override
    public SongAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == OFFSET_ITEM) {
            ItemListSingleRowBinding binding = ItemListSingleRowBinding.inflate(LayoutInflater.from(activity), parent, false);
            return createViewHolder(binding);
        }
        return super.onCreateViewHolder(parent, viewType);
    }

    @NonNull
    protected SongAdapter.ViewHolder createViewHolder(@NonNull ItemListSingleRowBinding binding) {
        return new AbsOffsetSongAdapter.ViewHolder(binding);
    }

    @NonNull
    @Override
    protected SongAdapter.ViewHolder createViewHolder(@NonNull ItemListBinding binding) {
        return new AbsOffsetSongAdapter.ViewHolder(binding);
    }

    @NonNull
    @Override
    protected SongAdapter.ViewHolder createViewHolder(@NonNull ItemGridBinding binding) {
        return new AbsOffsetSongAdapter.ViewHolder(binding);
    }

    @Override
    public long getItemId(int position) {
        position--;
        if (position < 0) return -2;
        return super.getItemId(position);
    }

    @Nullable
    @Override
    protected Song getIdentifier(int position) {
        position--;
        if (position < 0) return null;
        return super.getIdentifier(position);
    }

    @Override
    public int getItemCount() {
        int superItemCount = super.getItemCount();
        return superItemCount == 0 ? 0 : superItemCount + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? OFFSET_ITEM : SONG;
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        position--;
        if (position < 0) return "";
        return super.getSectionName(position);
    }

    public class ViewHolder extends SongAdapter.ViewHolder {
        public ViewHolder(@NonNull ItemListSingleRowBinding binding) {
            super(binding);
        }

        public ViewHolder(@NonNull ItemListBinding binding) {
            super(binding);
        }

        public ViewHolder(@NonNull ItemGridBinding binding) {
            super(binding);
        }

        @Override
        protected Song getSong() {
            if (getItemViewType() == OFFSET_ITEM)
                return Song.EMPTY_SONG; // could also return null, just to be safe return empty song
            return dataSet.get(getAdapterPosition() - 1);
        }

        @Override
        public void onClick(View v) {
            if (isInQuickSelectMode() && getItemViewType() != OFFSET_ITEM) {
                toggleChecked(getAdapterPosition());
            } else {
                MusicPlayerRemote.openQueue(dataSet, getAdapterPosition() - 1, true);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (getItemViewType() == OFFSET_ITEM) return false;
            setColor(ThemeStore.primaryColor(activity));
            toggleChecked(getAdapterPosition());
            return true;
        }
    }
}
