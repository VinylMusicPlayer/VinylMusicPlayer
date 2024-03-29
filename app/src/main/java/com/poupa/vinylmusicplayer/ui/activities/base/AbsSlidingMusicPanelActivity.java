package com.poupa.vinylmusicplayer.ui.activities.base;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.PathInterpolator;

import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.databinding.SlidingMusicPanelLayoutBinding;
import com.poupa.vinylmusicplayer.discog.Discography;
import com.poupa.vinylmusicplayer.helper.MusicPlayerRemote;
import com.poupa.vinylmusicplayer.helper.WeakMethodReference;
import com.poupa.vinylmusicplayer.ui.fragments.player.AbsPlayerFragment;
import com.poupa.vinylmusicplayer.ui.fragments.player.MiniPlayerFragment;
import com.poupa.vinylmusicplayer.ui.fragments.player.NowPlayingScreen;
import com.poupa.vinylmusicplayer.ui.fragments.player.card.CardPlayerFragment;
import com.poupa.vinylmusicplayer.ui.fragments.player.flat.FlatPlayerFragment;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;
import com.poupa.vinylmusicplayer.util.ViewUtil;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public abstract class AbsSlidingMusicPanelActivity extends AbsMusicServiceActivity implements CardPlayerFragment.Callbacks {
    MotionLayout slidingUpPanel;
    BottomSheetBehavior slidingUpPanelLayout;
    private BottomSheetBehavior.BottomSheetCallback slidingUpPanelCallback;

    private int navigationbarColor;
    private int taskColor;
    private boolean lightStatusbar;

    private NowPlayingScreen currentNowPlayingScreen;
    AbsPlayerFragment playerFragment;
    private MiniPlayerFragment miniPlayerFragment;

    private ValueAnimator navigationBarColorAnimator;
    private final ArgbEvaluator argbEvaluator = new ArgbEvaluator();

    private final WeakMethodReference<AbsSlidingMusicPanelActivity> onDiscographyChanged = new WeakMethodReference<>(this, AbsSlidingMusicPanelActivity::reload);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(createContentView());

        currentNowPlayingScreen = PreferenceUtil.getInstance().getNowPlayingScreen();
        final AbsPlayerFragment fragment = switch (currentNowPlayingScreen) {
            case FLAT -> new FlatPlayerFragment();
            case CARD -> new CardPlayerFragment();
        };
        getSupportFragmentManager().beginTransaction().replace(R.id.player_fragment_container, fragment).commit();
        getSupportFragmentManager().executePendingTransactions();

        playerFragment = (AbsPlayerFragment) getSupportFragmentManager().findFragmentById(R.id.player_fragment_container);
        miniPlayerFragment = (MiniPlayerFragment) getSupportFragmentManager().findFragmentById(R.id.mini_player_fragment);

        //noinspection ConstantConditions
        miniPlayerFragment.getView().setOnClickListener(v -> expandPanel());

        slidingUpPanelCallback = new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onSlide(View panel, @FloatRange(from = 0, to = 1) float slideOffset) {
                setMiniPlayerAlphaProgress(slideOffset);
                if (navigationBarColorAnimator != null) navigationBarColorAnimator.cancel();
                setNavigationbarColor((int) argbEvaluator.evaluate(slideOffset, navigationbarColor, playerFragment.getPaletteColor()));
            }

            @Override
            public void onStateChanged(View panel, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        onPanelCollapsed(panel);
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        onPanelExpanded(panel);
                        break;
//            case BottomSheetBehavior.STATE_ANCHORED:
//                collapsePanel(); // this fixes a bug where the panel would get stuck for some reason
//                break;
                }
            }
        };
        slidingUpPanelLayout.addBottomSheetCallback(slidingUpPanelCallback);

        slidingUpPanel.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                slidingUpPanel.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                switch (getPanelState()) {
                    case BottomSheetBehavior.STATE_EXPANDED:
                        slidingUpPanelCallback.onSlide(slidingUpPanel, 1);
                        onPanelExpanded(slidingUpPanel);
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        onPanelCollapsed(slidingUpPanel);
                        break;
                    default:
                        playerFragment.onHide();
                        break;
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Discography.getInstance().addChangedListener(onDiscographyChanged);
    }

    @Override
    protected void onStop() {
        Discography.getInstance().removeChangedListener(onDiscographyChanged);
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentNowPlayingScreen != PreferenceUtil.getInstance().getNowPlayingScreen()) {
            postRecreate();
        }
        reload();
    }

//    public void setAntiDragView(View antiDragView) {
//        slidingUpPanelLayout.setAntiDragView(antiDragView);
//    }

    protected abstract View createContentView();

    protected abstract void reload();

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        if (!MusicPlayerRemote.getPlayingQueue().isEmpty()) {
            slidingUpPanel.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    slidingUpPanel.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    hideBottomBar(false);
                }
            });
        } else {
            hideBottomBar(true);
        }
    }

    @Override
    public void onQueueChanged() {
        super.onQueueChanged();
        hideBottomBar(MusicPlayerRemote.getPlayingQueue().isEmpty());
    }

    public void onPanelCollapsed(View panel) {
        // restore values
        super.setLightStatusbar(lightStatusbar);
        super.setTaskDescriptionColor(taskColor);
        super.setNavigationbarColor(navigationbarColor);

        playerFragment.setMenuVisibility(false);
        playerFragment.setUserVisibleHint(false);
        playerFragment.onHide();
    }

    public void onPanelExpanded(View panel) {
        // setting fragments values
        int playerFragmentColor = playerFragment.getPaletteColor();
        super.setLightStatusbar(false);
        super.setTaskDescriptionColor(playerFragmentColor);
        super.setNavigationbarColor(playerFragmentColor);

        playerFragment.setMenuVisibility(true);
        playerFragment.setUserVisibleHint(true);
        playerFragment.onShow();
    }

    private void setMiniPlayerAlphaProgress(@FloatRange(from = 0, to = 1) float progress) {
        if (miniPlayerFragment.getView() == null) return;
        float alpha = 1 - progress;
        miniPlayerFragment.getView().setAlpha(alpha);
        // necessary to make the views below clickable
        miniPlayerFragment.getView().setVisibility(alpha == 0 ? View.GONE : View.VISIBLE);
    }

    protected int getPanelState() {
        return slidingUpPanelLayout == null ? -1 : slidingUpPanelLayout.getState();
    }

    public void collapsePanel() {
        slidingUpPanelLayout.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    public void expandPanel() {
        slidingUpPanelLayout.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    public void hideBottomBar(final boolean hide) {
        if (hide) {
            slidingUpPanelLayout.setState(BottomSheetBehavior.STATE_HIDDEN);
            collapsePanel();
        } else {
            slidingUpPanelLayout.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }

    @NonNull
    protected SlidingMusicPanelLayoutBinding createSlidingMusicPanel() {
        SlidingMusicPanelLayoutBinding binding = SlidingMusicPanelLayoutBinding.inflate(getLayoutInflater());
        slidingUpPanel = binding.slidingLayout;
        slidingUpPanelLayout = (BottomSheetBehavior) ((CoordinatorLayout.LayoutParams) slidingUpPanel.getLayoutParams()).getBehavior();

        return binding;
    }

    @Override
    public void onBackPressed() {
        if (!handleBackPress())
            super.onBackPressed();
    }

    public boolean handleBackPress() {
        if (slidingUpPanel.getHeight() != 0 && playerFragment.onBackPressed())
            return true;
        if (getPanelState() == BottomSheetBehavior.STATE_EXPANDED) {
            collapsePanel();
            return true;
        }
        return false;
    }

    @Override
    public void onPaletteColorChanged() {
        if (getPanelState() == BottomSheetBehavior.STATE_EXPANDED) {
            int playerFragmentColor = playerFragment.getPaletteColor();
            super.setTaskDescriptionColor(playerFragmentColor);
            animateNavigationBarColor(playerFragmentColor);
        }
    }

    @Override
    public void setLightStatusbar(boolean enabled) {
        lightStatusbar = enabled;
        if (getPanelState() == BottomSheetBehavior.STATE_COLLAPSED) {
            super.setLightStatusbar(enabled);
        }
    }

    @Override
    public void setNavigationbarColor(int color) {
        this.navigationbarColor = color;
        if (getPanelState() == BottomSheetBehavior.STATE_COLLAPSED) {
            if (navigationBarColorAnimator != null) navigationBarColorAnimator.cancel();
            super.setNavigationbarColor(color);
        }
    }

    private void animateNavigationBarColor(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (navigationBarColorAnimator != null) navigationBarColorAnimator.cancel();
            navigationBarColorAnimator = ValueAnimator
                    .ofArgb(getWindow().getNavigationBarColor(), color)
                    .setDuration(ViewUtil.VINYL_MUSIC_PLAYER_ANIM_TIME);
            navigationBarColorAnimator.setInterpolator(new PathInterpolator(0.4f, 0f, 1f, 1f));
            navigationBarColorAnimator.addUpdateListener(animation -> AbsSlidingMusicPanelActivity.super.setNavigationbarColor((Integer) animation.getAnimatedValue()));
            navigationBarColorAnimator.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        slidingUpPanelLayout.removeBottomSheetCallback(slidingUpPanelCallback);
        if (navigationBarColorAnimator != null) navigationBarColorAnimator.cancel(); // just in case
    }

    @Override
    public void setTaskDescriptionColor(@ColorInt int color) {
        this.taskColor = color;
        if (getPanelState() == BottomSheetBehavior.STATE_COLLAPSED) {
            super.setTaskDescriptionColor(color);
        }
    }

}
