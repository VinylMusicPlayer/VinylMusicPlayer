package com.poupa.vinylmusicplayer.preferences;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.poupa.vinylmusicplayer.ui.activities.SettingsActivity;
import com.poupa.vinylmusicplayer.util.DataTypeUtil;
import com.poupa.vinylmusicplayer.util.OopsHandler;

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

    /*
    public static void start(Context context, Activity activity) {
        Intent intent = new Intent(context, SharedPreferencesImporter.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }
    */

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
        //importSharedPreferencesWithPermission();
    }

    private void importSettings(Uri location) {
        // Prepare the table used to store the lines
        //ArrayList<String> content = new ArrayList<>() ;
        String[] preference;
        String buffer ;
        SharedPreferences.Editor spEditor = sharedPreferences.edit();

        try
        {
            // Try to open the file
            ParcelFileDescriptor file = this.context.getContentResolver().openFileDescriptor(location, "r") ;
            if(file == null) return ;

            // Read the content from the file line by line
            BufferedReader reader = new BufferedReader(new FileReader(file.getFileDescriptor())) ;
            String key = "";
            String value = "";
            while((buffer = reader.readLine()) != null) {
                if(buffer.startsWith("#")) {
                    continue;
                }
                try {
                    preference = buffer.split("=");
                    //Log.i(this.getLocalClassName(), buffer);
                    key = preference[0];
                    value = preference[1];
                } catch (Exception e) {
                    OopsHandler.collectStackTrace(e);
                }
                Object object = DataTypeUtil.checkType(value);
                if (object instanceof String) {
                    spEditor.putString(key, (String) object);
                } else if (object instanceof Integer) {
                    spEditor.putInt(key, (Integer) object);
                } else if (object instanceof Boolean) {
                    spEditor.putBoolean(key, (Boolean) object);
                } else if (object instanceof Float) {
                    spEditor.putFloat(key, (Float) object);
                }
                //content.add(buffer) ;
            }
            reader.close();
            file.close();
        }
        catch(IOException exception)
        {
            // An error happened while reading the file
            OopsHandler.collectStackTrace(exception);
        }
        //return content ;
        spEditor.apply();
        //this.finish();
    }

}
