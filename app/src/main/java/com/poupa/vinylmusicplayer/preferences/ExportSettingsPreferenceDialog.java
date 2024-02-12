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

import com.afollestad.materialdialogs.MaterialDialog;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.preferences.SharedPreferencesExporter;
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
        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.preference_dialog_export_settings, null);
        EditText editText = (EditText)view.findViewById(R.id.ed_export_settings_filename);
        editText.setText(filename, TextView.BufferType.EDITABLE);

        return new MaterialDialog.Builder(getContext())
                .title(R.string.export_settings)
                .customView(view, false)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                //.autoDismiss(false)
                .onNegative((dialog, action) -> dismiss())
                .onPositive((dialog, action) -> {
                    SharedPreferencesExporter.start(this.context, editText.getText().toString());
                    dismiss();
                })
                .build();
    }

    private static String getCurrentFormattedDateTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault());
        return simpleDateFormat.format(new Date(System.currentTimeMillis()));
    }

    private static String getExportSettingsFilename() {
        return "vinylmusicplayer-settings_"+ getCurrentFormattedDateTime();
    }
}
