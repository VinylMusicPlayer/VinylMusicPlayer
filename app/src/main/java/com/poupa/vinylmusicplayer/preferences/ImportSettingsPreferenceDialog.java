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
public class ImportSettingsPreferenceDialog extends AppCompatActivity {

    @NonNull
    public static ImportSettingsPreferenceDialog newInstance(@NonNull String preference) {
        return new ImportSettingsPreferenceDialog(preference);
    }

    @NonNull private final String preferenceKey;
    //private final int WRITE_REQUEST_CODE = 1;

    public ImportSettingsPreferenceDialog(@NonNull String preference) {
        preferenceKey = preference;
        //this.readFileIntent();
    }


    private final ActivityResultLauncher<String[]> openDocumentLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(),
                    result -> {
                        if (result != null) {
                            //readFileIntent();
                            Log.i(ImportSettingsPreferenceDialog.class.getName(), "result not null " + result);
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        importSharedPreferencesWithPermission();
    }

    private void importSharedPreferencesWithPermission() {
        openDocumentLauncher.launch(new String[]{"text/plain"});
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


}
