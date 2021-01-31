package com.poupa.vinylmusicplayer.discog;

import android.os.AsyncTask;

import com.poupa.vinylmusicplayer.App;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.model.Song;

/**
 * @author SC (soncaokim)
 */

class AddSongAsyncTask extends AsyncTask<Song, Void, Boolean> {
    private static int pendingCount;
    private static int currentBatchCount;

    @Override
    protected void onPreExecute() {
        ++pendingCount;
    }

    @Override
    protected Boolean doInBackground(Song... songs) {
        boolean effectiveAdd = false;
        for (Song song : songs) {
            effectiveAdd = Discography.getInstance().addSongImpl(song, false);
        }
        return effectiveAdd;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        --pendingCount;
        if (result) {
            ++currentBatchCount;
        }

        try {
            SnackbarUtil snackbar = Discography.getInstance().snackbar;
            if (pendingCount > 0 && currentBatchCount > 0) {
                if (snackbar != null) {
                    final String message = String.format(
                            App.getInstance().getApplicationContext().getString(R.string.scanning_x_songs_in_progress),
                            currentBatchCount);
                    snackbar.showProgress(message);
                }
            } else {
                // None pending, we are at the end of the batch
                if (snackbar != null) {
                    final String message = String.format(
                            App.getInstance().getApplicationContext().getString(R.string.scanning_x_songs_finished),
                            currentBatchCount);
                    snackbar.showResult(message);
                }
                currentBatchCount = 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
