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

    static final int OFFSET_ITEM = 0;
    private static final int SONG = 1;

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
    public SongAdapter.ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        if (viewType == OFFSET_ITEM) {
            final ItemListSingleRowBinding binding = ItemListSingleRowBinding.inflate(LayoutInflater.from(activity), parent, false);
            return createViewHolder(binding);
        }
        return super.onCreateViewHolder(parent, viewType);
    }

    @NonNull
    @Override
    protected SongAdapter.ViewHolder createViewHolder(@NonNull final ItemListSingleRowBinding binding) {
        return new AbsOffsetSongAdapter.ViewHolder(binding);
    }

    @NonNull
    @Override
    protected SongAdapter.ViewHolder createViewHolder(@NonNull final ItemListBinding binding) {
        return new AbsOffsetSongAdapter.ViewHolder(binding);
    }

    @NonNull
    @Override
    protected SongAdapter.ViewHolder createViewHolder(@NonNull final ItemGridBinding binding) {
        return new AbsOffsetSongAdapter.ViewHolder(binding);
    }

    @Override
    public long getItemId(final int position) {
        // Shifting by -1, since the very first item is the OFFSET_ITEM
        final int adjustedPosition = position - 1;
        if (adjustedPosition < 0) {return OFFSET_ITEM_ID;}

        return super.getItemId(adjustedPosition);
    }

    @Nullable
    @Override
    protected Song getIdentifier(int position) {
        // Shifting by -1, since the very first item is the OFFSET_ITEM
        final int adjustedPosition = position - 1;
        if (adjustedPosition < 0) {return null;}

        return super.getIdentifier(adjustedPosition);
    }

    @Override
    public int getItemCount() {
        int superItemCount = super.getItemCount();
        return superItemCount == 0 ? 0 : superItemCount + 1;
    }

    @Override
    public int getItemViewType(final int position) {
        return position == 0 ? OFFSET_ITEM : SONG;
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        position--;
        if (position < 0) return "";
        return super.getSectionName(position);
    }

    @Override
    public void onBindViewHolder(@NonNull final SongAdapter.ViewHolder holder, int position) {
        super.onBindViewHolder(holder,position);
        holder.itemView.setActivated(isChecked(position + 1));
    }

    public class ViewHolder extends SongAdapter.ViewHolder {
        public ViewHolder(@NonNull final ItemListSingleRowBinding binding) {
            super(binding);
        }

        public ViewHolder(@NonNull final ItemListBinding binding) {
            super(binding);
        }

        public ViewHolder(@NonNull final ItemGridBinding binding) {
            super(binding);
        }

        @NonNull
        @Override
        protected Song getSong() {
            if (getItemViewType() == OFFSET_ITEM) {
                return Song.EMPTY_SONG;
            }
            return dataSet.get(getBindingAdapterPosition() - 1);
        }

        @Override
        public void onClick(final View v) {
            if (isInQuickSelectMode() && getItemViewType() != OFFSET_ITEM) {
                toggleChecked(getBindingAdapterPosition());
            } else {
                MusicPlayerRemote.enqueueSongsWithConfirmation(v.getContext(), dataSet, getBindingAdapterPosition() - 1);
            }
        }

        @Override
        public boolean onLongClick(final View view) {
            if (getItemViewType() == OFFSET_ITEM) return false;
            setColor(ThemeStore.primaryColor(activity));
            toggleChecked(getBindingAdapterPosition());
            return true;
        }
    }
}
