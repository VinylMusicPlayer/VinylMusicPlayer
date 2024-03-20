package com.poupa.vinylmusicplayer.discog;

import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.poupa.vinylmusicplayer.App;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.ui.activities.base.AbsMusicServiceActivity;

/**
 * @author SC (soncaokim)
 */

class SyncWithMediaStoreAsyncTask extends AsyncTask<Void, Integer, Integer> {
    @NonNull
    private final Discography discography;

    @NonNull
    private final SnackbarUtil snackbar;

    private final boolean resetRequested;

    SyncWithMediaStoreAsyncTask(@NonNull final AbsMusicServiceActivity containerActivity, @NonNull final Discography discog, final boolean reset) {
        discography = discog;
        snackbar = new SnackbarUtil(containerActivity.getSnackBarContainer());
        resetRequested = reset;
    }

    @Override
    protected Integer doInBackground(Void...params) {
        if (resetRequested) discography.clear();
        return discography.syncWithMediaStore(this::publishProgress);
    }

    @Override
    protected void onPreExecute() {
        discography.setCacheState(resetRequested ? MemCache.ConsistencyState.RESETTING : MemCache.ConsistencyState.REFRESHING);
        startTimeMs = System.currentTimeMillis();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        if (isUIFeedbackNeeded()) {
            int value = values[values.length - 1];
            if (value == 0) return;

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
        discography.setCacheState(MemCache.ConsistencyState.OK);
        if (isUIFeedbackNeeded()) {
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

    private long startTimeMs = 0L;
    private boolean isUIFeedbackNeeded() {
        final long UI_VISIBLE_THRESHOLD_MS = 500L;
        final long now = System.currentTimeMillis();

        return (now - startTimeMs > UI_VISIBLE_THRESHOLD_MS);
    }
}
