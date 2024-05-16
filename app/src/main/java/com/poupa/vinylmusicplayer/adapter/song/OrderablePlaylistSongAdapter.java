package com.poupa.vinylmusicplayer.adapter.song;

import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.draggable.annotation.DraggableItemStateFlags;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.databinding.ItemGridBinding;
import com.poupa.vinylmusicplayer.databinding.ItemListBinding;
import com.poupa.vinylmusicplayer.dialogs.RemoveFromPlaylistDialog;
import com.poupa.vinylmusicplayer.interfaces.PaletteColorHolder;
import com.poupa.vinylmusicplayer.misc.queue.IndexedSong;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.ui.activities.base.AbsThemeActivity;
import com.poupa.vinylmusicplayer.util.ImageTheme.ThemeStyleUtil;
import com.poupa.vinylmusicplayer.util.ViewUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class OrderablePlaylistSongAdapter
        extends PlaylistSongAdapter
        implements DraggableItemAdapter<AbsOffsetSongAdapter.ViewHolder>
{
    final long playlistId;
    final OnMoveItemListener onMoveItemListener;

    public OrderablePlaylistSongAdapter(
            @NonNull final AbsThemeActivity activity,
            final long playlistId, @NonNull final ArrayList<Song> dataSet,
            final boolean usePalette, @Nullable final PaletteColorHolder palette,
            @Nullable final OnMoveItemListener onMoveItemListener)
    {
        super(activity, dataSet, usePalette, palette);
        setMultiSelectMenuRes(R.menu.menu_playlists_songs_selection);
        this.playlistId = playlistId;
        this.onMoveItemListener = onMoveItemListener;
    }

    @Override
    public long getItemId(final int position) {
        // Shifting by -1, since the very first item is the OFFSET_ITEM
        final int adjustedPosition = position - 1;
        if (adjustedPosition < 0) {return OFFSET_ITEM_ID;}

        // Since the playlist may contain duplicates of same song,
        // the song's ID cannot be used as the recycle view item ID
        return ((IndexedSong)dataSet.get(adjustedPosition)).getUniqueId();
    }

    @NonNull
    @Override
    protected SongAdapter.ViewHolder createViewHolder(@NonNull ItemListBinding binding) {
        return new OrderablePlaylistSongAdapter.ViewHolder(binding);
    }

    @NonNull
    @Override
    protected SongAdapter.ViewHolder createViewHolder(@NonNull ItemGridBinding binding) {
        return new OrderablePlaylistSongAdapter.ViewHolder(binding);
    }

    @Override
    protected void onMultipleItemAction(@NonNull final MenuItem menuItem, @NonNull final Map<Integer, Song> selection) {
        if (menuItem.getItemId() == R.id.action_remove_from_playlist) {
            // Shifting by -1, since the very first item is the OFFSET_ITEM
            // Use LinkedHashMap to preserve the insertion order in the original selection
            final Map<Integer, Song> selectionWithShiftedPosition = new LinkedHashMap<>(selection.size());
            selection.keySet().iterator().forEachRemaining(
                    position -> selectionWithShiftedPosition.put(position - 1, selection.get(position))
            );

            RemoveFromPlaylistDialog
                    .create(playlistId, selectionWithShiftedPosition)
                    .show(activity.getSupportFragmentManager(), RemoveFromPlaylistDialog.TAG);
            return;
        }
        super.onMultipleItemAction(menuItem, selection);
    }

    @Override
    public boolean onCheckCanStartDrag(AbsOffsetSongAdapter.ViewHolder holder, int position, int x, int y) {
        if (holder instanceof ViewHolder) {
            return onMoveItemListener != null && position > 0 &&
                    (ViewUtil.hitTest(holder.dragView, x, y) || ViewUtil.hitTest(holder.image, x, y));
        } else {
            return false;
        }
    }

    @Override
    public ItemDraggableRange onGetItemDraggableRange(AbsOffsetSongAdapter.ViewHolder holder, int position) {
        return new ItemDraggableRange(1, dataSet.size());
    }

    @Override
    public void onMoveItem(int fromPosition, int toPosition) {
        if (onMoveItemListener != null && fromPosition != toPosition) {
            onMoveItemListener.onMoveItem(fromPosition - 1, toPosition - 1);
        }
    }

    @Override
    public boolean onCheckCanDrop(int draggingPosition, int dropPosition) {
        return dropPosition > 0;
    }

    @Override
    public void onItemDragStarted(int position) {
        notifyDataSetChanged();
    }

    @Override
    public void onItemDragFinished(int fromPosition, int toPosition, boolean result) {
        notifyDataSetChanged();
    }

    public interface OnMoveItemListener {
        void onMoveItem(int fromPosition, int toPosition);
    }

    public class ViewHolder extends PlaylistSongAdapter.ViewHolder {
        @DraggableItemStateFlags
        private int mDragStateFlags;

        public ViewHolder(@NonNull ItemListBinding binding) {
            super(binding);
            if (onMoveItemListener != null) {
                dragView.setVisibility(View.VISIBLE);

                ThemeStyleUtil.getInstance().setDragView((AppCompatImageView)dragView);
            } else {
                dragView.setVisibility(View.GONE);
            }
        }

        public ViewHolder(@NonNull ItemGridBinding binding) {
            super(binding);
        }

        @Override
        protected int getSongMenuRes() {
            return R.menu.menu_item_playlist_song;
        }

        @Override
        protected boolean onSongMenuItemClick(MenuItem item) {
            if (item.getItemId() == R.id.action_remove_from_playlist) {
                RemoveFromPlaylistDialog
                        // Shifting by -1, since the very first item is the OFFSET_ITEM
                        .create(playlistId, getBindingAdapterPosition() - 1, getSong())
                        .show(activity.getSupportFragmentManager(), RemoveFromPlaylistDialog.TAG);
                return true;
            }
            return super.onSongMenuItemClick(item);
        }

        @Override
        public void setDragStateFlags(@DraggableItemStateFlags int flags) {
            mDragStateFlags = flags;
        }

        @Override
        @DraggableItemStateFlags
        public int getDragStateFlags() {
            return mDragStateFlags;
        }
    }
}
