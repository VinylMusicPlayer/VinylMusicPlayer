package com.poupa.vinylmusicplayer.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.model.Playlist;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.model.smartplaylist.AbsSmartPlaylist;
import com.poupa.vinylmusicplayer.provider.StaticPlaylist;
import com.poupa.vinylmusicplayer.util.SafeToast;

import java.util.List;

/**
 * @author SC (soncaokim)
 */
public class ImportFromPlaylistDialog extends DialogFragment {
    private static final String PLAYLIST = "playlist";

    @NonNull
    public static ImportFromPlaylistDialog create(AbsSmartPlaylist smartPlaylist) {
        ImportFromPlaylistDialog dialog = new ImportFromPlaylistDialog();

        Bundle args = new Bundle();
        args.putParcelable(PLAYLIST, smartPlaylist);
        dialog.setArguments(args);

        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final @NonNull Context context = requireContext();

        final List<StaticPlaylist> playlists = StaticPlaylist.getAllPlaylists();

        if (playlists.isEmpty()) {
            return new MaterialDialog.Builder(context)
                    .title(R.string.import_playlist_title)
                    .content(R.string.no_playlists)
                    .build();
        } else {
            CharSequence[] playlistNames = new CharSequence[playlists.size()];
            for (int i = 0; i < playlistNames.length; i++) {
                playlistNames[i] = playlists.get(i).asPlaylist().name;
            }
            return new MaterialDialog.Builder(context)
                    .title(R.string.import_playlist_title)
                    .items(playlistNames)
                    .itemsCallback((materialDialog, view, i, charSequence) -> {
                        materialDialog.dismiss();
                        final Playlist sourcePlaylist = playlists.get(i).asPlaylist();
                        final List<? extends Song> songs = sourcePlaylist.getSongs(context);
                        if (songs.isEmpty()) {
                            SafeToast.show(context, R.string.playlist_is_empty);
                        } else {
                            AbsSmartPlaylist destinationPlaylist = requireArguments().getParcelable(PLAYLIST);
                            if (destinationPlaylist != null) {
                                destinationPlaylist.importPlaylist(context, sourcePlaylist);
                                SafeToast.show(context, context.getResources().getString(R.string.added_x_titles_to_playlist, songs.size()));
                            }
                        }
                    })
                    .build();
        }
    }
}
