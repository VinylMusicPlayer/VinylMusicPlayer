package com.poupa.vinylmusicplayer.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.poupa.vinylmusicplayer.interfaces.MusicServiceEventListener;
import com.poupa.vinylmusicplayer.ui.activities.base.AbsMusicServiceActivity;
import com.poupa.vinylmusicplayer.util.SAFUtil;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class AbsMusicServiceFragment extends Fragment implements MusicServiceEventListener {
    private AbsMusicServiceActivity activity;

    ActivityResultLauncher<Uri> SAFTreePicker;
    public ActivityResultLauncher<Intent> LollipopSAFGuide;
    public ActivityResultLauncher<String> KitkatSAFFilePicker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO Isolate in a named method
        SAFTreePicker = registerForActivityResult(
                new ActivityResultContracts.OpenDocumentTree() {
                    @NonNull
                    @Override
                    public Intent createIntent(@NonNull Context context, Uri input) {
                        return super.createIntent(context, input)
                                .putExtra("android.content.extra.SHOW_ADVANCED", true);
                    }
                },
                resultUri -> SAFUtil.saveTreeUri(this.requireContext(), resultUri)
        );

        LollipopSAFGuide = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        SAFTreePicker.launch(null);
                    }
                });

        KitkatSAFFilePicker = registerForActivityResult(
                new ActivityResultContracts.CreateDocument("audio/*") { // or 'audio/mpegurl
                    @NonNull
                    @Override
                    public Intent createIntent(@NonNull Context context, @NonNull String input) {
                        return super.createIntent(context, input)
                                .addCategory(Intent.CATEGORY_OPENABLE)
                                .putExtra("android.content.extra.SHOW_ADVANCED", true);
                    }
                },
                resultUri -> {
                    // TODO Nothing?
                });
    }

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
}
