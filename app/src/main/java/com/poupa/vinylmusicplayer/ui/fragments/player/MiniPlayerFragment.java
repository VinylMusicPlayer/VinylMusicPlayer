package com.poupa.vinylmusicplayer.ui.fragments.player;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.util.ColorUtil;
import com.kabouzeid.appthemehelper.util.MaterialValueHelper;
import com.poupa.vinylmusicplayer.databinding.FragmentMiniPlayerBinding;
import com.poupa.vinylmusicplayer.helper.MusicPlayerRemote;
import com.poupa.vinylmusicplayer.helper.MusicProgressViewUpdateHelper;
import com.poupa.vinylmusicplayer.helper.PlayPauseButtonOnClickHandler;
import com.poupa.vinylmusicplayer.ui.fragments.AbsMusicServiceFragment;
import com.poupa.vinylmusicplayer.util.MusicUtil;
import com.poupa.vinylmusicplayer.views.PlayPauseDrawable;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class MiniPlayerFragment extends AbsMusicServiceFragment implements MusicProgressViewUpdateHelper.Callback {
    private FragmentMiniPlayerBinding layoutBinding;
    private PlayPauseDrawable miniPlayerPlayPauseDrawable;
    private MusicProgressViewUpdateHelper progressViewUpdateHelper;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressViewUpdateHelper = new MusicProgressViewUpdateHelper(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layoutBinding = FragmentMiniPlayerBinding.inflate(inflater, container, false);

        return layoutBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.setOnTouchListener(new FlingPlayBackController(getActivity()));
        setUpMiniPlayer();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void setUpMiniPlayer() {
        setUpPlayPauseButton();
    }

    private void setUpPlayPauseButton() {
        // TODO Make this a FAB
        miniPlayerPlayPauseDrawable = new PlayPauseDrawable(requireActivity());
        layoutBinding.miniPlayerPlayPauseButton.setImageDrawable(miniPlayerPlayPauseDrawable);
        layoutBinding.miniPlayerPlayPauseButton.setOnClickListener(new PlayPauseButtonOnClickHandler());
    }

    private void updateSongTitle() {
        layoutBinding.miniPlayerTitle.setText(MusicPlayerRemote.getCurrentSong().title);
        layoutBinding.miniPlayerText.setText(MusicUtil.getSongInfoString(MusicPlayerRemote.getCurrentSong()));
    }

    @Override
    public void onServiceConnected() {
        updateSongTitle();
        updatePlayPauseDrawableState(false);
    }

    @Override
    public void onPlayingMetaChanged() {
        updateSongTitle();
    }

    @Override
    public void onPlayStateChanged() {
        updatePlayPauseDrawableState(true);
    }

    @Override
    public void onUpdateProgressViews(int progress, int total) {
        layoutBinding.progressBar.setMax(total);
        layoutBinding.progressBar.setProgress(progress);
    }

    @Override
    public void onResume() {
        super.onResume();
        progressViewUpdateHelper.start();
        onThemeColorsChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        progressViewUpdateHelper.stop();
    }

    @Override
    public void onThemeColorsChanged() {
        @NonNull final Activity activity = requireActivity();

        @ColorInt final int primaryColor = ThemeStore.primaryColor(activity);
        @ColorInt final int accentColor = ThemeStore.accentColor(activity);
        @ColorInt final int textPrimaryColor = MaterialValueHelper.getPrimaryTextColor(activity, ColorUtil.isColorLight(primaryColor));
        @ColorInt final int textSecondaryColor = MaterialValueHelper.getSecondaryTextColor(activity, ColorUtil.isColorLight(primaryColor));
        @ColorInt final int iconColor = textPrimaryColor;

        layoutBinding.miniPlayerContainer.setBackgroundColor(primaryColor);

        layoutBinding.miniPlayerTitle.setTextColor(textPrimaryColor);
        layoutBinding.miniPlayerText.setTextColor(textSecondaryColor);

        layoutBinding.progressBar.setSupportProgressTintList(ColorStateList.valueOf(accentColor));

        layoutBinding.miniPlayerImage.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
        layoutBinding.miniPlayerPlayPauseButton.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
    }

    private static class FlingPlayBackController implements View.OnTouchListener {

        GestureDetector flingPlayBackController;

        public FlingPlayBackController(Context context) {
            flingPlayBackController = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                    if (Math.abs(velocityX) > Math.abs(velocityY)) {
                        if (velocityX < 0) {
                            MusicPlayerRemote.playNextSong(true);
                            return true;
                        } else if (velocityX > 0) {
                            MusicPlayerRemote.playPreviousSong(true);
                            return true;
                        }
                    }
                    return false;
                }
            });
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return flingPlayBackController.onTouchEvent(event);
        }
    }

    protected void updatePlayPauseDrawableState(boolean animate) {
        if (MusicPlayerRemote.isPlaying()) {
            miniPlayerPlayPauseDrawable.setPause(animate);
        } else {
            miniPlayerPlayPauseDrawable.setPlay(animate);
        }
    }
}
