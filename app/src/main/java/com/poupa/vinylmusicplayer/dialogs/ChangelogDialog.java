package com.poupa.vinylmusicplayer.dialogs;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

import com.poupa.vinylmusicplayer.util.PreferenceUtil;

/**
 * @author Aidan Follestad (afollestad)
 * @author SC (soncaokim)
 */
public class ChangelogDialog extends MarkdownViewDialog {
    public ChangelogDialog(Builder builder) {
        super(builder);

        final Context context = builder.getContext();
        setMarkdownContentFromAsset(context, "CHANGELOG.md");
        setOnDismissListener(dialog -> setChangelogRead(context));
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

    public static class Builder extends MarkdownViewDialog.Builder {
        public Builder(@NonNull Context context) {
            super(context);
        }

        @Override
        @UiThread
        public ChangelogDialog build() {
            super.build();

            return new ChangelogDialog(this);
        }
    }
}
