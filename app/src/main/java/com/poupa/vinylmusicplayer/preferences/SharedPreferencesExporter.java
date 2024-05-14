package com.poupa.vinylmusicplayer.preferences;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.google.gson.Gson;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.util.OopsHandler;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;
import com.poupa.vinylmusicplayer.util.SafeToast;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class SharedPreferencesExporter extends AppCompatActivity {
    private Context context;
    private static final String FILENAME = "filename";
    private SharedPreferences sharedPreferences;

    public static void start(@NonNull final Context context, @NonNull final String filename) {
        final Intent intent = new Intent(context, SharedPreferencesExporter.class);
        intent.putExtra(FILENAME, filename);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        final Bundle bundle = getIntent().getExtras();
        // Unless the selection has been cancelled, create the export file
        // Finishes the last activity to return to the settings activity.
        final ActivityResultLauncher<String> exportFilePicker = registerForActivityResult(new ActivityResultContracts.CreateDocument("text/plain"), result -> {
            // Unless the selection has been cancelled, create the export file
            if (result != null) {
                try {
                    writeToExportFile(result);
                } catch (PackageManager.NameNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
            // Finishes the last activity to return to the settings activity.
            this.finish();
        });
        exportFilePicker.launch(bundle.getString(FILENAME));
    }

    private void writeToExportFile(final Uri location) throws PackageManager.NameNotFoundException {
        final Gson gson = new Gson();
        final Map<String, ?> allPrefs = sharedPreferences.getAll();
        final Map<String, Object> exportablePrefs = PreferenceUtil.reducePreferencesToDeclared(allPrefs, key -> key.isExportImportable);

        final var packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        exportablePrefs.put(PreferenceUtil.VERSION_NAME, packageInfo.versionName);
        exportablePrefs.put(PreferenceUtil.VERSION_CODE, packageInfo.versionCode);
        exportablePrefs.put(PreferenceUtil.FILE_FORMAT, SharedPreferencesImporter.CURRENT_FILE_FORMAT);

        // Write all lines in the export file
        try {
            // Try to open the file
            final ParcelFileDescriptor file = context.getContentResolver().openFileDescriptor(location, "w");
            if (file == null) return;

            // Write all lines in the file
            final FileWriter writer = new FileWriter(file.getFileDescriptor());
            writer.write(gson.toJson(exportablePrefs));
            writer.close();
            file.close();
        } catch (final IOException exception) {
            // An error happened while writing the line
            SafeToast.show(context, R.string.cannot_export_settings);
            OopsHandler.collectStackTrace(exception);
        }
    }
}