package com.poupa.vinylmusicplayer.ui.fragments.player;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.audiofx.AudioEffect;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.h6ah4i.android.widget.advrecyclerview.animator.DraggableItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.adapter.song.PlayingQueueAdapter;
import com.poupa.vinylmusicplayer.dialogs.AddToPlaylistDialog;
import com.poupa.vinylmusicplayer.dialogs.CreatePlaylistDialog;
import com.poupa.vinylmusicplayer.dialogs.SleepTimerDialog;
import com.poupa.vinylmusicplayer.dialogs.SongDetailDialog;
import com.poupa.vinylmusicplayer.dialogs.SongShareDialog;
import com.poupa.vinylmusicplayer.helper.MusicPlayerRemote;
import com.poupa.vinylmusicplayer.interfaces.PaletteColorHolder;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.ui.activities.tageditor.AbsTagEditorActivity;
import com.poupa.vinylmusicplayer.ui.activities.tageditor.SongTagEditorActivity;
import com.poupa.vinylmusicplayer.ui.fragments.AbsMusicServiceFragment;
import com.poupa.vinylmusicplayer.util.MusicUtil;
import com.poupa.vinylmusicplayer.util.NavigationUtil;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

public abstract class AbsPlayerFragment extends AbsMusicServiceFragment implements Toolbar.OnMenuItemClickListener, PaletteColorHolder {

    private Callbacks callbacks;
    private static boolean isToolbarShown = true;

    protected Toolbar toolbar;

    public PlayingQueueAdapter playingQueueAdapter;
    public RecyclerView.Adapter wrappedAdapter;
    public RecyclerViewDragDropManager recyclerViewDragDropManager;
    public RecyclerViewSwipeManager recyclerViewSwipeManager;
    public RecyclerViewTouchActionGuardManager recyclerViewTouchActionGuardManager;
    public LinearLayoutManager layoutManager;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            callbacks = (Callbacks) context;
        } catch (ClassCastException e) {
            throw new RuntimeException(context.getClass().getSimpleName() + " must implement " + Callbacks.class.getSimpleName());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callbacks = null;
    }

    public void setUpRecyclerView(RecyclerView recyclerView, final SlidingUpPanelLayout slidingUpPanelLayout) {
        recyclerViewTouchActionGuardManager = new RecyclerViewTouchActionGuardManager();
        recyclerViewSwipeManager = new RecyclerViewSwipeManager();
        recyclerViewDragDropManager = new RecyclerViewDragDropManager();

        final GeneralItemAnimator animator = new DraggableItemAnimator();

        // Change animations are enabled by default since support-v7-recyclerview v22.
        // Disable the change animation in order to make turning back animation of swiped item works properly.
        animator.setSupportsChangeAnimations(false);

        playingQueueAdapter = new PlayingQueueAdapter(
                ((AppCompatActivity) getActivity()),
                MusicPlayerRemote.getPlayingQueue(),
                MusicPlayerRemote.getPosition(),
                false,
                null);
        wrappedAdapter = recyclerViewDragDropManager.createWrappedAdapter(playingQueueAdapter);
        wrappedAdapter = recyclerViewSwipeManager.createWrappedAdapter(wrappedAdapter);

        layoutManager = new LinearLayoutManager(getActivity());

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(wrappedAdapter);
        recyclerView.setItemAnimator(animator);

        recyclerViewTouchActionGuardManager.attachRecyclerView(recyclerView);
        recyclerViewSwipeManager.attachRecyclerView(recyclerView);
        recyclerViewDragDropManager.attachRecyclerView(recyclerView);

        recyclerViewSwipeManager.setOnItemSwipeEventListener(new RecyclerViewSwipeManager.OnItemSwipeEventListener() {
            @Override
            public void onItemSwipeStarted(int i) {
                slidingUpPanelLayout.setTouchEnabled(false);
            }

            @Override
            public void onItemSwipeFinished(int i, int i1, int i2) {
                slidingUpPanelLayout.setTouchEnabled(true);
            }
        });

        layoutManager.scrollToPositionWithOffset(MusicPlayerRemote.getPosition() + 1, 0);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        final @NonNull Song song = MusicPlayerRemote.getCurrentSong();
        int itemId = item.getItemId();
        if (itemId == R.id.action_sleep_timer) {
            new SleepTimerDialog().show(getParentFragmentManager(), "SET_SLEEP_TIMER");
            return true;
        } else if (itemId == R.id.action_toggle_favorite) {
            toggleFavorite(song);
            return true;
        } else if (itemId == R.id.action_share) {
            SongShareDialog.create(song).show(getParentFragmentManager(), "SHARE_SONG");
            return true;
        } else if (itemId == R.id.action_equalizer) {
            NavigationUtil.openEqualizer(getActivity());
            return true;
        } else if (itemId == R.id.action_add_to_playlist) {
            AddToPlaylistDialog.create(song).show(getParentFragmentManager(), "ADD_PLAYLIST");
            return true;
        } else if (itemId == R.id.action_clear_playing_queue) {
            MusicPlayerRemote.clearQueue();
            return true;
        } else if (itemId == R.id.action_save_playing_queue) {
            CreatePlaylistDialog.create(MusicPlayerRemote.getPlayingQueue()).show(getActivity().getSupportFragmentManager(), "ADD_TO_PLAYLIST");
            return true;
        } else if (itemId == R.id.action_tag_editor) {
            Intent intent = new Intent(getActivity(), SongTagEditorActivity.class);
            intent.putExtra(AbsTagEditorActivity.EXTRA_ID, song.id);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.action_details) {
            SongDetailDialog.create(song).show(getParentFragmentManager(), "SONG_DETAIL");
            return true;
        } else if (itemId == R.id.action_go_to_album) {
            NavigationUtil.goToAlbum(getActivity(), song.albumId);
            return true;
        } else if (itemId == R.id.action_go_to_artist) {
            NavigationUtil.goToArtist(getActivity(), song.artistId);
            return true;
        }
        return false;
    }

    protected void toggleFavorite(Song song) {
        MusicUtil.toggleFavorite(getActivity(), song);
    }

    protected boolean isToolbarShown() {
        return isToolbarShown;
    }

    protected void setToolbarShown(boolean toolbarShown) {
        isToolbarShown = toolbarShown;
    }

    protected void showToolbar(@Nullable final View toolbar) {
        if (toolbar == null) return;

        setToolbarShown(true);

        toolbar.setVisibility(View.VISIBLE);
        toolbar.animate().alpha(1f).setDuration(PlayerAlbumCoverFragment.VISIBILITY_ANIM_DURATION);
    }

    protected void hideToolbar(@Nullable final View toolbar) {
        if (toolbar == null) return;

        setToolbarShown(false);

        toolbar.animate().alpha(0f)
                .setDuration(PlayerAlbumCoverFragment.VISIBILITY_ANIM_DURATION)
                .withEndAction(() -> toolbar.setVisibility(View.GONE));
    }

    protected void toggleToolbar(@Nullable final View toolbar) {
        if (isToolbarShown()) {
            hideToolbar(toolbar);
        } else {
            showToolbar(toolbar);
        }
    }

    protected void checkToggleToolbar(@Nullable final View toolbar) {
        if (toolbar != null && !isToolbarShown() && toolbar.getVisibility() != View.GONE) {
            hideToolbar(toolbar);
        } else if (toolbar != null && isToolbarShown() && toolbar.getVisibility() != View.VISIBLE) {
            showToolbar(toolbar);
        }
    }

    protected boolean hasEqualizer() {
        final Intent effects = new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
        PackageManager pm = getActivity().getPackageManager();
        ResolveInfo ri = pm.resolveActivity(effects, 0);
        return ri != null;
    }

    protected void setUpPlayerToolbar() {
        // Hide equalizer if it is unavailable
        if (!hasEqualizer()) {
            MenuItem equalizerItem = toolbar.getMenu().findItem(R.id.action_equalizer);
            equalizerItem.setVisible(false);
        }
    }

    public void onShow(){
        recyclerViewDragDropManager.setCheckCanDropEnabled(true);
    }

    public void onHide(){
        recyclerViewDragDropManager.setCheckCanDropEnabled(false);
        recyclerViewSwipeManager.cancelSwipe();
    }

    @Override
    public void onDestroyView() {
        if (recyclerViewDragDropManager != null) {
            recyclerViewDragDropManager.release();
            recyclerViewDragDropManager = null;
        }
        if (recyclerViewSwipeManager != null) {
            recyclerViewSwipeManager.release();
            recyclerViewSwipeManager = null;
        }

        if (wrappedAdapter != null) {
            WrapperAdapterUtils.releaseAll(wrappedAdapter);
            wrappedAdapter = null;
        }
        playingQueueAdapter = null;
        layoutManager = null;
        super.onDestroyView();
    }

    public abstract boolean onBackPressed();

    public Callbacks getCallbacks() {
        return callbacks;
    }

    public interface Callbacks {
        void onPaletteColorChanged();
    }
}
