package com.poupa.vinylmusicplayer.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.loader.PlaylistLoader;
import com.poupa.vinylmusicplayer.model.Playlist;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.util.PlaylistsUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid), Aidan Follestad (afollestad)
 */
public class AddToPlaylistDialog extends DialogFragment {

    @NonNull
    public static AddToPlaylistDialog create(Song song) {
        ArrayList<Song> list = new ArrayList<>();
        list.add(song);
        return create(list);
    }

    @NonNull
    public static AddToPlaylistDialog create(ArrayList<Song> songs) {
        AddToPlaylistDialog dialog = new AddToPlaylistDialog();
        Bundle args = new Bundle();
        args.putParcelableArrayList("songs", songs);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final List<Playlist> playlists = PlaylistLoader.getAllPlaylists(getActivity());
        CharSequence[] playlistNames = new CharSequence[playlists.size() + 1];
        playlistNames[0] = getActivity().getResources().getString(R.string.action_new_playlist);
        for (int i = 1; i < playlistNames.length; i++) {
            playlistNames[i] = playlists.get(i - 1).name;
        }
        return new MaterialDialog.Builder(getActivity())
                .title(R.string.add_playlist_title)
                .items(playlistNames)
                .itemsCallback((materialDialog, view, i, charSequence) -> {
                    //noinspection unchecked
                    final ArrayList<Song> songs = getArguments().getParcelableArrayList("songs");
                    if (songs == null) return;
                    if (i == 0) {
                        materialDialog.dismiss();
                        CreatePlaylistDialog.create(songs).show(getActivity().getSupportFragmentManager(), "ADD_TO_PLAYLIST");
                    } else {
                        materialDialog.dismiss();
                        Context ctx = getActivity();
                        if (hasDuplicates(playlists.get(i - 1).id, songs, ctx)) {
                            new MaterialDialog.Builder(ctx)
                                    .title(R.string.confirm_adding_duplicates)
                                    .positiveText(R.string.yes).negativeText(R.string.no)
                                    .onPositive((dialog, which) ->
                                            PlaylistsUtil.addToPlaylist(ctx, songs, playlists.get(i - 1).id, true)
                                    ).onNegative((dialog, which) -> {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                            songs.removeIf(song -> PlaylistsUtil.doesPlaylistContain(ctx, playlists.get(i - 1).id, song.id));
                                        } else {
                                            for (Song song: new ArrayList<>(songs)) {
                                                if (PlaylistsUtil.doesPlaylistContain(ctx, playlists.get(i - 1).id, song.id)) {
                                                    songs.remove(song);
                                                }
                                            }
                                        }
                                        if (!songs.isEmpty()) {
                                            PlaylistsUtil.addToPlaylist(ctx, songs, playlists.get(i - 1).id, true);
                                        }
                                    }
                            ).show();
                        } else {
                            PlaylistsUtil.addToPlaylist(ctx, songs, playlists.get(i - 1).id, true);
                        }
                    }
                })
                .build();
    }

    private boolean hasDuplicates(long playlistId, ArrayList<Song> songs, Context ctx) {
        for (Song song: songs) {
            if (PlaylistsUtil.doesPlaylistContain(ctx, playlistId, song.id)) {
                return true;
            }
        }

        return false;
    }
}
