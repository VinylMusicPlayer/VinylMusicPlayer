package com.poupa.vinylmusicplayer.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.afollestad.materialdialogs.MaterialDialog;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.ui.activities.bugreport.BugReportActivity;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Date;

public class OopsHandler implements UncaughtExceptionHandler {
    private final Context context;

    public OopsHandler(final Context ctx) {
        context = ctx;
    }

    public void uncaughtException(@NonNull final Thread t, @NonNull final Throwable e) {
        try {
            final StringBuilder report = new StringBuilder();
            report.append("Time: ").append(new Date()).append("\n\n");
            report.append("Stack:\n");
            report.append(getStackTrace(e));
            report.append('\n');
            Log.e(OopsHandler.class.getName(), "Submitting crash report");

            sendBugReport(report);
        } catch (final Throwable sendError) {
            Log.e(OopsHandler.class.getName(), "Error while submitting", sendError);
        }
    }

    private void sendBugReport(final CharSequence errorContent) {
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
                            sendIntent.putExtra(Intent.EXTRA_TEXT, errorContent.toString());
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

    private static String getStackTrace(@NonNull final Throwable exception) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        exception.printStackTrace(printWriter);
        printWriter.close();

        return result.toString();
    }

    public static void copyStackTraceToClipboard(@NonNull final Context context, @NonNull final Throwable exception) {
        final ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        final ClipData clip = ClipData.newPlainText(context.getString(R.string.failed_to_save_playlist), OopsHandler.getStackTrace(exception));
        clipboard.setPrimaryClip(clip);
    }}