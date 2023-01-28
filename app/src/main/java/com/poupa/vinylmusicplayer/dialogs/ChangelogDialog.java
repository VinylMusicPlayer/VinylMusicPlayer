package com.poupa.vinylmusicplayer.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.internal.ThemeSingleton;
import com.kabouzeid.appthemehelper.util.ATHUtil;
import com.kabouzeid.appthemehelper.util.ColorUtil;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

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

        final View customView;
        try {
            customView = LayoutInflater.from(activity).inflate(R.layout.dialog_web_view, null);
        } catch (InflateException e) {
            e.printStackTrace();
            return new MaterialDialog.Builder(activity)
                    .title(android.R.string.dialog_alert_title)
                    .content("This device doesn't support web view, which is necessary to view the change log. It is missing a system component.")
                    .positiveText(android.R.string.ok)
                    .build();
        }
        MaterialDialog dialog = new MaterialDialog.Builder(activity)
                .title(R.string.changelog)
                .customView(customView, false)
                .positiveText(android.R.string.ok)
                .showListener(dialog1 -> setChangelogRead(activity))
                .build();

        final WebView webView = customView.findViewById(R.id.web_view);
        try {
            // Load from vinylmusicplayer-changelog.html in the assets folder
            StringBuilder buf = new StringBuilder();
            InputStream json = activity.getAssets().open("vinylmusicplayer-changelog.html");
            BufferedReader in = new BufferedReader(new InputStreamReader(json, "UTF-8"));
            String str;
            while ((str = in.readLine()) != null)
                buf.append(str);
            in.close();

            // Inject color values for WebView body background and links
            final String backgroundColor = colorToHex(ATHUtil.resolveColor(activity, R.attr.md_background_color, Color.parseColor(ThemeSingleton.get().darkTheme ? "#424242" : "#ffffff")));
            final String contentColor = ThemeSingleton.get().darkTheme ? "#ffffff" : "#000000";
            final int defaultColor = ThemeSingleton.get().positiveColor.getDefaultColor();
            String recoloredBuf = buf.toString()
                    .replace("{style-placeholder}",
                            String.format("body { background-color: %s; color: %s; }", backgroundColor, contentColor))
                    .replace("{link-color}", colorToHex(defaultColor))
                    .replace("{link-color-active}",
                            colorToHex(ColorUtil.lightenColor(defaultColor)));
            String base64Buf = Base64.encodeToString(recoloredBuf.getBytes("UTF-8"), Base64.DEFAULT);
            webView.loadData(base64Buf, "text/html; charset=UTF-8", "base64");
        } catch (Throwable e) {
            webView.loadData("<h1>Unable to load</h1><p>" + e.getLocalizedMessage() + "</p>", "text/html", "UTF-8");
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

    public static String colorToHex(int color) {
        return Integer.toHexString(color).substring(2);
    }
}
