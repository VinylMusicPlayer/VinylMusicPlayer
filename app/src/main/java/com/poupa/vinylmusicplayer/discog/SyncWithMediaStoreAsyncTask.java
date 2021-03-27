package com.poupa.vinylmusicplayer.discog;

import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.poupa.vinylmusicplayer.App;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.ui.activities.MainActivity;

/**
 * @author SC (soncaokim)
 */

class SyncWithMediaStoreAsyncTask extends AsyncTask<Boolean, Integer, Integer> {
    @NonNull
    final Discography discography;

    @Nullable
    SnackbarUtil snackbar = null;

    SyncWithMediaStoreAsyncTask(@Nullable MainActivity mainActivity, @NonNull Discography discog) {
        discography = discog;
        if (mainActivity != null) {
            snackbar = new SnackbarUtil(mainActivity.getSnackBarContainer());
        }
    }

    @Override
    protected Integer doInBackground(Boolean... params) {
        final boolean reset = params[0];
        if (reset) discography.clear();

        return discography.syncWithMediaStore(this::publishProgress);
    }

    @Override
    protected void onPreExecute() {
        discography.setStale(true);

        if (snackbar != null) {
            final String message = App.getInstance().getApplicationContext().getString(R.string.scanning_songs_started);
            snackbar.showProgress(message);
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        if (snackbar == null) return;

        int value = values[values.length - 1];
        if (value == 0) return;

        final String message = String.format(
                App.getInstance().getApplicationContext().getString(R.string.scanning_x_songs_in_progress),
                Math.abs(value));
        snackbar.showProgress(message);
    }

    @Override
    protected void onPostExecute(Integer value) {
        onTermination(value);
    }
    @Override
    protected void onCancelled(Integer value) {
        onTermination(value);
    }

    private void onTermination(Integer value) {
        discography.setStale(false);
        if (snackbar == null) return;

        if (value != 0) {
            final String message = String.format(
                    App.getInstance().getApplicationContext().getString(R.string.scanning_x_songs_finished),
                    Math.abs(value));
            snackbar.showResult(message);
        } else {
            snackbar.dismiss();
        }
    }
}
