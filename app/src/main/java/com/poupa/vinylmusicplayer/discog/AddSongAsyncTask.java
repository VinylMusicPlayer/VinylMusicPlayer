package com.poupa.vinylmusicplayer.discog;

import android.os.AsyncTask;
import android.os.Build;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.google.android.material.snackbar.Snackbar;
import com.poupa.vinylmusicplayer.App;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.model.Song;

/**
 * @author SC (soncaokim)
 */

class AddSongAsyncTask extends AsyncTask<Song, Void, Boolean> {
    private static int pendingCount;
    private static int currentBatchCount;

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
            ++currentBatchCount;
        }

        try {
            if (pendingCount > 0 && currentBatchCount > 0) {
                final CharSequence message = buildMessage(R.string.scanning_x_songs_in_progress, currentBatchCount);

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
            } else {
                // None pending, we are at the end of the batch
                if (progressBar.isShownOrQueued()) {
                    progressBar.dismiss();
                }

                if (currentBatchCount > 0) {
                    final CharSequence message = buildMessage(R.string.scanning_x_songs_finished, currentBatchCount);
                    Snackbar.make(
                            discography.mainActivity.getSnackBarContainer(),
                            message,
                            Snackbar.LENGTH_LONG).show();

                    currentBatchCount = 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NonNull
    private static CharSequence buildMessage(@StringRes int resId, int count) {
        final String message = String.format(
                App.getInstance().getApplicationContext().getString(resId),
                count);

        SpannableStringBuilder messageWithIcon = new SpannableStringBuilder();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            messageWithIcon.append(
                    " ",
                    new ImageSpan(App.getInstance().getApplicationContext(), Discography.ICON),
                    0);
            messageWithIcon.append(" "); // some extra space before the text message
        }
        messageWithIcon.append(message);

        return messageWithIcon;
    }
}
