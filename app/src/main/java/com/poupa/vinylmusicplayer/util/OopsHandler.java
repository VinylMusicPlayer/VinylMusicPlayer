package com.poupa.vinylmusicplayer.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.ui.activities.bugreport.BugReportActivity;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class OopsHandler implements UncaughtExceptionHandler {
    @NonNull final Context context;
    private static final String NL = "\n";

    public OopsHandler(@NonNull final Context ctx) {
        context = ctx;
    }

    public void uncaughtException(@NonNull final Thread t, @NonNull final Throwable e) {
        try {
            sendBugReport(getStackTraceWithTime(e, null));
        } catch (final Throwable ignore) {}
    }

    private void sendBugReport(final String errorContent) {
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();

                new AlertDialog.Builder(context)
                        .setTitle(R.string.app_crashed)
                        .setMessage(R.string.report_a_crash_invitation)
                        .setCancelable(true)
                        .setPositiveButton(R.string.report_a_crash, (dialog, which) -> {
                            final Intent sendIntent = new Intent(context, BugReportActivity.class);
                            sendIntent.putExtra(Intent.EXTRA_TEXT, errorContent);
                            context.startActivity(sendIntent);
                            System.exit(0);
                        })
                        .setNegativeButton(android.R.string.cancel, ((dialog, which) -> System.exit(0)))
                        .create()
                        .show();

                Looper.loop();
            }
        }.start();
    }

    @NonNull
    private static String getStackTraceWithTime(@NonNull final Throwable exception, @Nullable CharSequence extraInfo) {
        final Writer result = new StringWriter();
        try {
            final String when = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US).format(System.currentTimeMillis());
            result.append("## Time: ").append(NL).append(when).append(NL);
            if (!TextUtils.isEmpty(extraInfo)) {
                result.append("## Extra info: ").append(NL).append(extraInfo).append(NL);
            }
            result.append("## Stack: ").append(NL);
            final PrintWriter printWriter = new PrintWriter(result);
            exception.printStackTrace(printWriter);
            printWriter.close();
            result.append(NL);
        } catch (IOException ignore) {}

        return result.toString();
    }

    public static void collectStackTrace(@NonNull final Throwable exception) {
        PreferenceUtil.getInstance().pushOopsHandlerReport(getStackTraceWithTime(exception, null));
    }

    public static void collectStackTrace(@NonNull final Throwable exception, @NonNull final String extraInfo) {
        PreferenceUtil.getInstance().pushOopsHandlerReport(getStackTraceWithTime(exception, extraInfo));
    }
}