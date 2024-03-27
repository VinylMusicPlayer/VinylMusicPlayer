package com.poupa.vinylmusicplayer.util;

import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.util.Log;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.MaterialDialog;
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
            String result = getStackTraceWithTime(e, null);
            Log.e(OopsHandler.class.getName(), "Submitting crash report");
            Log.e(OopsHandler.class.getName(), result);
            sendBugReport(result);
        } catch (final Throwable ignore) {}
    }

    private void sendBugReport(final String errorContent) {
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();

                new MaterialDialog.Builder(context)
                        .title(R.string.app_crashed)
                        .content(R.string.report_a_crash_invitation)
                        .autoDismiss(true)
                        .onPositive((dialog, which) -> {
                            final Intent sendIntent = new Intent(context, BugReportActivity.class);
                            sendIntent.putExtra(Intent.EXTRA_TEXT, errorContent);
                            context.startActivity(sendIntent);
                            System.exit(0);
                        })
                        .onNegative(((dialog, which) -> System.exit(0)))
                        .positiveText(R.string.report_a_crash)
                        .negativeText(android.R.string.cancel)
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