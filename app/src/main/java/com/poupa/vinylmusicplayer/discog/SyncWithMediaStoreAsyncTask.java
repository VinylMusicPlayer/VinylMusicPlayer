package com.poupa.vinylmusicplayer.discog;

import android.content.Context;
import android.os.AsyncTask;

import com.poupa.vinylmusicplayer.App;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.provider.BlacklistStore;
import com.poupa.vinylmusicplayer.ui.activities.MainActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * @author SC (soncaokim)
 */

class SyncWithMediaStoreAsyncTask extends AsyncTask<Boolean, Integer, Integer> {
    final Discography discography;
    final SnackbarUtil snackbar;

    SyncWithMediaStoreAsyncTask(MainActivity mainActivity) {
        discography = Discography.getInstance();
        snackbar = new SnackbarUtil(mainActivity.getSnackBarContainer());
    }

    @Override
    protected Integer doInBackground(Boolean... params) {
        boolean resetCache = params[0];
        if (resetCache) {
            discography.clear();
        }

        // Zombies are tracks that are removed but still indexed by MediaStore
        Predicate<Song> isZombie = (s) -> !(new File(s.data)).exists();

        // Blacklist
        final Context context = App.getInstance().getApplicationContext();
        final ArrayList<String> blackListedPaths = BlacklistStore.getInstance(context).getPaths();
        Predicate<Song> isBlackListed = (s) -> {
            for (String path : blackListedPaths) {
                if (s.data.startsWith(path)) return true;
            }
            return false;
        };

        final int initialSongCount = discography.getSongCount();
        ArrayList<Song> alienSongs = MediaStoreBridge.getAllSongs(context);
        final HashSet<Long> importedSongIds = new HashSet<>();
        for (Song song : alienSongs) {
            if (isBlackListed.test(song)) continue;
            if (isZombie.test(song)) continue;

            Song matchedSong = discography.getOrAddSong(song);
            importedSongIds.add(matchedSong.id);

            publishProgress(discography.getSongCount() - initialSongCount);
        }

        // Clean orphan songs (removed from MediaStore)
        synchronized (discography.cache) {
            Set<Long> cacheSongsId = new HashSet<>(discography.cache.songsById.keySet()); // make a copy
            cacheSongsId.removeAll(importedSongIds);
            discography.removeSongById(cacheSongsId.toArray(new Long[0]));
        }

        return discography.getSongCount();
    }

    @Override
    protected void onPreExecute() {
        discography.setStale(true);

        final String message = App.getInstance().getApplicationContext().getString(R.string.scanning_songs_started);
        snackbar.showProgress(message);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        int value = values[values.length - 1];

        if (value != 0)
        {
            final String message = String.format(
                    App.getInstance().getApplicationContext().getString(R.string.scanning_x_songs_in_progress),
                    Math.abs(value));
            snackbar.showProgress(message);
        }
    }

    @Override
    protected void onPostExecute(Integer value) {
        if (value != 0) {
            final String message = String.format(
                    App.getInstance().getApplicationContext().getString(R.string.scanning_x_songs_finished),
                    Math.abs(value));
            snackbar.showResult(message);
        } else {
            snackbar.dismiss();
        }

        discography.setStale(false);
    }
}





