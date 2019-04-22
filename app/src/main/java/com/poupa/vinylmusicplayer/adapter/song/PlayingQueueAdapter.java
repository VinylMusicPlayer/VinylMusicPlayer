package com.poupa.vinylmusicplayer.adapter.song;

import android.graphics.Rect;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.draggable.annotation.DraggableItemStateFlags;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemConstants;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultAction;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionDefault;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionRemoveItem;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.annotation.SwipeableItemResults;
import com.kabouzeid.appthemehelper.util.ATHUtil;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.helper.MusicPlayerRemote;
import com.poupa.vinylmusicplayer.interfaces.CabHolder;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.util.ViewUtil;

import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class PlayingQueueAdapter extends SongAdapter
        implements DraggableItemAdapter<PlayingQueueAdapter.ViewHolder>, SwipeableItemAdapter<PlayingQueueAdapter.ViewHolder> {

    private static final int HISTORY = 0;
    private static final int CURRENT = 1;
    private static final int UP_NEXT = 2;

    public Song songToRemove;

    private static Snackbar currentlyShownSnackbar;

    private int current;

    public PlayingQueueAdapter(AppCompatActivity activity, ArrayList<Song> dataSet, int current, @LayoutRes int itemLayoutRes, boolean usePalette, @Nullable CabHolder cabHolder) {
        super(activity, dataSet, itemLayoutRes, usePalette, cabHolder);
        this.showAlbumImage = false; // We don't want to load it in this adapter
        this.current = current;
    }

    @Override
    protected SongAdapter.ViewHolder createViewHolder(View view) {
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongAdapter.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        if (holder.imageText != null) {
            holder.imageText.setText(String.valueOf(position - current));
        }

        if (holder.getItemViewType() == HISTORY) {
            setAlpha(holder, 0.5f);
        }
     }

    @Override
    public int getItemViewType(int position) {
        if (position < current) {
            return HISTORY;
        } else if (position > current) {
            return UP_NEXT;
        }
        return CURRENT;
    }

    public void swapDataSet(ArrayList<Song> dataSet, int position) {
        this.dataSet = dataSet;
        current = position;
        notifyDataSetChanged();
    }

    public void setCurrent(int current) {
        this.current = current;
        notifyDataSetChanged();
    }

    protected void setAlpha(SongAdapter.ViewHolder holder, float alpha) {
        if (holder.image != null) {
            holder.image.setAlpha(alpha);
        }
        if (holder.title != null) {
            holder.title.setAlpha(alpha);
        }
        if (holder.text != null) {
            holder.text.setAlpha(alpha);
        }
        if (holder.imageText != null) {
            holder.imageText.setAlpha(alpha);
        }
        if (holder.paletteColorContainer != null) {
            holder.paletteColorContainer.setAlpha(alpha);
        }
    }

    @Override
    public boolean onCheckCanStartDrag(ViewHolder holder, int position, int x, int y) {
        return ViewUtil.hitTest(holder.imageText, x, y);
    }

    @Override
    public ItemDraggableRange onGetItemDraggableRange(ViewHolder holder, int position) {
        return null;
    }

    public void onItemDragStarted(int position) {
        notifyDataSetChanged();
    }

    @Override
    public void onItemDragFinished(int fromPosition, int toPosition, boolean result) {
        notifyDataSetChanged();
    }

    @Override
    public void onMoveItem(int fromPosition, int toPosition) {
        MusicPlayerRemote.moveSong(fromPosition, toPosition);
    }

    @Override
    public boolean onCheckCanDrop(int draggingPosition, int dropPosition) {
        return true;
    }

    @Override
    public int onGetSwipeReactionType(ViewHolder holder, int position, int x, int y) {
        // Get a Rect containing the coordinates of the titleScrollView that may be scrolled
        Rect scrollViewRect = holder.titleScrollview.getScrollViewRect();

        // Handle the 16dp margin top
        float pixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16,
                holder.titleScrollview.getContext().getResources().getDisplayMetrics());

        // Was the titleScrollView touched?
        boolean touchedScrollView =
                x > scrollViewRect.left && x < scrollViewRect.right &&
                        y < (scrollViewRect.bottom - scrollViewRect.top + pixels);

        // Check if the left part of the song, that can be dragged to rearrange the songs,
        // was touched: if yes, do not allow swiping
        boolean onCheckCanStartDrag = onCheckCanStartDrag(holder, position, x, y);

        // Is the current title horizontally scrollable?
        boolean isScrollable = holder.titleScrollview.isScrollable();

        // If the left part was touched, or the titleScrollView was touched: forbid swiping
        if (onCheckCanStartDrag || isScrollable && touchedScrollView) {
            return SwipeableItemConstants.REACTION_CAN_NOT_SWIPE_BOTH_H;
        } else {
            // Else, allow swiping
            return SwipeableItemConstants.REACTION_CAN_SWIPE_BOTH_H;
        }
    }

    @Override
    public void onSwipeItemStarted(ViewHolder holder, int position) {
    }

    @Override
    public void onSetSwipeBackground(ViewHolder holder, int i, int i1) {
            holder.itemView.setBackgroundColor(getBackgroundColor(activity));
            holder.dummyContainer.setBackgroundColor(ATHUtil.resolveColor(activity, R.attr.cardBackgroundColor));
    }

    @Override
    public SwipeResultAction onSwipeItem(ViewHolder holder, int position, @SwipeableItemResults int result) {
        if (result == SwipeableItemConstants.RESULT_CANCELED) {
            return new SwipeResultActionDefault();
        } else {
            return new SwipedResultActionRemoveItem(this, position, activity);
        }
    }

    public class ViewHolder extends SongAdapter.ViewHolder {
        @DraggableItemStateFlags
        private int mDragStateFlags;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        protected int getSongMenuRes() {
            return R.menu.menu_item_playing_queue_song;
        }

        @Override
        protected boolean onSongMenuItemClick(MenuItem item) {
            if (item.getItemId() == R.id.action_remove_from_playing_queue) {
                // If song removed was the playing song, then play the next song
                if (MusicPlayerRemote.isPlaying(getSong()))
                {
                    MusicPlayerRemote.playNextSong();
                }

                final int position = getAdapterPosition();
                MusicPlayerRemote.removeFromQueue(position);
                return true;
            }
            return super.onSongMenuItemClick(item);
        }

        @Override
        public void setDragStateFlags(int flags) {
            mDragStateFlags = flags;
        }

        @Override
        public int getDragStateFlags() {
            return mDragStateFlags;
        }

        @Override
        public View getSwipeableContainerView() {
            return dummyContainer;
        }
    }

    static class SwipedResultActionRemoveItem extends SwipeResultActionRemoveItem {
        private PlayingQueueAdapter adapter;
        private int position;
        private Song songToRemove;
        private AppCompatActivity activity;
        private boolean isPlaying;
        private int songProgressMillis;

        public SwipedResultActionRemoveItem(PlayingQueueAdapter adapter, int position, AppCompatActivity activity) {
            this.adapter = adapter;
            this.position = position;
            this.activity = activity;
            this.isPlaying = MusicPlayerRemote.isPlaying();
        }

        @Override
        protected void onPerformAction() {
            currentlyShownSnackbar = null;
        }
        @Override
        protected void onSlideAnimationEnd() {
            initializeSnackBar(adapter, position, activity, isPlaying);
            songToRemove = adapter.dataSet.get(position);

            //If song removed was the playing song, then play the next song
            if (MusicPlayerRemote.isPlaying(songToRemove)) {
                MusicPlayerRemote.playNextSong();
            }

            //Swipe animation is much smoother when we do the heavy lifting after it's completed
            adapter.setSongToRemove(songToRemove);
            MusicPlayerRemote.removeFromQueue(songToRemove);
        }
    }

    public static int getBackgroundColor(AppCompatActivity activity){
        //TODO: Find a better way to get the album background color
        TextView tV = activity.findViewById(R.id.player_queue_sub_header);
        if(tV != null){
            return tV.getCurrentTextColor();
        } else {
            return ATHUtil.resolveColor(activity, R.attr.cardBackgroundColor);
        }
    }

    public static void initializeSnackBar(final PlayingQueueAdapter adapter,final int position,
                                          final AppCompatActivity activity, final Boolean isPlaying){

        CharSequence snackBarTitle = activity.getString(R.string.snack_bar_title_removed_song);

        Snackbar snackbar = Snackbar.make(activity.findViewById(R.id.content_container),
                snackBarTitle,
                Snackbar.LENGTH_LONG);

        TextView songTitle = snackbar.getView().findViewById(R.id.snackbar_text);

        songTitle.setSingleLine();
        songTitle.setEllipsize(TextUtils.TruncateAt.END);
        songTitle.setText(adapter.dataSet.get(position).title + " " + snackBarTitle);

        snackbar.setAction(R.string.snack_bar_action_undo, v -> {
            MusicPlayerRemote.addSong(position,adapter.getSongToRemove());
            //If playing and currently playing song is removed, then added back, then play it at
            //current song progress
            if (isPlaying && position == 0) {
                MusicPlayerRemote.playSongAt(0);
            }
        });
        snackbar.setActionTextColor(getBackgroundColor(activity));
        snackbar.show();


        //Fixes Snackbar not showing when it replaces another Snackbar
        //See: https://stackoverflow.com/questions/43680655/snackbar-sometimes-doesnt-show-up-when-it-replaces-another-one
        currentlyShownSnackbar = snackbar;

    }

    public void setSongToRemove (@NonNull Song song){
        songToRemove = song;
    }

    public Song getSongToRemove(){
        return songToRemove;
    }
}
