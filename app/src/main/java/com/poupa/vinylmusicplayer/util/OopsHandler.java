package com.poupa.vinylmusicplayer.util;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Date;
import java.util.Locale;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Looper;
import android.os.StatFs;
import android.util.Log;

import androidx.annotation.NonNull;

/**
 * Capture uncaught exceptions
 *
 * Heavily based on https://www.coderzheaven.com/2013/03/13/customize-force-close-dialog-android/
 */
public class OopsHandler implements UncaughtExceptionHandler {
    public final static String EMAIL = "D-1qzcxj36d5n2jhccd@maildrop.cc";
    public final static String APP_NAME = "Vinyl";

    private final Context context;

    public OopsHandler(Context ctx) {
        context = ctx;
    }

    @NonNull
    private StatFs getStatFs() {
        File path = Environment.getDataDirectory();
        return new StatFs(path.getPath());
    }

    private long getAvailableInternalMemorySize(@NonNull StatFs stat) {
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }

    private long getTotalInternalMemorySize(@NonNull StatFs stat) {
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return totalBlocks * blockSize;
    }

    private void addInformation(@NonNull StringBuilder message) {
        message.append("Locale: ").append(Locale.getDefault()).append('\n');
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi;
            pi = pm.getPackageInfo(context.getPackageName(), 0);
            message.append("Version: ").append(pi.versionName).append('\n');
            message.append("Package: ").append(pi.packageName).append('\n');
        } catch (Exception e) {
            Log.e("OopsHandler", "Error", e);
            message.append("Could not get Version information for ").append(context.getPackageName());
        }
        message.append("Phone Model: ").append(android.os.Build.MODEL).append('\n');
        message.append("Android Version: ")
                .append(android.os.Build.VERSION.RELEASE).append('\n');
        message.append("Board: ").append(android.os.Build.BOARD).append('\n');
        message.append("Brand: ").append(android.os.Build.BRAND).append('\n');
        message.append("Device: ").append(android.os.Build.DEVICE).append('\n');
        message.append("Host: ").append(android.os.Build.HOST).append('\n');
        message.append("ID: ").append(android.os.Build.ID).append('\n');
        message.append("Model: ").append(android.os.Build.MODEL).append('\n');
        message.append("Product: ").append(android.os.Build.PRODUCT).append('\n');
        message.append("Type: ").append(android.os.Build.TYPE).append('\n');
        StatFs stat = getStatFs();
        message.append("Total Internal memory: ")
                .append(getTotalInternalMemorySize(stat)).append('\n');
        message.append("Available Internal memory: ")
                .append(getAvailableInternalMemorySize(stat)).append('\n');
    }

    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        try {
            StringBuilder report = new StringBuilder();
            Date curDate = new Date();
            report.append("Crash collected on : ")
                    .append(curDate.toString()).append('\n').append('\n');
            report.append("Informations :").append('\n');
            addInformation(report);
            report.append('\n').append('\n');
            report.append("Stack:\n");
            final Writer result = new StringWriter();
            final PrintWriter printWriter = new PrintWriter(result);
            e.printStackTrace(printWriter);
            report.append(result.toString());
            printWriter.close();
            report.append('\n');
            Log.e(OopsHandler.class.getName(), "Sending crash log to " + EMAIL);
            sendErrorMail(report);
        } catch (Throwable emailError) {
            Log.e(OopsHandler.class.getName(), "Error while sending error e-mail", emailError);
        }
    }

    /**
     * This method for call alert dialog when application crashed!
     */
    public void sendErrorMail(final StringBuilder errorContent) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                // TODO Localized text
                String subject = APP_NAME + " crashed";
                builder.setTitle(subject);
                builder.create();
                builder.setNegativeButton("Cancel",
                        (dialog, which) -> System.exit(0));
                builder.setPositiveButton("Report",
                        (dialog, which) -> {
                            Intent sendIntent = new Intent(Intent.ACTION_SEND);
                            // sendIntent.setType("text/plain");
                            sendIntent.setType("message/rfc822");
                            sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { EMAIL });
                            sendIntent.putExtra(Intent.EXTRA_TEXT, errorContent.toString());
                            sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject);

                            context.startActivity(sendIntent);
                            System.exit(0);
                        });
                builder.setMessage("Crash report information is collected and can be reported to developer (via email).\n\nYou can review the crash report before sending.");
                builder.show();
                Looper.loop();
            }
        }.start();
    }
}