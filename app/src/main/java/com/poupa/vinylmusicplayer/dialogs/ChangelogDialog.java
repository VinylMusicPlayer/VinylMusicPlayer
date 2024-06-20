package com.poupa.vinylmusicplayer.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

import com.poupa.vinylmusicplayer.util.PreferenceUtil;

/**
 * @author SC (soncaokim)
 */
public class ChangelogDialog extends MarkdownViewDialog {
    protected ChangelogDialog(MarkdownViewDialog.Builder builder) {
        super(builder);
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
        public AlertDialog create() {
            setMarkdownContentFromAsset(getContext(), "CHANGELOG.md");
            setOnDismissListener(dialog -> setChangelogRead(getContext()));

            return super.create();
        }
    }
}
