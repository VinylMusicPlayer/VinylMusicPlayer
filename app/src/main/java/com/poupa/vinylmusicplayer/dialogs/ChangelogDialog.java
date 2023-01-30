package com.poupa.vinylmusicplayer.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.afollestad.materialdialogs.MaterialDialog;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;

import es.dmoral.markdownview.MarkdownView;

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
                //.title(R.string.changelog)
                .customView(customView, false)
                .positiveText(android.R.string.ok)
                .showListener(dialog1 -> setChangelogRead(activity))
                .build();

        final MarkdownView markdownView = customView.findViewById(R.id.markdown_view);
        try {
            // Load from CHANGELOG.md in the assets folder
            markdownView.loadFromAssets("CHANGELOG.md");
        } catch (Throwable e) {
            markdownView.loadFromText("Unable to load change log\n" + e.getLocalizedMessage());
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
}
