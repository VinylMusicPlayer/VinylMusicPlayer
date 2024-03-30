package com.poupa.vinylmusicplayer.util;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

/**
 * Toast wrapper that can be used from non-UI thread
 */
public class SafeToast {
    private static final int DURATION = Toast.LENGTH_SHORT;

    public static void show(@NonNull final Context context, @StringRes final int resId)
            throws Resources.NotFoundException {
        show(context, context.getResources().getText(resId));
    }

    public static void show(@NonNull final Context context, @NonNull final CharSequence text) {
        final Looper mainLooper = context.getMainLooper();
        if (mainLooper.getThread() == Thread.currentThread()) {
            Toast.makeText(context, text, DURATION).show();
        } else {
            new Handler(mainLooper).post(() -> {
                Toast.makeText(context, text, DURATION).show();
            });
        }
    }
}
