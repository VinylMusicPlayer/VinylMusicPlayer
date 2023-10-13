package com.poupa.vinylmusicplayer.dialogs.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.misc.WeakContextAsyncTask;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.ui.activities.saf.SAFGuideActivity;
import com.poupa.vinylmusicplayer.util.MusicUtil;
import com.poupa.vinylmusicplayer.util.OopsHandler;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;
import com.poupa.vinylmusicplayer.util.SAFUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid), Aidan Follestad (afollestad)
 */
@RequiresApi(api = VERSION_CODES.R)
public class DeleteSongsDialogAndroidR extends Fragment {
    private static final String SONGS = "songs";

    private BaseDeleteSongsAsyncTask deleteSongsTask;
    private ArrayList<Song> songsToRemove;
    private ActivityResultLauncher<Intent> deleteSongs_SAFGuide;
    private ActivityResultLauncher<Uri> deleteSongs_SAFTreePicker;

    private ActivityResultLauncher<IntentSenderRequest> deleteRequestAndroidR;

    @NonNull
    public static DeleteSongsDialogAndroidR create(Song song) {
        ArrayList<Song> list = new ArrayList<>();
        list.add(song);
        return create(list);
    }

    @NonNull
    public static DeleteSongsDialogAndroidR create(ArrayList<Song> songs) {
        DeleteSongsDialogAndroidR dialog = new DeleteSongsDialogAndroidR();
        Bundle args = new Bundle();
        args.putParcelableArrayList(SONGS, songs);
        dialog.setArguments(args);
        return dialog;
    }

    public void show(@NonNull FragmentManager manager, @Nullable String tag) {
        FragmentTransaction ft = manager.beginTransaction();
        ft.add(this, tag);
        ft.commit();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        deleteSongs_SAFGuide = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        try {
                            deleteSongs_SAFTreePicker.launch(Uri.parse(PreferenceUtil.getInstance().getStartDirectory().getAbsolutePath()));
                        } catch (android.content.ActivityNotFoundException noActivity) {
                            Toast.makeText(getActivity(), R.string.android13_no_file_browser_error, Toast.LENGTH_LONG).show();
                        }
                    }
                });

        deleteSongs_SAFTreePicker = registerForActivityResult(
                new ActivityResultContracts.OpenDocumentTree() {
                    @NonNull
                    @Override
                    public Intent createIntent(@NonNull Context context, Uri input) {
                        return super.createIntent(context, input)
                                .addCategory(Intent.CATEGORY_OPENABLE)
                                .putExtra("android.content.extra.SHOW_ADVANCED", true);
                    }
                },
                resultUri -> {
                    if (deleteSongsTask != null) {
                        deleteSongsTask.cancel(true);
                    }
                    deleteSongsTask = new DeleteSongsAndroidRAsyncTask(this);
                    ((DeleteSongsAndroidRAsyncTask)deleteSongsTask).execute(resultUri);
                });

        deleteRequestAndroidR = registerForActivityResult(
                new ActivityResultContracts.StartIntentSenderForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        DeleteSongsHelper.managePlayingSong(songsToRemove);
                        Toast.makeText(getActivity(), getString(R.string.deleted_x_songs, songsToRemove.size()), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), getString(R.string.saf_delete_failed, Integer.toString(songsToRemove.size())), Toast.LENGTH_SHORT).show();
                    }
                });

        songsToRemove = getArguments().getParcelableArrayList(SONGS);

        deleteSongsTask = new DeleteSongsAsyncTask(this);
        ((DeleteSongsAsyncTask)deleteSongsTask).execute(songsToRemove);
    }

    private void deleteSongs(List<Song> songsToRemove) {
        MusicUtil.deleteTracks(DeleteSongsDialogAndroidR.this, this.deleteRequestAndroidR, songsToRemove, null);
    }

    public static abstract class BaseDeleteSongsAsyncTask<Params> extends
            WeakContextAsyncTask<Params, Integer, Void> {
        protected final WeakReference<DeleteSongsDialogAndroidR> fragment;
        protected final WeakReference<FragmentActivity> activity;

        public BaseDeleteSongsAsyncTask(DeleteSongsDialogAndroidR fragment) {
            super(fragment.getActivity());
            this.fragment = new WeakReference<>(fragment);
            this.activity = new WeakReference<>(fragment.getActivity());
        }
    }

    private static class DeleteSongsAsyncTask
            extends BaseDeleteSongsAsyncTask<List<Song>> {
        public DeleteSongsAsyncTask(DeleteSongsDialogAndroidR fragment) {
            super(fragment);
        }

        @SafeVarargs
        @Override
        protected final Void doInBackground(List<Song>... lists) {
            DeleteSongsDialogAndroidR fragment = this.fragment.get();
            FragmentActivity activity = this.activity.get();

            try {
                if (fragment == null || activity == null) {return null;}

                final List<Song> songs = lists[0];
                if (!SAFUtil.isSAFRequired(songs)) {
                    fragment.deleteSongs(songs);
                } else {
                    if (SAFUtil.isSDCardAccessGranted(activity)) {
                        fragment.deleteSongs(songs);
                    } else {
                        fragment.deleteSongs_SAFGuide.launch(new Intent(activity, SAFGuideActivity.class));
                    }
                }
            } catch (Exception e) {
                OopsHandler.copyStackTraceToClipboard(activity, e);
            }

            return null;
        }
    }

    private static class DeleteSongsAndroidRAsyncTask
            extends BaseDeleteSongsAsyncTask<Uri> {

        public DeleteSongsAndroidRAsyncTask(DeleteSongsDialogAndroidR dialog) {
            super(dialog);
        }

        @Override
        protected Void doInBackground(Uri... uris) {
            try {
                DeleteSongsDialogAndroidR fragment = this.fragment.get();
                FragmentActivity activity = this.activity.get();

                if (fragment == null || activity == null) {return null;}

                SAFUtil.saveTreeUri(activity, uris[0]);
                fragment.deleteSongs(fragment.songsToRemove);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}