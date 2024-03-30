package com.poupa.vinylmusicplayer.dialogs.helper;

import android.os.Build;
import android.os.Build.VERSION_CODES;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.poupa.vinylmusicplayer.helper.MusicPlayerRemote;
import com.poupa.vinylmusicplayer.model.Song;

import java.util.ArrayList;
import java.util.Collection;

public class DeleteSongsHelper {

    public static void delete(Song song, @NonNull FragmentManager manager, @Nullable String tag) {
        if (Build.VERSION.SDK_INT < VERSION_CODES.R) {
            DeleteSongsDialogApi19.create(song).show(manager, tag);
        } else {
            DeleteSongsDialogApi30.create(song).show(manager, tag);
        }
    }

    public static void delete(Collection<? extends Song> songs, @NonNull FragmentManager manager, @Nullable String tag) {
        if (Build.VERSION.SDK_INT < VERSION_CODES.R) {
            DeleteSongsDialogApi19.create(songs).show(manager, tag);
        } else {
            DeleteSongsDialogApi30.create(songs).show(manager, tag);
        }
    }

    public static void managePlayingSong(ArrayList<Song> songs) {
        // If song removed was the playing song, then play the next song
        if ((songs.size() == 1) && MusicPlayerRemote.isPlaying(songs.get(0))) {
            MusicPlayerRemote.playNextSong(false);
        }
    }
}
