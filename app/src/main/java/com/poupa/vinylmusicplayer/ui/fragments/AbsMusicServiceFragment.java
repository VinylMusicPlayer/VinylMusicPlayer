package com.poupa.vinylmusicplayer.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.poupa.vinylmusicplayer.interfaces.MusicServiceEventListener;
import com.poupa.vinylmusicplayer.interfaces.ThemeEventListener;
import com.poupa.vinylmusicplayer.ui.activities.base.AbsMusicServiceActivity;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class AbsMusicServiceFragment extends Fragment
        implements MusicServiceEventListener, ThemeEventListener
{
    private AbsMusicServiceActivity activity;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            activity = (AbsMusicServiceActivity) context;
        } catch (ClassCastException e) {
            throw new RuntimeException(context.getClass().getSimpleName() + " must be an instance of " + AbsMusicServiceActivity.class.getSimpleName());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity.addMusicServiceEventListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        activity.removeMusicServiceEventListener(this);
    }

    @Override
    public void onPlayingMetaChanged() {

    }

    @Override
    public void onServiceConnected() {

    }

    @Override
    public void onServiceDisconnected() {

    }

    @Override
    public void onQueueChanged() {

    }

    @Override
    public void onPlayStateChanged() {

    }

    @Override
    public void onRepeatModeChanged() {

    }

    @Override
    public void onShuffleModeChanged() {

    }

    @Override
    public void onMediaStoreChanged() {

    }

    @Override
    public void onThemeColorsChanged() {}

    @Override
    public boolean overrideThemeColorsForStatusBar() {
        // By default, the status bar should follow the theme
        return false;
    }
}
