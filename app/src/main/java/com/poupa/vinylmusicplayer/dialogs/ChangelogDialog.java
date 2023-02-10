package com.poupa.vinylmusicplayer.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.poupa.vinylmusicplayer.util.PreferenceUtil;

/**
 * @author Aidan Follestad (afollestad)
 * @author SC (soncaokim)
 */
public class ChangelogDialog extends MarkdownViewDialog {
    public ChangelogDialog() {
        super("CHANGELOG.md");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog( savedInstanceState);
        dialog.setOnShowListener(dialog1 -> setChangelogRead(requireActivity()));

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
