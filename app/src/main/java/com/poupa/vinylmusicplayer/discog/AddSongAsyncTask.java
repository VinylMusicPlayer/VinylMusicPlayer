package com.poupa.vinylmusicplayer.discog;

import android.os.AsyncTask;

import com.google.android.material.snackbar.Snackbar;
import com.poupa.vinylmusicplayer.App;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.model.Song;

/**
 * @author SC (soncaokim)
 */

class AddSongAsyncTask extends AsyncTask<Song, Void, Boolean> {
    private static int pendingCount;
    private static int burstSongCount;

    private static Snackbar progressBar;
    private final static Discography discography = Discography.getInstance();

    @Override
    protected void onPreExecute() {
        ++pendingCount;
    }

    @Override
    protected Boolean doInBackground(Song... songs) {
        boolean effectiveAdd = false;
        for (Song song : songs) {
            effectiveAdd = discography.addSongImpl(song, false);
        }
        return effectiveAdd;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        --pendingCount;
        if (result) {
            ++burstSongCount;
        }

        try {
            if (pendingCount > 0) {
                if (burstSongCount > 0) {
                    final String message = String.format(
                            App.getInstance().getApplicationContext().getString(R.string.scanning_x_songs_in_progress),
                            burstSongCount);
                    if (progressBar == null) {
                        progressBar = Snackbar.make(
                                discography.mainActivity.getSnackBarContainer(),
                                message,
                                Snackbar.LENGTH_INDEFINITE);
                        progressBar.show();
                    } else {
                        progressBar.setText(message);
                        if (!progressBar.isShownOrQueued()) {
                            progressBar.show();
                        }
                    }
                }
            } else {
                if (progressBar.isShownOrQueued()) {
                    progressBar.dismiss();
                }

                if (burstSongCount > 0) {
                    final String message = String.format(
                            App.getInstance().getApplicationContext().getString(R.string.scanning_x_songs_finished),
                            burstSongCount);
                    Snackbar.make(
                            Discography.getInstance().mainActivity.getSnackBarContainer(),
                            message,
                            Snackbar.LENGTH_LONG).show();

                    burstSongCount = 0;

                    // Notify the main activity to reload the tabs content
                    discography.mainActivity.onMediaStoreChanged();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
