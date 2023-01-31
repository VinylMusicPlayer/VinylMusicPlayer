package com.poupa.vinylmusicplayer.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.TypedValue;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;

import androidx.annotation.NonNull;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.internal.ThemeSingleton;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.databinding.ActivityAboutBinding;
import com.poupa.vinylmusicplayer.dialogs.ChangelogDialog;
import com.poupa.vinylmusicplayer.ui.activities.base.AbsBaseActivity;
import com.poupa.vinylmusicplayer.ui.activities.bugreport.BugReportActivity;
import com.poupa.vinylmusicplayer.ui.activities.intro.AppIntroActivity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

import de.psdev.licensesdialog.LicensesDialog;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class AboutActivity extends AbsBaseActivity implements View.OnClickListener {

    private static final String GITHUB = "https://github.com/VinylMusicPlayer/VinylMusicPlayer";

    private static final String WEBSITE = "https://adrien.poupa.fr/";

    private static final String RATE_ON_GOOGLE_PLAY = "https://play.google.com/store/apps/details?id=com.poupa.vinylmusicplayer";

    private ActivityAboutBinding layoutBinding;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            layoutBinding = ActivityAboutBinding.inflate(LayoutInflater.from(this));
        } catch (InflateException e) {
            e.printStackTrace();
            new MaterialDialog.Builder(this)
                    .title(android.R.string.dialog_alert_title)
                    .content(R.string.missing_webview_component)
                    .positiveText(android.R.string.ok)
                    .build()
                    .show();
        }

        setContentView(layoutBinding.getRoot());

        setDrawUnderStatusbar();

        setStatusbarColorAuto();
        setNavigationbarColorAuto();
        setTaskDescriptionColorAuto();

        setUpViews();
    }

    private void setUpViews() {
        setUpToolbar();
        setUpAppVersion();
        setUpContributorsView();
        setUpOnClickListeners();
    }

    private void setUpToolbar() {
        layoutBinding.toolbar.setBackgroundColor(ThemeStore.primaryColor(this));
        setSupportActionBar(layoutBinding.toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setUpAppVersion() {
        layoutBinding.content.cardAboutApp.appVersion.setText(getCurrentVersionName(this));
    }

    private void setUpContributorsView()
    {
        final WebView webView = layoutBinding.content.cardContributors.viewContributors;
        try {
            StringBuilder buf = new StringBuilder();
            InputStream json = getAssets().open("credits.html");
            BufferedReader in = new BufferedReader(new InputStreamReader(json, StandardCharsets.UTF_8));
            String str;
            while ((str = in.readLine()) != null) {buf.append(str);}
            in.close();

            // Inject color values for WebView body background and links
            Function<Integer, String> colorHex = (color) -> {
                if (color == 0) {return "000000";}
                return Integer.toHexString(color).substring(2); // strip the alpha part, keep only RGB
            };

            final TypedValue typedColor = new TypedValue();
            getTheme().resolveAttribute(R.attr.cardBackgroundColor, typedColor, true);
            final String backgroundColor = colorHex.apply(typedColor.data);
            getTheme().resolveAttribute(R.attr.iconColor, typedColor, true);
            final String contentColor = colorHex.apply(typedColor.data);

            final String recoloredBuf = buf.toString()
                    .replace("%{color}", contentColor)
                    .replace("%{background-color}", backgroundColor)
                    .replace("%{link-color}", contentColor)
                    .replace("%{@string/maintainers}", getResources().getString(R.string.maintainers))
                    .replace("%{@string/contributors}", getResources().getString(R.string.contributors))
                    .replace("%{@string/label_other_contributors}", getResources().getString(R.string.label_other_contributors))
                    .replace("%{@string/special_thanks_to}", getResources().getString(R.string.special_thanks_to))
                    .replace("%{@string/karim_abou_zeid}", getResources().getString(R.string.karim_abou_zeid))
                    .replace("%{@string/karim_abou_zeid_summary}", getResources().getString(R.string.karim_abou_zeid_summary))
                    .replace("%{@string/aidan_follestad}", getResources().getString(R.string.aidan_follestad))
                    .replace("%{@string/aidan_follestad_summary}", getResources().getString(R.string.aidan_follestad_summary))
                    .replace("%{@string/michael_cook_cookicons}", getResources().getString(R.string.michael_cook_cookicons))
                    .replace("%{@string/michael_cook_summary}", getResources().getString(R.string.michael_cook_summary))
                    .replace("%{@string/maarten_corpel}", getResources().getString(R.string.maarten_corpel))
                    .replace("%{@string/maarten_corpel_summary}", getResources().getString(R.string.maarten_corpel_summary))
                    .replace("%{@string/aleksandar_tesic}", getResources().getString(R.string.aleksandar_tesic))
                    .replace("%{@string/aleksandar_tesic_summary}", getResources().getString(R.string.aleksandar_tesic_summary))
                    .replace("%{@string/eugene_cheung}", getResources().getString(R.string.eugene_cheung))
                    .replace("%{@string/eugene_cheung_summary}", getResources().getString(R.string.eugene_cheung_summary))
                    .replace("%{@string/adrian}", getResources().getString(R.string.adrian))
                    .replace("%{@string/adrian_summary}", getResources().getString(R.string.adrian_summary))
                    ;

            String base64Buf = Base64.encodeToString(recoloredBuf.getBytes(StandardCharsets.UTF_8), Base64.DEFAULT);
            webView.loadData(base64Buf, "text/html; charset=UTF-8", "base64");
        } catch (Throwable e) {
            webView.loadData("<b>Unable to load</b><br/>" + e, "text/html", "UTF-8");
        }
    }

    private void setUpOnClickListeners() {
        layoutBinding.content.cardAboutApp.changelog.setOnClickListener(this);
        layoutBinding.content.cardAboutApp.intro.setOnClickListener(this);
        layoutBinding.content.cardAboutApp.licenses.setOnClickListener(this);
        layoutBinding.content.cardAboutApp.forkOnGithub.setOnClickListener(this);
        layoutBinding.content.cardSupportDevelopment.reportBugs.setOnClickListener(this);
        layoutBinding.content.cardSupportDevelopment.rateOnGooglePlay.setOnClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private static String getCurrentVersionName(@NonNull final Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (final PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "Unkown";
    }

    @Override
    public void onClick(final View v) {
        if (v == layoutBinding.content.cardAboutApp.changelog) {
            ChangelogDialog.create().show(getSupportFragmentManager(), "CHANGELOG_DIALOG");
        } else if (v == layoutBinding.content.cardAboutApp.licenses) {
            showLicenseDialog();
        } else if (v == layoutBinding.content.cardAboutApp.intro) {
            startActivity(new Intent(this, AppIntroActivity.class));
        } else if (v == layoutBinding.content.cardAboutApp.forkOnGithub) {
            openUrl(GITHUB);
        } else if (v == layoutBinding.content.cardSupportDevelopment.reportBugs) {
            startActivity(new Intent(this, BugReportActivity.class));
        } else if (v == layoutBinding.content.cardSupportDevelopment.rateOnGooglePlay) {
            openUrl(RATE_ON_GOOGLE_PLAY);
        }
    }

    private void openUrl(final String url) {
        final Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    private void showLicenseDialog() {
        new LicensesDialog.Builder(this)
                .setNotices(R.raw.notices)
                .setTitle(R.string.licenses)
                .setNoticesCssStyle(getString(R.string.license_dialog_style)
                        .replace("{bg-color}", ThemeSingleton.get().darkTheme ? "424242" : "ffffff")
                        .replace("{text-color}", ThemeSingleton.get().darkTheme ? "ffffff" : "000000")
                        .replace("{license-bg-color}", ThemeSingleton.get().darkTheme ? "535353" : "eeeeee")
                )
                .setIncludeOwnLicense(true)
                .build()
                .show();
    }
}
