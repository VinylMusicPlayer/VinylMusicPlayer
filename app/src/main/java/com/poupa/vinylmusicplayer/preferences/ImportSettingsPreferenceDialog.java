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

import static android.app.Activity.RESULT_OK;

/**
 * @author Andreas Lechner ()
 */
public class ImportSettingsPreferenceDialog extends AppCompatActivity {

    /*
    @NonNull
    public static ImportSettingsPreferenceDialog newInstance(@NonNull String preference) {
        return new ImportSettingsPreferenceDialog(preference);
    }*/

    public static void start(Context context) {
        Intent intent = new Intent(context, ImportSettingsPreferenceDialog.class);
        context.startActivity(intent);
    }

    //@NonNull private final String preferenceKey;

    private Context context;
    private ActivityResultLauncher importFilePicker;

    /*public ImportSettingsPreferenceDialog(@NonNull String preference) {
        preferenceKey = preference;
        this.readFileIntent();
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this.getApplicationContext();
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

    private void importSharedPreferencesWithPermission() {
        //openDocumentLauncher.launch(new String[]{"text/plain"});
    }


    private Intent readFileIntent() {
        Intent intent = null;
        //if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
        intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);//, MediaStore.Downloads.EXTERNAL_CONTENT_URI);
        //intent = new Intent(Intent.ACTION_);//, MediaStore.Downloads.EXTERNAL_CONTENT_URI);
        //}

        //Context context = getContext();
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");

        Log.i(ImportSettingsPreferenceDialog.class.getName(), "Intent");

        //this.startActivityRe(intent);

        return intent;
    }

    private void importSettings(Uri location) {
        /*
        StringBuilder stringBuilder = new StringBuilder();
        Map<String, ?> prefsMap;

        for (Map.Entry<String, ?> entry : prefsMap.entrySet()) {
            stringBuilder.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
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
            OopsHandler.collectStackTrace(exception);
        }
        */


        // Prepare the table used to store the lines
        ArrayList<String> content = new ArrayList<>() ;
        String buffer ;

        try
        {
            // Try to open the file
            ParcelFileDescriptor file = this.context.getContentResolver().openFileDescriptor(location, "r") ;
            if(file == null) return ;

            // Read the content from the file line by line
            BufferedReader reader = new BufferedReader(new FileReader(file.getFileDescriptor())) ;
            while((buffer = reader.readLine()) != null) {
                Log.i(this.getLocalClassName(), buffer);
                //content.add(buffer) ;
            }
            reader.close() ;
            file.close() ;
        }
        catch(IOException exception)
        {
            // An error happened while reading the file
            OopsHandler.collectStackTrace(exception);
        }
        //return content ;


    }

}
