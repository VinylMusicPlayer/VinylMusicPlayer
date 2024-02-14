package com.poupa.vinylmusicplayer.preferences;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;

import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.util.OopsHandler;
import com.poupa.vinylmusicplayer.util.SafeToast;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

public class SharedPreferencesExporter extends AppCompatActivity {
    private Context context;
    private static final String FILENAME = "filename";
    private ActivityResultLauncher<String> exportFilePicker;
    private SharedPreferences sharedPreferences;

    public static void start(Context context, String filename) {
        Intent intent = new Intent(context, SharedPreferencesExporter.class);
        intent.putExtra(FILENAME, filename);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this.getApplicationContext();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.context);
        Bundle b = this.getIntent().getExtras();
        exportFilePicker = registerForActivityResult(new ActivityResultContracts.CreateDocument("text/plain"), result -> {
            // Unless the selection has been cancelled, create the export file
            if(result != null) {
                try {
                    writeToExportFile(result);
                } catch (PackageManager.NameNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
            // Finishes the last activity to return to the settings activity.
            this.finish();
        });
        exportFilePicker.launch(b.getString(FILENAME));
    }

    private void writeToExportFile(Uri location) throws PackageManager.NameNotFoundException {
        StringBuilder stringBuilder = new StringBuilder();
        Map<String, ?> prefsMap = sharedPreferences.getAll();
        stringBuilder.append("#" + context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName + "\n");
        for (Map.Entry<String, ?> entry : prefsMap.entrySet()) {
            stringBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
        }

        // Write all lines in the export file
        try {
            // Try to open the file
            ParcelFileDescriptor file = this.context.getContentResolver().openFileDescriptor(location, "w");
            if (file == null) return;

            // Write all lines in the file
            FileWriter writer = new FileWriter(file.getFileDescriptor());
            writer.write(stringBuilder.toString());
            writer.close();
            file.close();
        } catch (IOException exception) {
            // An error happened while writing the line
            SafeToast.show(this.context, R.string.cannot_export_settings);
            OopsHandler.collectStackTrace(exception);
        }
    }
}

