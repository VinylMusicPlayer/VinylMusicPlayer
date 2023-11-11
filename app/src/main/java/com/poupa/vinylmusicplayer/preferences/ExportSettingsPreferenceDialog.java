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

import java.io.File;
import java.io.FileOutputStream;
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
public class ExportSettingsPreferenceDialog extends DialogFragment {

    @NonNull
    public static ExportSettingsPreferenceDialog newInstance(@NonNull String preference) {
        return new ExportSettingsPreferenceDialog(preference);
    }

    @NonNull private final String preferenceKey;
    //private final int WRITE_REQUEST_CODE = 1;

    public ExportSettingsPreferenceDialog(@NonNull String preference) {
        preferenceKey = preference;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Gson gson = new Gson(); // com.google.gson.Gson
        //String jsonFromMap = gson.toJson(allPreferences);
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
            //createFile(filename);
            //buttonCreateFile(activity, filename, jsonFromMap);
            //preferences;
            SharedPreferencesExporter spe = new SharedPreferencesExporter();//App.getStaticContext());
            //spe.export();

        });
        alert.setNegativeButton(android.R.string.cancel, (dialog, id) -> {
            // User cancels the dialog.
        });

        return alert.create();

    }

    private String getKeyValuePair(Map<String, String> preferences, ArrayList<String> blacklist) {
        Log.i(ExportSettingsPreferenceDialog.class.getName(), preferences.toString());
        Log.i(ExportSettingsPreferenceDialog.class.getName(), blacklist.toString());
        StringBuilder out = new StringBuilder();

        for(String key: preferences.keySet()) {
            out.append(key+"="+preferences.get(key)+"\n");
        }

        out.append("blacklist="+blacklist.toString());

        return out.toString();
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

    private Intent createFileIntent(String filename) {
        Intent intent = null;
        //if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
        intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);//, MediaStore.Downloads.EXTERNAL_CONTENT_URI);
        //intent = new Intent(Intent.ACTION_);//, MediaStore.Downloads.EXTERNAL_CONTENT_URI);
        //}

        //Context context = getContext();
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TITLE, filename);

        this.startActivity(intent);

        return intent;
    }

    public void buttonCreateFile(Activity activity, String filename, String content) {

        //intent.setAction(Intent.AC);
        //intent.putExtra(Intent.EXTRA_TEXT, content);
        //intent.setType("*/*");
        Intent intent = createFileIntent(filename);
        //activity.startActivityForResult(intent, WRITE_REQUEST_CODE);


        try {
            //Uri uri = context.getContentResolver().insert(intent.getData(), null);
            ParcelFileDescriptor pfd = activity.getContentResolver().openFileDescriptor(intent.getData(), "w");
            FileOutputStream fileOutputStream = new FileOutputStream(pfd.getFileDescriptor());
            fileOutputStream.write(content.getBytes());
            fileOutputStream.close();
            pfd.close();
        } catch (IOException e) {
            Log.e(ExportSettingsPreferenceDialog.class.getName(), e.getMessage());
        }
        //Log.i(ExportSettingsPreferenceDialog.class.getName(), intent.getData().toString());
    }

    private void doActivityThings(Activity activity, Intent intent, String content) {

    }




    public void buttonOpenFile(View view){
        Intent intent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            intent = new Intent(Intent.ACTION_VIEW); //, MediaStore.Downloads.EXTERNAL_CONTENT_URI);
        }
        //intent.setType("application/pdf");
        intent.setType("*/*");
        this.startActivity(intent);
    }




}
