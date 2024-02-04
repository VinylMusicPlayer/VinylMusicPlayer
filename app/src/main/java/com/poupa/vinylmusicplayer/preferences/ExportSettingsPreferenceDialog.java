package com.poupa.vinylmusicplayer.preferences;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.util.SharedPreferencesExporter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author Andreas Lechner ()
 */
public class ExportSettingsPreferenceDialog extends DialogFragment {

    @NonNull
    public static ExportSettingsPreferenceDialog newInstance(@NonNull String preference) {
        return new ExportSettingsPreferenceDialog(preference);
    }

    @NonNull private final String preferenceKey;
    private Context context;

    public ExportSettingsPreferenceDialog(@NonNull String preference) {
        preferenceKey = preference;
    }

    private SharedPreferences sharedPreferences;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        this.context = this.getContext();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.context);
        final Activity activity = requireActivity();
        String filename = this.getExportSettingsFilename();
        Log.i(ExportSettingsPreferenceDialog.class.getName(), filename);
        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle(R.string.export_settings);
        LayoutInflater inflater = this.getLayoutInflater();
        View parent = inflater.inflate(R.layout.preference_dialog_export_settings, null);
        alert.setView(parent);
        EditText editText = (EditText)parent.findViewById(R.id.ed_export_settings_filename);
        editText.setText(filename, TextView.BufferType.EDITABLE);
        alert.setPositiveButton(android.R.string.ok, (dialog, id) -> {
            // User taps OK button.
            SharedPreferencesExporter.start(this.context, editText.getText().toString());
        });
        alert.setNegativeButton(android.R.string.cancel, (dialog, id) -> {
            // User cancels the dialog.
        });

        return alert.create();
    }

    private static String getCurrentFormattedDateTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault());
        return simpleDateFormat.format(new Date(System.currentTimeMillis()));
    }

    private static String getExportSettingsFilename() {
        return "vinylmusicplayer-settings_"+ getCurrentFormattedDateTime();
    }

    private void createFile(String filename) {
        final Intent createDocument = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        createDocument.setType("text/plain");
        createDocument.putExtra(Intent.EXTRA_TITLE, filename);
        createDocument.addCategory(Intent.CATEGORY_OPENABLE);
        //Log.i("LogFileinfo: ", createDocument.)
    }
}
