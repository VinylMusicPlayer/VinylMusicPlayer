package com.poupa.vinylmusicplayer.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.provider.StaticPlaylist;
import com.poupa.vinylmusicplayer.util.PlaylistsUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid), Aidan Follestad (afollestad)
 */
public class AddToPlaylistDialog extends DialogFragment {
    private static final String SONGS = "songs";

    @NonNull
    public static AddToPlaylistDialog create(Song song) {
        ArrayList<Song> list = new ArrayList<>();
        list.add(song);
        return create(list);
    }

    @NonNull
    public static AddToPlaylistDialog create(Collection<? extends Song> songs) {
        AddToPlaylistDialog dialog = new AddToPlaylistDialog();
        Bundle args = new Bundle();
        args.putParcelableArrayList(SONGS, new ArrayList<>(songs));
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final List<StaticPlaylist> playlists = StaticPlaylist.getAllPlaylists();
        CharSequence[] playlistNames = new CharSequence[playlists.size() + 1];
        playlistNames[0] = requireActivity().getResources().getString(R.string.action_new_playlist);
        for (int i = 1; i < playlistNames.length; i++) {
            playlistNames[i] = playlists.get(i - 1).asPlaylist().name;
        }
        return new MaterialDialog.Builder(requireActivity())
                .title(R.string.add_playlist_title)
                .items(playlistNames)
                .itemsCallback((materialDialog, view, i, charSequence) -> {
                    final ArrayList<Song> songs = requireArguments().getParcelableArrayList(SONGS);
                    if (songs == null) return;
                    if (i == 0) {
                        materialDialog.dismiss();
                        CreatePlaylistDialog.create(songs).show(requireActivity().getSupportFragmentManager(), "ADD_TO_PLAYLIST");
                    } else {
                        materialDialog.dismiss();
                        Context ctx = requireActivity();
                        final long playlistId = playlists.get(i - 1).asPlaylist().id;

                        if (hasDuplicates(playlistId, songs)) {
                            new MaterialDialog.Builder(ctx)
                                    .title(R.string.confirm_adding_duplicates)
                                    .positiveText(R.string.yes).negativeText(R.string.no)
                                    .onPositive((dialog, which) ->
                                            PlaylistsUtil.addToPlaylist(ctx, songs, playlistId, true)
                                    ).onNegative((dialog, which) -> {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                            songs.removeIf(song -> PlaylistsUtil.doesPlaylistContain(playlistId, song.id));
                                        } else {
                                            for (Song song: new ArrayList<>(songs)) {
                                                if (PlaylistsUtil.doesPlaylistContain(playlistId, song.id)) {
                                                    songs.remove(song);
                                                }
                                            }
                                        }
                                        if (!songs.isEmpty()) {
                                            PlaylistsUtil.addToPlaylist(ctx, songs, playlistId, true);
                                        }
                                    }
                            ).show();
                        } else {
                            PlaylistsUtil.addToPlaylist(ctx, songs, playlistId, true);
                        }
                    }
                })
                .build();
    }

    private static boolean hasDuplicates(long playlistId, ArrayList<Song> songs) {
        for (Song song: songs) {
            if (PlaylistsUtil.doesPlaylistContain(playlistId, song.id)) {
                return true;
            }
        }

        return false;
    }
}
