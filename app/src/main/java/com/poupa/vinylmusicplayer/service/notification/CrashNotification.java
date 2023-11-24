package com.poupa.vinylmusicplayer.service.notification;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.PendingIntentCompat;

import com.poupa.vinylmusicplayer.App;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.ui.activities.bugreport.BugReportActivity;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;

public class CrashNotification extends AbsNotification {
    private static final int NOTIFICATION_ID = 3;
    public static final String NOTIFICATION_CHANNEL_ID = "crash_notification";

    public void update() {
        final String crashReport = PreferenceUtil.getInstance().popOopsHandlerReport();
        if (crashReport != null) {
            final Context context = App.getStaticContext();
            final Intent reportIntent = new Intent(context, BugReportActivity.class);
            reportIntent.putExtra(Intent.EXTRA_TEXT, crashReport);

            final PendingIntent reportPendingIntent = PendingIntentCompat.getActivity(
                    context,
                    0,
                    reportIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT,
                    false);

            final Notification notification = new NotificationCompat.Builder(service, NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(service.getString(R.string.app_crashed))
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(service.getString(R.string.report_a_crash_invitation) + "\n\n" + crashReport))
                    .setOngoing(false)
                    .setCategory(NotificationCompat.CATEGORY_ERROR)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .addAction(
                            R.drawable.ic_bug_report_white_24dp,
                            service.getString(R.string.report_a_crash),
                            reportPendingIntent)
                    .build();
            notificationManager.notify(NOTIFICATION_ID, notification);
        }
    }

    public void stop() {
        notificationManager.cancel(NOTIFICATION_ID);
    }
}
