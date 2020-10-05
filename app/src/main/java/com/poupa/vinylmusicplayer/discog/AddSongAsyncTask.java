package com.poupa.vinylmusicplayer.discog;

import android.os.AsyncTask;
import android.os.Build;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;

import androidx.annotation.NonNull;

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
                    updateProgressBar(message, Snackbar.LENGTH_INDEFINITE);
                }
            } else {
                if (progressBar.isShownOrQueued()) {
                    progressBar.dismiss();
                }

                if (burstSongCount > 0) {
                    final String message = String.format(
                            App.getInstance().getApplicationContext().getString(R.string.scanning_x_songs_finished),
                            burstSongCount);
                    updateProgressBar(message, Snackbar.LENGTH_LONG);

                    burstSongCount = 0;

                    // Notify the main activity to reload the tabs content
                    discography.mainActivity.onMediaStoreChanged();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NonNull
    private static void updateProgressBar(@NonNull final CharSequence message, int length) {
        SpannableStringBuilder messageWithIcon = new SpannableStringBuilder();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            messageWithIcon.append(
                    " ",
                    new ImageSpan(App.getInstance().getApplicationContext(), Discography.ICON),
                    0);
            messageWithIcon.append(" "); // some extra space before the text message
        }
        messageWithIcon.append(message);

        if (progressBar == null) {
            progressBar = Snackbar.make(
                    discography.mainActivity.getSnackBarContainer(),
                    messageWithIcon,
                    length);
            progressBar.show();
        } else {
            progressBar.setText(messageWithIcon);
            if (!progressBar.isShownOrQueued()) {
                progressBar.show();
            }
        }
    }
}
