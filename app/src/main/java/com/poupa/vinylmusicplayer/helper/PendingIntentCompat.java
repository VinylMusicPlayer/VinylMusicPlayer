package com.poupa.vinylmusicplayer.helper;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Build.VERSION_CODES;

import androidx.annotation.NonNull;

public class PendingIntentCompat {

    public static PendingIntent getActivity(Context context, int requestCode, Intent intent, int flags) {
        if (Build.VERSION.SDK_INT < VERSION_CODES.M) //TODO when targeting API 31+: add "|| (flags & PendingIntent.FLAG_MUTABLE) != 0)" on each if
            return PendingIntent.getActivity(context, requestCode, intent, flags);
        else
            return PendingIntent.getActivity(context, requestCode, intent, flags | PendingIntent.FLAG_IMMUTABLE);
    }

    public static PendingIntent getService(Context context, int requestCode, @NonNull Intent intent, int flags) {
        if (Build.VERSION.SDK_INT < VERSION_CODES.M)
            return PendingIntent.getService(context, requestCode, intent, flags);
        else
            return PendingIntent.getService(context, requestCode, intent, flags | PendingIntent.FLAG_IMMUTABLE);
    }

    public static PendingIntent getBroadcast(Context context, int requestCode, @NonNull Intent intent, int flags) {
        if (Build.VERSION.SDK_INT < VERSION_CODES.M)
            return PendingIntent.getBroadcast(context, requestCode, intent, flags);
        else
            return PendingIntent.getBroadcast(context, requestCode, intent, flags | PendingIntent.FLAG_IMMUTABLE);
    }
}
