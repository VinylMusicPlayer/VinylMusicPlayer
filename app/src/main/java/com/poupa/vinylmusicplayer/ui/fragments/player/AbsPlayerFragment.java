package com.poupa.vinylmusicplayer.ui.fragments.player;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.audiofx.AudioEffect;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

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
import com.kabouzeid.appthemehelper.util.ToolbarContentTintHelper;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.adapter.song.PlayingQueueAdapter;
import com.poupa.vinylmusicplayer.dialogs.AddToPlaylistDialog;
import com.poupa.vinylmusicplayer.dialogs.CreatePlaylistDialog;
import com.poupa.vinylmusicplayer.dialogs.LyricsDialog;
import com.poupa.vinylmusicplayer.dialogs.SleepTimerDialog;
import com.poupa.vinylmusicplayer.dialogs.SongDetailDialog;
import com.poupa.vinylmusicplayer.dialogs.SongShareDialog;
import com.poupa.vinylmusicplayer.helper.MusicPlayerRemote;
import com.poupa.vinylmusicplayer.interfaces.PaletteColorHolder;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.model.lyrics.Lyrics;
import com.poupa.vinylmusicplayer.ui.activities.tageditor.AbsTagEditorActivity;
import com.poupa.vinylmusicplayer.ui.activities.tageditor.SongTagEditorActivity;
import com.poupa.vinylmusicplayer.ui.fragments.AbsMusicServiceFragment;
import com.poupa.vinylmusicplayer.util.ImageUtil;
import com.poupa.vinylmusicplayer.util.MusicUtil;
import com.poupa.vinylmusicplayer.util.NavigationUtil;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

public abstract class AbsPlayerFragment
        extends AbsMusicServiceFragment
        implements PlayerAlbumCoverFragment.Callbacks, Toolbar.OnMenuItemClickListener, PaletteColorHolder
{
    @Nullable
    private Callbacks callbacks;

    protected PlayingQueueAdapter playingQueueAdapter;
    private RecyclerView.Adapter wrappedAdapter;
    protected RecyclerViewDragDropManager recyclerViewDragDropManager;
    private RecyclerViewSwipeManager recyclerViewSwipeManager;
    protected LinearLayoutManager layoutManager;

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

    protected void setUpRecyclerView(RecyclerView recyclerView, final SlidingUpPanelLayout slidingUpPanelLayout) {
        RecyclerViewTouchActionGuardManager recyclerViewTouchActionGuardManager = new RecyclerViewTouchActionGuardManager();
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
            NavigationUtil.openEqualizer(requireActivity());
            return true;
        } else if (itemId == R.id.action_add_to_playlist) {
            AddToPlaylistDialog.create(song).show(getParentFragmentManager(), "ADD_PLAYLIST");
            return true;
        } else if (itemId == R.id.action_clear_playing_queue) {
            MusicPlayerRemote.clearQueue();
            return true;
        } else if (itemId == R.id.action_save_playing_queue) {
            CreatePlaylistDialog.create(MusicPlayerRemote.getPlayingQueue()).show(requireActivity().getSupportFragmentManager(), "ADD_TO_PLAYLIST");
            return true;
        } else if (itemId == R.id.action_tag_editor) {
            Intent intent = new Intent(requireActivity(), SongTagEditorActivity.class);
            intent.putExtra(AbsTagEditorActivity.EXTRA_ID, song.id);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.action_details) {
            SongDetailDialog.create(song).show(getParentFragmentManager(), "SONG_DETAIL");
            return true;
        } else if (itemId == R.id.action_go_to_album) {
            NavigationUtil.goToAlbum(requireActivity(), song.albumId);
            return true;
        } else if (itemId == R.id.action_go_to_artist) {
            NavigationUtil.goToArtist(requireActivity(), song.artistNames);
            return true;
        } else if (itemId == R.id.action_show_lyrics) {
            if (lyrics != null) {
                LyricsDialog.create(lyrics).show(getParentFragmentManager(), "LYRICS");
            }
            return true;
        }

        return false;
    }

    private static boolean isToolbarShown = true;
    protected Toolbar toolbar;
    protected FrameLayout toolbarContainer;

    private static void showToolbar(@Nullable final View toolbar) {
        if (toolbar == null) return;

        isToolbarShown = true;

        toolbar.setVisibility(View.VISIBLE);
        toolbar.animate().alpha(1.0f).setDuration(PlayerAlbumCoverFragment.VISIBILITY_ANIM_DURATION);
    }

    private static void hideToolbar(@Nullable final View toolbar) {
        if (toolbar == null) return;

        isToolbarShown = false;

        toolbar.animate().alpha(0f)
                .setDuration(PlayerAlbumCoverFragment.VISIBILITY_ANIM_DURATION)
                .withEndAction(() -> toolbar.setVisibility(View.GONE));
    }

    private static void toggleToolbar(@Nullable final View toolbar) {
        if (isToolbarShown) {
            hideToolbar(toolbar);
        } else {
            showToolbar(toolbar);
        }
    }

    protected static void checkToggleToolbar(@Nullable final View toolbar) {
        if (toolbar != null && !isToolbarShown && toolbar.getVisibility() != View.GONE) {
            hideToolbar(toolbar);
        } else if (toolbar != null && isToolbarShown && toolbar.getVisibility() != View.VISIBLE) {
            showToolbar(toolbar);
        }
    }

    private boolean hasEqualizer() {
        final Intent effects = new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
        PackageManager pm = requireActivity().getPackageManager();
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

    public interface Callbacks {
        void onPaletteColorChanged();
    }

    @Override
    public void onColorChanged(int color) {
        if (callbacks != null) {
            callbacks.onPaletteColorChanged();
        }
    }

    @Override
    public void onToolbarToggled() {
        toggleToolbar(toolbarContainer);
    }

    private PlayerAlbumCoverFragment playerAlbumCoverFragment;

    protected void setUpSubFragments() {
        playerAlbumCoverFragment = (PlayerAlbumCoverFragment) getChildFragmentManager().findFragmentById(R.id.player_album_cover_fragment);
        if (playerAlbumCoverFragment == null) {throw new AssertionError("No fragment with id=" + R.id.player_album_cover_fragment);}
        playerAlbumCoverFragment.setCallbacks(this);
    }

    private Lyrics lyrics;
    private AsyncTask<Void, Void, Lyrics> updateLyricsAsyncTask;
    private AsyncTask<Song, Void, Boolean> updateIsFavoriteTask;

    private void toggleFavorite(Song song) {
        MusicUtil.toggleFavorite(requireActivity(), song);

        if (song.id == MusicPlayerRemote.getCurrentSong().id) {
            if (MusicUtil.isFavorite(requireActivity(), song)) {
                playerAlbumCoverFragment.showHeartAnimation();
            }
            updateIsFavorite();
        }
    }

    protected void updateIsFavorite() {
        if (updateIsFavoriteTask != null) {updateIsFavoriteTask.cancel(false);}
        updateIsFavoriteTask = new AsyncTask<Song, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Song... params) {
                Activity activity = getActivity();
                if (activity != null) {
                    return MusicUtil.isFavorite(getActivity(), params[0]);
                } else {
                    cancel(false);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Boolean isFavorite) {
                Activity activity = getActivity();
                if (activity != null) {
                    int res = isFavorite ? R.drawable.ic_favorite_white_24dp : R.drawable.ic_favorite_border_white_24dp;
                    int color = ToolbarContentTintHelper.toolbarContentColor(activity, Color.TRANSPARENT);
                    Drawable drawable = ImageUtil.getTintedVectorDrawable(activity, res, color);
                    toolbar.getMenu().findItem(R.id.action_toggle_favorite)
                            .setIcon(drawable)
                            .setTitle(isFavorite ? getString(R.string.action_remove_from_favorites) : getString(R.string.action_add_to_favorites));
                }
            }
        }.execute(MusicPlayerRemote.getCurrentSong());
    }

    protected void updateLyrics() {
        if (updateLyricsAsyncTask != null) {updateLyricsAsyncTask.cancel(false);}

        final Song song = MusicPlayerRemote.getCurrentSong();
        if (song.equals(Song.EMPTY_SONG)) return;

        updateLyricsAsyncTask = new AsyncTask<Void, Void, Lyrics>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                lyrics = null;
                playerAlbumCoverFragment.setLyrics(null);
                toolbar.getMenu().removeItem(R.id.action_show_lyrics);
            }

            @Override
            protected Lyrics doInBackground(Void... params) {
                final Context context = getContext();
                if (context == null) {
                    return null;
                }

                String data = MusicUtil.getLyrics(context, song);
                if (TextUtils.isEmpty(data)) {
                    return null;
                }
                return Lyrics.parse(song, data);
            }

            @Override
            protected void onPostExecute(Lyrics l) {
                lyrics = l;
                playerAlbumCoverFragment.setLyrics(lyrics);
                if (lyrics == null) {
                    if (toolbar != null) {
                        toolbar.getMenu().removeItem(R.id.action_show_lyrics);
                    }
                } else {
                    Activity activity = getActivity();
                    if (toolbar != null && activity != null)
                        if (toolbar.getMenu().findItem(R.id.action_show_lyrics) == null) {
                            int color = ToolbarContentTintHelper.toolbarContentColor(activity, Color.TRANSPARENT);
                            Drawable drawable = ImageUtil.getTintedVectorDrawable(activity, R.drawable.ic_comment_text_outline_white_24dp, color);
                            toolbar.getMenu()
                                    .add(Menu.NONE, R.id.action_show_lyrics, Menu.NONE, R.string.action_show_lyrics)
                                    .setIcon(drawable)
                                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                        }
                }
            }

            @Override
            protected void onCancelled(Lyrics s) {
                onPostExecute(null);
            }
        }.execute();
    }
}
