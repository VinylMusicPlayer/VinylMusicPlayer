package com.poupa.vinylmusicplayer.discog;

import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.poupa.vinylmusicplayer.ui.activities.base.AbsMusicServiceActivity;

/**
 * @author SC (soncaokim)
 */

class SyncWithMediaStoreAsyncTask extends AsyncTask<Void, SyncWithMediaStoreAsyncTask.Progress, SyncWithMediaStoreAsyncTask.Progress> {
    @NonNull
    private final Discography discography;

    @NonNull
    private final SnackbarUtil snackbar;

    private final boolean resetRequested;

    public static class Progress {
        public int added = 0;
        public int removed = 0;
        public int updated = 0;

        boolean isEmpty() {
            return added == 0 && removed == 0 && updated == 0;
        }

        @NonNull
        String buildInfoString() {
            if (isEmpty()) return "";

            final StringBuilder builder = new StringBuilder();
            builder.append("Track update:");
            if (added > 0) {
                builder.append(String.format(" %1$d added", added));
            }
            if (updated > 0) {
                builder.append(String.format(" %1$d updated", updated));
            }
            if (removed > 0) {
                builder.append(String.format(" %1$d removed", removed));
            }
            return builder.toString();
        }
    }

    SyncWithMediaStoreAsyncTask(@NonNull final AbsMusicServiceActivity containerActivity, @NonNull final Discography discog, final boolean reset) {
        discography = discog;
        snackbar = new SnackbarUtil(containerActivity.getSnackBarContainer());
        resetRequested = reset;
    }

    @Override
    protected Progress doInBackground(final Void...params) {
        if (resetRequested) discography.clear();
        return discography.syncWithMediaStore(this::publishProgress);
    }

    @Override
    protected void onPreExecute() {
        discography.setCacheState(resetRequested ? MemCache.ConsistencyState.RESETTING : MemCache.ConsistencyState.REFRESHING);
        startTimeMs = System.currentTimeMillis();
    }

    @Override
    protected void onProgressUpdate(final Progress... values) {
        final Progress last = values[values.length - 1];
        if (!last.isEmpty() && isUIFeedbackNeeded()) {
            snackbar.showProgress(last.buildInfoString());
        }
    }

    @Override
    protected void onPostExecute(@NonNull final Progress value) {
        onTermination(value);
    }
    @Override
    protected void onCancelled(@NonNull final Progress value) {
        onTermination(value);
    }

    private void onTermination(@NonNull final Progress value) {
        discography.setCacheState(MemCache.ConsistencyState.OK);
        if (isUIFeedbackNeeded()) {
            if (value.isEmpty()) {
                snackbar.dismiss();
            } else {
                snackbar.showResult(value.buildInfoString());
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
