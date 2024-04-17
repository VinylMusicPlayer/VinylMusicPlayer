package com.poupa.vinylmusicplayer.service.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;

import com.poupa.vinylmusicplayer.service.MusicService;

public abstract class AbsNotification {
    NotificationManager notificationManager;
    protected MusicService service;

    public void init(final MusicService musicService, @NonNull final String channelId,
                     @StringRes final int channelName,
                     @StringRes final int channelDescription) {
        service = musicService;
        notificationManager = (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(channelId, channelName, channelDescription);
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void createNotificationChannel(@NonNull final String channelId, @StringRes final int channelName,
                                           @StringRes final int channelDescription) {
        NotificationChannel notificationChannel = notificationManager.getNotificationChannel(channelId);
        if (notificationChannel == null) {
            notificationChannel = new NotificationChannel(
                    channelId,
                    service.getString(channelName),
                    NotificationManager.IMPORTANCE_LOW);
            notificationChannel.setDescription(service.getString(channelDescription));
            notificationChannel.enableLights(false);
            notificationChannel.enableVibration(false);

            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    public abstract void update();

    public abstract void stop();

}
