package com.poupa.vinylmusicplayer.preferences;

import android.app.Dialog;
import android.content.Context;
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

import com.afollestad.materialdialogs.MaterialDialog;
import com.poupa.vinylmusicplayer.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author Andreas Lechner
 */
public class ExportSettingsPreferenceDialog extends DialogFragment {

    @NonNull
    public static ExportSettingsPreferenceDialog newInstance() {
        return new ExportSettingsPreferenceDialog();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final Context context = requireContext();
        final String filename = getExportSettingsFilename();
        Log.i(ExportSettingsPreferenceDialog.class.getName(), filename);
        final LayoutInflater inflater = getLayoutInflater();
        final View view = inflater.inflate(R.layout.preference_dialog_export_settings, null);
        final EditText editText = (EditText)view.findViewById(R.id.ed_export_settings_filename);
        editText.setText(filename, TextView.BufferType.EDITABLE);

        return new MaterialDialog.Builder(context)
                .title(R.string.export_settings)
                .customView(view, false)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .onNegative((dialog, action) -> dismiss())
                .onPositive((dialog, action) -> {
                    SharedPreferencesExporter.start(context, filename);
                    dismiss();
                })
                .build();
    }

    private static String getCurrentFormattedDateTime() {
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmssZ", Locale.getDefault());
        return simpleDateFormat.format(new Date(System.currentTimeMillis()));
    }

    @NonNull
    private static String getExportSettingsFilename() {
        return "vinylmusicplayer-settings_"+ getCurrentFormattedDateTime();
    }
}
