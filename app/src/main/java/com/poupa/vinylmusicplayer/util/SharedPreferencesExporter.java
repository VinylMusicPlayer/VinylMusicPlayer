package com.poupa.vinylmusicplayer.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.poupa.vinylmusicplayer.App;
import com.poupa.vinylmusicplayer.ui.activities.SettingsActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import androidx.preference.PreferenceManager;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class SharedPreferencesExporter extends AppCompatActivity {

    private static final String SHARED_PREFS_NAME = "MySharedPrefsFile";
    public static final int WRITE_REQUEST_CODE = 1;

    private Context context;
    private String content;
    private ActivityResultLauncher<String> exportFilePicker;

    public SharedPreferencesExporter() { //(Context context) {//, Activity activity) {
        //this.context = this.getApplicationContext();
        //this.content = content;
    }

    private void setContent(String content) {
        this.content = content;
    }

    private String getContent() {
        return this.content;
    }

    private SharedPreferences sharedPreferences;

    /*
    private final ActivityResultLauncher<String> createDocumentLauncher =
            registerForActivityResult(new ActivityResultContracts.CreateDocument("text/plain"),
                    result -> {
                        if (result != null) {
                            try {
                                exportSharedPreferences(result);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
     */
    public static void start(Context context, String filename) {
        Intent intent = new Intent(context, SharedPreferencesExporter.class);
        intent.putExtra("filename", filename);
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
                writeToExportFile(result);
            }
            //this.context.startActivity(new Intent().setClass(this.context, SettingsActivity.class).addFlags(FLAG_ACTIVITY_NEW_TASK)) ;
            this.finish();
        });
        exportFilePicker.launch(b.getString("filename"));
        //exportSharedPreferencesWithPermission();
    }

    private static String getCurrentFormattedDateTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault());
        return simpleDateFormat.format(new Date(System.currentTimeMillis()));
    }

    private static String getExportSettingsFilename() {
        return "vinylmusicplayer-settings_"+ getCurrentFormattedDateTime();
    }

    private void exportSharedPreferencesWithPermission() {
        //createDocumentLauncher.launch(this.getExportSettingsFilename());
    }

    private void exportSharedPreferences(Uri uri) throws IOException {
        OutputStream outputStream = getContentResolver().openOutputStream(uri);
        if (outputStream != null) {
            StringBuilder stringBuilder = new StringBuilder();
            Map<String, ?> prefsMap = sharedPreferences.getAll();
            for (Map.Entry<String, ?> entry : prefsMap.entrySet()) {
                stringBuilder.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }

            outputStream.write(stringBuilder.toString().getBytes());
            outputStream.close();
        }
    }

    public void export() { //Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.context);
        String sharedPrefsString = sharedPreferences.getAll().toString();
        this.setContent(sharedPrefsString);

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TITLE, "shared_prefs.txt");
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);

        this.context.startActivity(intent);
        this.startActivityForResult(intent, 1);

        //Uri uri = intent.getData();
        //Log.i(SharedPreferencesExporter.class.getName(), uri.toString());
        //Log.i(SharedPreferencesExporter.class.getName(), "Bis hier her");

    }

    private void writeToExportFile(Uri location)
    {
        StringBuilder stringBuilder = new StringBuilder();
        Map<String, ?> prefsMap = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : prefsMap.entrySet()) {
            stringBuilder.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }

        // Write all lines in the export file
        try
        {
            // Try to open the file
            ParcelFileDescriptor file = this.context.getContentResolver().openFileDescriptor(location, "w") ;
            if(file == null) return;

            // Write all lines in the file
            FileWriter writer = new FileWriter(file.getFileDescriptor()) ;
            writer.write(stringBuilder.toString());
            writer.close() ;
            file.close() ;
        }
        catch(IOException exception)
        {
            // An error happened while writing the line
            OopsHandler.collectStackTrace(exception);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == WRITE_REQUEST_CODE) {
            try {
                FileOutputStream fileOutupStream = (FileOutputStream) getContentResolver().openOutputStream(data.getData());
                try {
                    fileOutupStream.write(this.getContent().getBytes());
                    fileOutupStream.flush();
                    fileOutupStream.close();
                    //Toast.makeText(this, "saved " + fileName, Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    //Toast.makeText(this, "something went wrong" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {

            }

        }
    }


}

