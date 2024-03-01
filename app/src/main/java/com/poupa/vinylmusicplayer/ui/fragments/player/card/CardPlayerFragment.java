package com.poupa.vinylmusicplayer.ui.fragments.player.card;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.util.ATHUtil;
import com.kabouzeid.appthemehelper.util.ColorUtil;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.adapter.base.MediaEntryViewHolder;
import com.poupa.vinylmusicplayer.databinding.FragmentCardPlayerBinding;
import com.poupa.vinylmusicplayer.databinding.ItemListBinding;
import com.poupa.vinylmusicplayer.dialogs.SongShareDialog;
import com.poupa.vinylmusicplayer.helper.MusicPlayerRemote;
import com.poupa.vinylmusicplayer.helper.menu.SongMenuHelper;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.ui.activities.base.AbsSlidingMusicPanelActivity;
import com.poupa.vinylmusicplayer.ui.fragments.player.AbsPlayerFragment;
import com.poupa.vinylmusicplayer.util.MusicUtil;
import com.poupa.vinylmusicplayer.util.PlayingSongDecorationUtil;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;
import com.poupa.vinylmusicplayer.util.Util;
import com.poupa.vinylmusicplayer.util.ViewUtil;
import com.poupa.vinylmusicplayer.util.VinylMusicPlayerColorUtil;
import com.poupa.vinylmusicplayer.views.WidthFitSquareLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

public class CardPlayerFragment extends AbsPlayerFragment implements SlidingUpPanelLayout.PanelSlideListener {
    private SlidingUpPanelLayout slidingUpPanelLayout;
    private RecyclerView recyclerView;
    private CardView playingQueueCard;
    private View colorBackground;
    private TextView playerQueueSubHeader;

    private int lastColor;

    private CardPlayerPlaybackControlsFragment playbackControlsFragment;

    private Impl impl;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (Util.isLandscape(getResources())) {
            impl = new LandscapeImpl(this);
        } else {
            impl = new PortraitImpl(this);
        }

        FragmentCardPlayerBinding binding = FragmentCardPlayerBinding.inflate(inflater, container, false);
        toolbar = binding.playerToolbar;
        toolbarContainer = binding.toolbarContainer;
        slidingUpPanelLayout = binding.playerSlidingLayout;
        recyclerView = binding.playerRecyclerView;
        playingQueueCard = binding.playingQueueCard;
        colorBackground = binding.colorBackground;
        playerQueueSubHeader = binding.playerQueueSubHeader;

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        impl.init();

        setUpPlayerToolbar();
        setUpSubFragments();

        setUpRecyclerView(recyclerView,slidingUpPanelLayout);

        slidingUpPanelLayout.addPanelSlideListener(this);
        slidingUpPanelLayout.setAntiDragView(view.findViewById(R.id.draggable_area));

        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                impl.setUpPanelAndAlbumCoverHeight();
            }
        });

        // for some reason the xml attribute doesn't get applied here.
        playingQueueCard.setCardBackgroundColor(ATHUtil.resolveColor(requireActivity(), R.attr.cardBackgroundColor));
    }

    @Override
    public void onDestroyView() {
        if (slidingUpPanelLayout != null) {
            slidingUpPanelLayout.removePanelSlideListener(this);
        }

        if (recyclerView != null) {
            recyclerView.setItemAnimator(null);
            recyclerView.setAdapter(null);
            recyclerView = null;
        }

        super.onDestroyView();
    }

    @Override
    public void onPause() {
        if (recyclerViewDragDropManager != null) {
            recyclerViewDragDropManager.cancelDrag();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkToggleToolbar(toolbarContainer);
    }

    @Override
    public void onServiceConnected() {
        updateQueue();
        updateCurrentSong();
        updateIsFavorite();
        updateLyrics();
    }

    @Override
    public void onPlayingMetaChanged() {
        updateCurrentSong();
        updateIsFavorite();
        updateQueuePosition();
        updateLyrics();
    }

    @Override
    public void onPlayStateChanged() {
        updateCurrentSong();
    }

    @Override
    public void onQueueChanged() {
        updateQueue();
    }

    @Override
    public void onMediaStoreChanged() {
        // TODO If a song is removed from the MediaStore, this is not reflected in the playing queue until restart
        // TODO If a song is updated (tags edited), this is not reflected in the playing queue until restart
        updateQueue();
    }

    private void updateQueue() {
        playingQueueAdapter.swapDataSet(MusicPlayerRemote.getPlayingQueue(), MusicPlayerRemote.getPosition());
        playerQueueSubHeader.setText(MusicPlayerRemote.getQueueInfoString());
        if (slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
            resetToCurrentPosition();
        }
    }

    private void updateQueuePosition() {
        playingQueueAdapter.setCurrent(MusicPlayerRemote.getPosition());
        playerQueueSubHeader.setText(MusicPlayerRemote.getQueueInfoString());
        if (slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
            resetToCurrentPosition();
        }
    }

    private void updateCurrentSong() {
        impl.updateCurrentSong(MusicPlayerRemote.getCurrentSong());

        // give the adapter a chance to update the decoration
        recyclerView.getAdapter().notifyItemChanged(MusicPlayerRemote.getPosition());
    }

    @Override
    protected void setUpSubFragments() {
        playbackControlsFragment = (CardPlayerPlaybackControlsFragment) getChildFragmentManager().findFragmentById(R.id.playback_controls_fragment);
        super.setUpSubFragments();
    }

    protected void setUpPlayerToolbar() {
        toolbar.inflateMenu(R.menu.menu_player);
        toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
        toolbar.setOnMenuItemClickListener(this);

        super.setUpPlayerToolbar();
    }

    @Override
    @ColorInt
    public int getPaletteColor() {
        return lastColor;
    }

    private void animateColorChange(final int newColor) {
        impl.animateColorChange(newColor);
        lastColor = newColor;
    }

    @Override
    public void onShow() {
        playbackControlsFragment.show();
    }

    @Override
    public void onHide() {
        playbackControlsFragment.hide();
        onBackPressed();
    }

    @Override
    public boolean onBackPressed() {
        boolean wasExpanded = false;
        if (slidingUpPanelLayout != null) {
            wasExpanded = slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED;
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }

        return wasExpanded;
    }

    @Override
    public void onColorChanged(int color) {
        animateColorChange(color);
        playbackControlsFragment.setDark(ColorUtil.isColorLight(color));

        super.onColorChanged(color);
    }

    @Override
    public void onPanelSlide(View panel, float slideOffset) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            float density = getResources().getDisplayMetrics().density;
            float cardElevation = (6 * slideOffset + 2) * density;
            if (isNotValidElevation(cardElevation)) return; // we have received some crash reports in setCardElevation()
            playingQueueCard.setCardElevation(cardElevation);

            float buttonElevation = (2 * Math.max(0, (1 - (slideOffset * 16))) + 2) * density;
            if (isNotValidElevation(buttonElevation)) return;
            playbackControlsFragment.playPauseFab.setElevation(buttonElevation);
        }
    }

    private static boolean isNotValidElevation(float elevation) {
        return !(elevation >= -Float.MAX_VALUE) || !(elevation <= Float.MAX_VALUE);
    }

    @Override
    public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
        switch (newState) {
            case COLLAPSED:
                onPanelCollapsed(panel);
                break;
            case ANCHORED:
                slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED); // this fixes a bug where the panel would get stuck for some reason
                break;
        }
    }

    public void onPanelCollapsed(View panel) {
        resetToCurrentPosition();
    }

    private void resetToCurrentPosition() {
        recyclerView.stopScroll();
        layoutManager.scrollToPositionWithOffset(MusicPlayerRemote.getPosition() + 1, 0);
    }

    interface Impl {
        void init();

        void updateCurrentSong(@NonNull final Song song);

        void animateColorChange(final int newColor);

        void setUpPanelAndAlbumCoverHeight();
    }

    private abstract static class BaseImpl implements Impl {
        protected CardPlayerFragment fragment;

        public BaseImpl(CardPlayerFragment fragment) {
            this.fragment = fragment;
        }

        public AnimatorSet createDefaultColorChangeAnimatorSet(int newColor) {
            Animator backgroundAnimator;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //noinspection ConstantConditions
                int x = (int) (fragment.playbackControlsFragment.playPauseFab.getX() + fragment.playbackControlsFragment.playPauseFab.getWidth() / 2 + fragment.playbackControlsFragment.getView().getX());
                int y = (int) (fragment.playbackControlsFragment.playPauseFab.getY() + fragment.playbackControlsFragment.playPauseFab.getHeight() / 2 + fragment.playbackControlsFragment.getView().getY() + fragment.playbackControlsFragment.progressSlider.getHeight());
                float startRadius = Math.max(fragment.playbackControlsFragment.playPauseFab.getWidth() / 2, fragment.playbackControlsFragment.playPauseFab.getHeight() / 2);
                float endRadius = Math.max(fragment.colorBackground.getWidth(), fragment.colorBackground.getHeight());
                fragment.colorBackground.setBackgroundColor(newColor);
                backgroundAnimator = ViewAnimationUtils.createCircularReveal(fragment.colorBackground, x, y, startRadius, endRadius);
            } else {
                backgroundAnimator = ViewUtil.createBackgroundColorTransition(fragment.colorBackground, fragment.lastColor, newColor);
            }

            AnimatorSet animatorSet = new AnimatorSet();

            animatorSet.play(backgroundAnimator);

            int adjustedLastColor = fragment.lastColor;
            int adjustedNewColor = newColor;

            int backgroundColor = ATHUtil.resolveColor(fragment.requireActivity(), R.attr.cardBackgroundColor);
            adjustedLastColor = VinylMusicPlayerColorUtil.getContrastedColor(adjustedLastColor, backgroundColor);
            adjustedNewColor = VinylMusicPlayerColorUtil.getContrastedColor(adjustedNewColor, backgroundColor);

            Animator subHeaderAnimator = ViewUtil.createTextColorTransition(fragment.playerQueueSubHeader, adjustedLastColor, adjustedNewColor);
            animatorSet.play(subHeaderAnimator);

            // Workaround for a bug https://github.com/AdrienPoupa/VinylMusicPlayer/issues/620
            for (Animator animator : animatorSet.getChildAnimations()){
                animator.setDuration(ViewUtil.VINYL_MUSIC_PLAYER_ANIM_TIME);
            }

            return animatorSet;
        }
    }

    @SuppressWarnings("ConstantConditions")
    private static class PortraitImpl extends BaseImpl {
        MediaEntryViewHolder currentSongViewHolder;
        Song currentSong = Song.EMPTY_SONG;

        public PortraitImpl(CardPlayerFragment fragment) {
            super(fragment);
        }

        @Override
        public void init() {
            ItemListBinding binding = ItemListBinding.bind(fragment.getView().findViewById(R.id.current_song));
            currentSongViewHolder = new MediaEntryViewHolder(binding);

            currentSongViewHolder.separator.setVisibility(View.VISIBLE);
            currentSongViewHolder.shortSeparator.setVisibility(View.GONE);
            currentSongViewHolder.image.setScaleType(ImageView.ScaleType.CENTER);
            currentSongViewHolder.image.setColorFilter(ATHUtil.resolveColor(fragment.getActivity(), R.attr.iconColor, ThemeStore.textColorSecondary(fragment.getActivity())), PorterDuff.Mode.SRC_IN);
            currentSongViewHolder.image.setImageResource(PlayingSongDecorationUtil.sIconPlaying);
            currentSongViewHolder.itemView.setOnClickListener(v -> {
                // toggle the panel
                if (fragment.slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    fragment.slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                } else if (fragment.slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    fragment.slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                }
            });
            currentSongViewHolder.menu.setOnClickListener(new SongMenuHelper.OnClickSongMenu((AppCompatActivity) fragment.getActivity()) {
                @Override
                public Song getSong() {
                    return currentSong;
                }

                public int getMenuRes() {
                    return R.menu.menu_item_playing_queue_song;
                }

                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    final int itemId = item.getItemId();
                    if (itemId == R.id.action_remove_from_playing_queue) {
                        MusicPlayerRemote.removeFromQueue(MusicPlayerRemote.getPosition());
                        return true;
                    } else if (itemId == R.id.action_share) {
                        SongShareDialog.create(getSong()).show(fragment.getParentFragmentManager(), "SONG_SHARE_DIALOG");
                        return true;
                    }
                    return super.onMenuItemClick(item);
                }
            });
            currentSongViewHolder.title.setTypeface(null, Typeface.BOLD);
        }

        @Override
        public void setUpPanelAndAlbumCoverHeight() {
            WidthFitSquareLayout albumCoverContainer = fragment.getView().findViewById(R.id.album_cover_container);

            final int availablePanelHeight = fragment.slidingUpPanelLayout.getHeight() - fragment.getView().findViewById(R.id.player_content).getHeight() + (int) ViewUtil.convertDpToPixel(8, fragment.getResources());
            final int minPanelHeight = (int) ViewUtil.convertDpToPixel(72 + 24, fragment.getResources());
            if (availablePanelHeight < minPanelHeight) {
                albumCoverContainer.getLayoutParams().height = albumCoverContainer.getHeight() - (minPanelHeight - availablePanelHeight);
                albumCoverContainer.forceSquare(false);
            }
            fragment.slidingUpPanelLayout.setPanelHeight(Math.max(minPanelHeight, availablePanelHeight));

            ((AbsSlidingMusicPanelActivity) fragment.getActivity()).setAntiDragView(fragment.slidingUpPanelLayout.findViewById(R.id.player_panel));
        }

        @Override
        public void updateCurrentSong(@NonNull final Song song) {
            currentSong = song;
            currentSongViewHolder.title.setText(song.title);
            currentSongViewHolder.text.setText(MusicUtil.getSongInfoString(song));

            if (PreferenceUtil.getInstance().animatePlayingSongIcon()) {
                final boolean isPlaying = MusicPlayerRemote.isPlaying(song);
                if (isPlaying) {
                    currentSongViewHolder.image.startAnimation(PlayingSongDecorationUtil.sIconAnimation);
                } else {
                    currentSongViewHolder.image.clearAnimation();
                }
            }
        }

        @Override
        public void animateColorChange(int newColor) {
            fragment.slidingUpPanelLayout.setBackgroundColor(fragment.lastColor);

            createDefaultColorChangeAnimatorSet(newColor).start();
        }
    }

    @SuppressWarnings("ConstantConditions")
    private static class LandscapeImpl extends BaseImpl {
        public LandscapeImpl(CardPlayerFragment fragment) {
            super(fragment);
        }

        @Override
        public void init() {

        }

        @Override
        public void setUpPanelAndAlbumCoverHeight() {
            int panelHeight = fragment.slidingUpPanelLayout.getHeight() - fragment.playbackControlsFragment.getView().getHeight();
            fragment.slidingUpPanelLayout.setPanelHeight(panelHeight);

            ((AbsSlidingMusicPanelActivity) fragment.getActivity()).setAntiDragView(fragment.slidingUpPanelLayout.findViewById(R.id.player_panel));
        }

        @Override
        public void updateCurrentSong(@NonNull final Song song) {
            fragment.toolbar.setTitle(song.title);
            fragment.toolbar.setSubtitle(MusicUtil.getSongInfoString(song));
        }

        @Override
        public void animateColorChange(int newColor) {
            fragment.slidingUpPanelLayout.setBackgroundColor(fragment.lastColor);

            AnimatorSet animatorSet = createDefaultColorChangeAnimatorSet(newColor);
            animatorSet.play(ViewUtil.createBackgroundColorTransition(fragment.toolbar, fragment.lastColor, newColor))
                    .with(ViewUtil.createBackgroundColorTransition(fragment.getView().findViewById(R.id.status_bar), ColorUtil.darkenColor(fragment.lastColor), ColorUtil.darkenColor(newColor)));
            animatorSet.start();
        }
    }
}
