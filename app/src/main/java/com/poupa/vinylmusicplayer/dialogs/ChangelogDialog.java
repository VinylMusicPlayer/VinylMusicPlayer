package com.poupa.vinylmusicplayer.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.afollestad.materialdialogs.MaterialDialog;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * @author Aidan Follestad (afollestad)
 */
public class ChangelogDialog extends DialogFragment {

    public static ChangelogDialog create() {
        return new ChangelogDialog();
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final FragmentActivity activity = getActivity();
        assert(activity != null);

        final View customView = getLayoutInflater().inflate(R.layout.dialog_changelog_view, null);
        MaterialDialog dialog = new MaterialDialog.Builder(activity)
                .title(R.string.changelog)
                .customView(customView, false)
                .positiveText(android.R.string.ok)
                .showListener(dialog1 -> setChangelogRead(activity))
                .build();

        final TextView textView = customView.findViewById(R.id.text_view);
        try {
            // Load from CHANGELOG.md in the assets folder
            StringBuilder buf = new StringBuilder();
            InputStream json = activity.getAssets().open("CHANGELOG.md");
            BufferedReader in = new BufferedReader(new InputStreamReader(json, StandardCharsets.UTF_8));
            String str;
            while ((str = in.readLine()) != null) {
                buf.append(str);
                buf.append('\n');
            }
            in.close();

            textView.setText(buf.toString());
        } catch (Throwable e) {
            textView.setText("Unable to load change log\n" + e.getLocalizedMessage());
        }
        return dialog;
    }

    public static void setChangelogRead(@NonNull Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            int currentVersion = pInfo.versionCode;
            PreferenceUtil.getInstance().setLastChangeLogVersion(currentVersion);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static String colorToHex(int color) {
        return Integer.toHexString(color).substring(2);
    }
}
