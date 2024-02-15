package com.poupa.vinylmusicplayer.adapter.song;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.kabouzeid.appthemehelper.ThemeStore;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.databinding.ItemGridBinding;
import com.poupa.vinylmusicplayer.databinding.ItemListBinding;
import com.poupa.vinylmusicplayer.databinding.ItemListSingleRowBinding;
import com.poupa.vinylmusicplayer.helper.MusicPlayerRemote;
import com.poupa.vinylmusicplayer.interfaces.CabHolder;
import com.poupa.vinylmusicplayer.model.Song;

import java.util.List;

/**
 * @author Eugene Cheung (arkon)
 */
public abstract class AbsOffsetSongAdapter extends SongAdapter {

    protected static final int OFFSET_ITEM = 0;
    protected static final int SONG = 1;

    // Need to be different from RecyclerView.NO_ID to not to upset the base class
    protected static final long OFFSET_ITEM_ID = RecyclerView.NO_ID - 1;

    public AbsOffsetSongAdapter(final AppCompatActivity activity, final List<? extends Song> dataSet,
                                @LayoutRes final int itemLayoutRes,
                                final boolean usePalette, @Nullable final CabHolder cabHolder) {
        super(activity, dataSet, itemLayoutRes, usePalette, cabHolder);
    }

    public AbsOffsetSongAdapter(final AppCompatActivity activity, final List<? extends Song> dataSet,
                                final boolean usePalette, @Nullable final CabHolder cabHolder,
                                final boolean showSectionName) {
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
    @Override
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
        // Shifting by -1, since the very first item is the OFFSET_ITEM
        position--;

        return (position < 0 ? OFFSET_ITEM_ID : super.getItemId(position));
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
            return dataSet.get(getBindingAdapterPosition() - 1);
        }

        @Override
        public void onClick(View v) {
            if (isInQuickSelectMode() && getItemViewType() != OFFSET_ITEM) {
                toggleChecked(getBindingAdapterPosition());
            } else {
                MusicPlayerRemote.enqueueSongsWithConfirmation(v.getContext(), dataSet, getBindingAdapterPosition() - 1);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (getItemViewType() == OFFSET_ITEM) return false;
            setColor(ThemeStore.primaryColor(activity));
            toggleChecked(getBindingAdapterPosition());
            return true;
        }
    }
}
