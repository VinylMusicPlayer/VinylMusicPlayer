package com.poupa.vinylmusicplayer.preferences;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.preference.Preference;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;

import com.google.gson.Gson;
import com.poupa.vinylmusicplayer.App;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.provider.BlacklistStore;
import com.poupa.vinylmusicplayer.ui.activities.SettingsActivity;
import com.poupa.vinylmusicplayer.util.DataTypeUtil;
import com.poupa.vinylmusicplayer.util.OopsHandler;
import com.poupa.vinylmusicplayer.util.SafeToast;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;
import com.poupa.vinylmusicplayer.util.SharedPreferencesExporter;
import com.poupa.vinylmusicplayer.util.StringUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

/**
 * @author Andreas Lechner ()
 */
public class ImportSettingsPreferenceDialog extends AppCompatActivity {

    public static void start(Context context) {
        Intent intent = new Intent(context, ImportSettingsPreferenceDialog.class);
        context.startActivity(intent);
    }

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
            while((buffer = reader.readLine()) != null) {
                preference = buffer.split("=");
                Log.i(this.getLocalClassName(), buffer);
                String key = preference[0];
                String value = preference[1];
                if (DataTypeUtil.checkType(value) instanceof String) {
                    spEditor.putString(key, value);
                } else if (DataTypeUtil.checkType(value) instanceof Integer) {
                    spEditor.putInt(key, Integer.parseInt(value));
                } else if (DataTypeUtil.checkType(value) instanceof Boolean) {
                    spEditor.putBoolean(key, Boolean.parseBoolean(value));
                } else if (DataTypeUtil.checkType(value) instanceof Double) {
                    spEditor.putFloat(key, Float.parseFloat(value));
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
    }

}
