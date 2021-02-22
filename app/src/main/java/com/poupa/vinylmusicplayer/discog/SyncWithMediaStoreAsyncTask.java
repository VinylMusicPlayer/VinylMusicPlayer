package com.poupa.vinylmusicplayer.discog;

import android.os.AsyncTask;

import com.poupa.vinylmusicplayer.App;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.ui.activities.MainActivity;

/**
 * @author SC (soncaokim)
 */

class SyncWithMediaStoreAsyncTask extends AsyncTask<Boolean, Integer, Integer> {
    final Discography discography;
    final SnackbarUtil snackbar;

    SyncWithMediaStoreAsyncTask(MainActivity mainActivity, Discography discog) {
        discography = discog;
        snackbar = new SnackbarUtil(mainActivity.getSnackBarContainer());
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
        onTermination(value);
    }
    @Override
    protected void onCancelled(Integer value) {
        onTermination(value);
    }

    private void onTermination(Integer value) {
        discography.setStale(false);

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
