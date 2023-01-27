package com.poupa.vinylmusicplayer.service.notification;

import android.app.Notification;

public abstract class PlayingNotification extends AbsNotification {
    private static final int NOTIFICATION_ID = 1;
    public static final String NOTIFICATION_CHANNEL_ID = "playing_notification";

    boolean stopped;

    public abstract void update();

    public synchronized void stop() {
        stopped = true;
        notificationManager.cancel(NOTIFICATION_ID);
    }

    void updateImpl(Notification notification) {
        service.startForeground(NOTIFICATION_ID, notification);
    }
}
