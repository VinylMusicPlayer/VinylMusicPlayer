package com.poupa.vinylmusicplayer.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.afollestad.materialdialogs.MaterialDialog;
import com.poupa.vinylmusicplayer.App;
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
    private final Context context;
    private static final String NL = "\n";

    public OopsHandler(final Context ctx) {
        context = ctx;
    }

    public void uncaughtException(@NonNull final Thread t, @NonNull final Throwable e) {
        try {
            sendBugReport(getStackTraceWithTime(e));
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
    private static String getStackTraceWithTime(@NonNull final Throwable exception) {
        final Writer result = new StringWriter();
        try {
            final String when = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US).format(System.currentTimeMillis());
            result.append("## Time: ").append(NL).append(when).append(NL);

            result.append("## Stack: ").append(NL);
            final PrintWriter printWriter = new PrintWriter(result);
            exception.printStackTrace(printWriter);
            printWriter.close();
            result.append(NL);
        } catch (IOException ignore) {}

        return result.toString();
    }

    public static void copyStackTraceToClipboard(@NonNull final Throwable exception) {
        if (!PreferenceUtil.getInstance().isOopsHandlerEnabled()) {return;}

        final String stackTrace = getStackTraceWithTime(exception);
        final Context context = App.getStaticContext();

        // Post the clipboard manipulation task to the main thread, since this method may be called from a non-UI thread
        new Handler(context.getMainLooper()).post(() -> {
            final ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            final ClipData clip = ClipData.newPlainText(context.getString(R.string.app_crashed), stackTrace);
            clipboard.setPrimaryClip(clip);
        });
    }
}