package com.poupa.vinylmusicplayer.misc;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.media.MediaScannerConnection;
import android.net.Uri;

import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.util.SafeToast;

import java.lang.ref.WeakReference;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class UpdateToastMediaScannerCompletionListener implements MediaScannerConnection.OnScanCompletedListener {
    private int scanned = 0;
    private int failed = 0;

    private final String[] toBeScanned;

    private final String scannedFiles;
    private final String couldNotScanFiles;

    private final WeakReference<Activity> activityWeakReference;

    @SuppressLint("ShowToast")
    public UpdateToastMediaScannerCompletionListener(Activity activity, String[] toBeScanned) {
        this.toBeScanned = toBeScanned;
        scannedFiles = activity.getString(R.string.scanned_files);
        couldNotScanFiles = activity.getString(R.string.could_not_scan_files);
        activityWeakReference = new WeakReference<>(activity);
    }

    @Override
    public void onScanCompleted(final String path, final Uri uri) {
        if (uri == null) {
            failed++;
        } else {
            scanned++;
        }

        Activity activity = activityWeakReference.get();
        if ((activity != null) && (failed + scanned == toBeScanned.length)){
            activity.runOnUiThread(() -> {
                String text = " " + String.format(scannedFiles, scanned, toBeScanned.length) + (failed > 0 ? " " + String.format(couldNotScanFiles, failed) : "");
                SafeToast.show(activity, text);
            });
        }
    }
}
