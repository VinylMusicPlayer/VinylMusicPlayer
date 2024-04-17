package com.poupa.vinylmusicplayer.service.notification;

import android.app.Notification;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.poupa.vinylmusicplayer.R;

/**
 * This notification shows up in
 * place of the usual now-playing notification if nothing is playing, to indicate that the app is
 * running in the background.
 * See <a href="https://github.com/VinylMusicPlayer/VinylMusicPlayer/issues/952">#952</a> for
 * further information.
 */
public class IdleNotification extends AbsNotification {
    private static final int NOTIFICATION_ID = 2;
    public static final String NOTIFICATION_CHANNEL_ID = "idle_notification";

    public void update() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final Notification notification = new NotificationCompat.Builder(service, NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(service.getString(R.string.idle_notification_title))
                    .setOngoing(true)
                    .setCategory(NotificationCompat.CATEGORY_SERVICE)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .build();
            service.startForeground(NOTIFICATION_ID, notification);
        }
    }

    public void stop() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.cancel(NOTIFICATION_ID);
        }
    }
}
