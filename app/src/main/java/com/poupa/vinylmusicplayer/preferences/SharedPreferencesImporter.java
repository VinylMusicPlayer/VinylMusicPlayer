package com.poupa.vinylmusicplayer.preferences;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;
import com.google.gson.ToNumberStrategy;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.ui.activities.SettingsActivity;
import com.poupa.vinylmusicplayer.util.OopsHandler;
import com.poupa.vinylmusicplayer.util.SafeToast;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

public class SharedPreferencesImporter extends AppCompatActivity {

    private Context context;
    private ActivityResultLauncher importFilePicker;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this.getApplicationContext();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.context);
        importFilePicker = registerForActivityResult(new ActivityResultContracts.OpenDocument(), result -> {
            // Unless the selection has been cancelled, create the export file
            if(result != null) {
                importSettings(result);
            }
            // Finishes the last activity to return to the settings activity.
            this.finish();
        });
        importFilePicker.launch(new String[]{"text/plain"});
    }

    private void importSettings(Uri location) {
        // Prepare the table used to store the lines
        Map<String, ?> preferences;
        Gson gson = new GsonBuilder().setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE).create();
        SharedPreferences.Editor spEditor = sharedPreferences.edit();

        try {
            // Try to open the file
            ParcelFileDescriptor file = this.context.getContentResolver().openFileDescriptor(location, "r") ;
            if(file == null) return;

            // Read the content from the file line by line
            BufferedReader reader = new BufferedReader(new FileReader(file.getFileDescriptor()));
            preferences = gson.fromJson(reader, Map.class);

            String fileFormat = (String) preferences.get("file_format");
            int versionCode = (int) preferences.get("version_code");

            if(fileFormat == "json" && versionCode == context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode) {

                for (Map.Entry<String, ?> entry : preferences.entrySet()) {

                    if(entry.getKey() == "file_format" || entry.getKey() == "version_code") continue;

                    Object object = entry.getValue();
                    String key = entry.getKey();
                    if (object instanceof String) {
                        spEditor.putString(key, (String) object);
                    } else if (object instanceof Long) {
                        spEditor.putInt(key, object == null ? null : Math.toIntExact((Long) object));
                    } else if (object instanceof Boolean) {
                        spEditor.putBoolean(key, (Boolean) object);
                    } else if (object instanceof Float) {
                        spEditor.putFloat(key, (Float) object);
                    }
                }
                reader.close();
                file.close();

            } else if (fileFormat != "json" && versionCode == context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode) {
                // Import as another format
            } else if (fileFormat == "json" && versionCode != context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode) {
                // Settings migration
            }
        }
        catch(IOException exception)
        {
            // An error happened while reading the file
            SafeToast.show(this.context, R.string.cannot_import_settings);
            OopsHandler.collectStackTrace(exception);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
        spEditor.apply();
    }

}
