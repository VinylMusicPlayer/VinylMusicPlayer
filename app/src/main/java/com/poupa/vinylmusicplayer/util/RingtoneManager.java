package com.poupa.vinylmusicplayer.util;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.Settings;

import androidx.annotation.NonNull;

import com.poupa.vinylmusicplayer.R;

public class RingtoneManager {


    public static boolean requiresDialog(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return !Settings.System.canWrite(context);
        }
        return false;
    }

    public static void showDialog(Context context) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.dialog_ringtone_title)
                .setMessage(R.string.dialog_ringtone_message)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    dialog.dismiss();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                        intent.setData(Uri.parse("package:" + context.getPackageName()));
                        context.startActivity(intent);
                    }
                })
                .setNegativeButton(android.R.string.cancel, ((dialog, which) -> dialog.dismiss()))
                .show();
    }

    public   void setRingtone(@NonNull final Context context, final long id) {
        final ContentResolver resolver = context.getContentResolver();
        final Uri uri = MusicUtil.getSongFileUri(id);
        try {
            final ContentValues values = new ContentValues(2);
            values.put(MediaStore.Audio.AudioColumns.IS_RINGTONE, "1");
            values.put(MediaStore.Audio.AudioColumns.IS_ALARM, "1");
            resolver.update(uri, values, null, null);
        } catch (@NonNull final UnsupportedOperationException ignored) {
            return;
        }

        try {
            try (Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.MediaColumns.TITLE},
                    BaseColumns._ID + "=?",
                    new String[]{String.valueOf(id)},
                    null)) {
                if (cursor != null && cursor.getCount() == 1) {
                    cursor.moveToFirst();
                    Settings.System.putString(resolver, Settings.System.RINGTONE, uri.toString());
                    final String message = context.getString(R.string.x_has_been_set_as_ringtone, cursor.getString(0));
                    SafeToast.show(context, message);
                }
            }
        } catch (SecurityException ignored) {
        }
    }
}
