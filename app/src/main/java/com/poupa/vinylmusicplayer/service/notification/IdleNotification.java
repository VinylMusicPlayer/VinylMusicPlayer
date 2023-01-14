package com.poupa.vinylmusicplayer.service.notification;

import android.app.Notification;

import androidx.core.app.NotificationCompat;

import com.poupa.vinylmusicplayer.R;

public class IdleNotification extends AbsNotification {
    private static final int NOTIFICATION_ID = 2;
    public static final String NOTIFICATION_CHANNEL_ID = "idle_notification";

    public void update() {
        final Notification notification = new NotificationCompat.Builder(service, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(service.getString(R.string.idle_notification_title))
                .setContentText(service.getString(R.string.idle_notification_description))
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build();
        service.startForeground(NOTIFICATION_ID, notification);
    }

    public void stop() {
        notificationManager.cancel(NOTIFICATION_ID);
    }
}
