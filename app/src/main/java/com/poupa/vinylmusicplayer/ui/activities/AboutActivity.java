package com.poupa.vinylmusicplayer.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import com.afollestad.materialdialogs.internal.ThemeSingleton;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.util.ATHUtil;
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

import de.psdev.licensesdialog.LicensesDialog;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class AboutActivity extends AbsBaseActivity implements View.OnClickListener {

    private static final String GITHUB = "https://github.com/VinylMusicPlayer/VinylMusicPlayer";

    private static final String WEBSITE = "https://adrien.poupa.fr/";

    private static final String RATE_ON_GOOGLE_PLAY = "https://play.google.com/store/apps/details?id=com.poupa.vinylmusicplayer";

    private static final String KABOUZEID_GOOGLE_PLUS = "https://google.com/+KarimAbouZeid23697";
    private static final String KABOUZEID_WEBSITE = "https://kabouzeid.com";

    private static final String AIDAN_FOLLESTAD_GOOGLE_PLUS = "https://google.com/+AidanFollestad";
    private static final String AIDAN_FOLLESTAD_GITHUB = "https://github.com/afollestad";

    private static final String MICHAEL_COOK_GOOGLE_PLUS = "https://plus.google.com/102718493746376292361";
    private static final String MICHAEL_COOK_WEBSITE = "https://cookicons.co/";

    private static final String MAARTEN_CORPEL_GOOGLE_PLUS = "https://google.com/+MaartenCorpel";

    private static final String ALEKSANDAR_TESIC_GOOGLE_PLUS = "https://google.com/+aleksandartešić";

    private static final String EUGENE_CHEUNG_GITHUB = "https://github.com/arkon";
    private static final String EUGENE_CHEUNG_WEBSITE = "https://echeung.me/";

    private static final String ADRIAN_TWITTER = "https://twitter.com/froschgames";
    private static final String ADRIAN_WEBSITE = "https://froschgames.com/";

    private ActivityAboutBinding layoutBinding;

    private Toolbar toolbar;
    private TextView appVersion;
    private LinearLayout changelog;
    private LinearLayout intro;
    private LinearLayout licenses;
    private LinearLayout forkOnGitHub;
    private LinearLayout reportBugs;
    private LinearLayout rateOnGooglePlay;

    private AppCompatButton kabouzeidGooglePlus;
    private AppCompatButton kabouzeidWebsite;
    private AppCompatButton aidanFollestadGooglePlus;
    private AppCompatButton aidanFollestadGitHub;
    private AppCompatButton michaelCookGooglePlus;
    private AppCompatButton michaelCookWebsite;
    private AppCompatButton maartenCorpelGooglePlus;
    private AppCompatButton aleksandarTesicGooglePlus;
    private AppCompatButton eugeneCheungGitHub;
    private AppCompatButton eugeneCheungWebsite;
    private AppCompatButton adrianTwitter;
    private AppCompatButton adrianWebsite;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO Catch InflateException - might mean that some component - ie WebView - not available on the system.
        layoutBinding = ActivityAboutBinding.inflate(LayoutInflater.from(this));

        // TODO Remove all these local copies
        toolbar = layoutBinding.toolbar;

        appVersion = layoutBinding.content.cardAboutApp.appVersion;
        changelog = layoutBinding.content.cardAboutApp.changelog;
        intro = layoutBinding.content.cardAboutApp.intro;
        licenses = layoutBinding.content.cardAboutApp.licenses;
        forkOnGitHub = layoutBinding.content.cardAboutApp.forkOnGithub;

        reportBugs = layoutBinding.content.cardSupportDevelopment.reportBugs;
        rateOnGooglePlay = layoutBinding.content.cardSupportDevelopment.rateOnGooglePlay;

        kabouzeidGooglePlus = layoutBinding.content.cardSpecialThanks.kabouzeidGooglePlus;
        kabouzeidWebsite = layoutBinding.content.cardSpecialThanks.kabouzeidWebsite;
        aidanFollestadGooglePlus = layoutBinding.content.cardSpecialThanks.aidanFollestadGooglePlus;
        aidanFollestadGitHub = layoutBinding.content.cardSpecialThanks.aidanFollestadGitHub;
        michaelCookGooglePlus = layoutBinding.content.cardSpecialThanks.michaelCookGooglePlus;
        michaelCookWebsite = layoutBinding.content.cardSpecialThanks.michaelCookWebsite;
        maartenCorpelGooglePlus = layoutBinding.content.cardSpecialThanks.maartenCorpelGooglePlus;
        aleksandarTesicGooglePlus = layoutBinding.content.cardSpecialThanks.aleksandarTesicGooglePlus;
        eugeneCheungGitHub = layoutBinding.content.cardSpecialThanks.eugeneCheungGitHub;
        eugeneCheungWebsite = layoutBinding.content.cardSpecialThanks.eugeneCheungWebsite;
        adrianTwitter = layoutBinding.content.cardSpecialThanks.adrianTwitter;
        adrianWebsite = layoutBinding.content.cardSpecialThanks.adrianWebsite;

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
        toolbar.setBackgroundColor(ThemeStore.primaryColor(this));
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setUpAppVersion() {
        appVersion.setText(getCurrentVersionName(this));
    }

    private void setUpContributorsView()
    {
        final WebView webView = layoutBinding.content.cardContributors.viewContributors;
        try {
            StringBuilder buf = new StringBuilder();
            InputStream json = getAssets().open("contributors.html");
            BufferedReader in = new BufferedReader(new InputStreamReader(json, StandardCharsets.UTF_8));
            String str;
            while ((str = in.readLine()) != null) {buf.append(str);}
            in.close();

            // Inject color values for WebView body background and links
            final String backgroundColor = ChangelogDialog.colorToHex(ATHUtil.resolveColor(this,
                    R.attr.md_background_color,
                    Color.parseColor(ThemeSingleton.get().darkTheme ? "#000000" : "#ffffff"))); // TODO Get the color from the theme, not hardcoded
            final String contentColor = ThemeSingleton.get().darkTheme ? "#ffffff" : "#000000"; // TODO Get the color from the theme, not hardcoded
            final String defaultColor = ChangelogDialog.colorToHex(ThemeSingleton.get().positiveColor.getDefaultColor());
            final String style = String.format("body { background-color: %s; color: %s; }",
                    backgroundColor, contentColor);
            final String recoloredBuf = buf.toString()
                    .replace("%{style-placeholder}", style)
                    .replace("%{link-color}", contentColor);

            String base64Buf = Base64.encodeToString(recoloredBuf.getBytes(StandardCharsets.UTF_8), Base64.DEFAULT);
            webView.loadData(base64Buf, "text/html; charset=UTF-8", "base64");
        } catch (Throwable e) {
            webView.loadData("<h1>Unable to load</h1><p>" + e.getLocalizedMessage() + "</p>", "text/html", "UTF-8");
        }
    }

    private void setUpOnClickListeners() {
        changelog.setOnClickListener(this);
        intro.setOnClickListener(this);
        licenses.setOnClickListener(this);
        forkOnGitHub.setOnClickListener(this);
        reportBugs.setOnClickListener(this);
        rateOnGooglePlay.setOnClickListener(this);
        aidanFollestadGooglePlus.setOnClickListener(this);
        aidanFollestadGitHub.setOnClickListener(this);
        kabouzeidGooglePlus.setOnClickListener(this);
        kabouzeidWebsite.setOnClickListener(this);
        michaelCookGooglePlus.setOnClickListener(this);
        michaelCookWebsite.setOnClickListener(this);
        maartenCorpelGooglePlus.setOnClickListener(this);
        aleksandarTesicGooglePlus.setOnClickListener(this);
        eugeneCheungGitHub.setOnClickListener(this);
        eugeneCheungWebsite.setOnClickListener(this);
        adrianTwitter.setOnClickListener(this);
        adrianWebsite.setOnClickListener(this);
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
        if (v == changelog) {
            ChangelogDialog.create().show(getSupportFragmentManager(), "CHANGELOG_DIALOG");
        } else if (v == licenses) {
            showLicenseDialog();
        } else if (v == intro) {
            startActivity(new Intent(this, AppIntroActivity.class));
        } else if (v == forkOnGitHub) {
            openUrl(GITHUB);
        } else if (v == reportBugs) {
            startActivity(new Intent(this, BugReportActivity.class));
        } else if (v == rateOnGooglePlay) {
            openUrl(RATE_ON_GOOGLE_PLAY);
        } else if (v == aidanFollestadGooglePlus) {
            openUrl(AIDAN_FOLLESTAD_GOOGLE_PLUS);
        } else if (v == aidanFollestadGitHub) {
            openUrl(AIDAN_FOLLESTAD_GITHUB);
        } else if (v == kabouzeidGooglePlus) {
            openUrl(KABOUZEID_GOOGLE_PLUS);
        } else if (v == kabouzeidWebsite) {
            openUrl(KABOUZEID_WEBSITE);
        } else if (v == michaelCookGooglePlus) {
            openUrl(MICHAEL_COOK_GOOGLE_PLUS);
        } else if (v == michaelCookWebsite) {
            openUrl(MICHAEL_COOK_WEBSITE);
        } else if (v == maartenCorpelGooglePlus) {
            openUrl(MAARTEN_CORPEL_GOOGLE_PLUS);
        } else if (v == aleksandarTesicGooglePlus) {
            openUrl(ALEKSANDAR_TESIC_GOOGLE_PLUS);
        } else if (v == eugeneCheungGitHub) {
            openUrl(EUGENE_CHEUNG_GITHUB);
        } else if (v == eugeneCheungWebsite) {
            openUrl(EUGENE_CHEUNG_WEBSITE);
        } else if (v == adrianTwitter) {
            openUrl(ADRIAN_TWITTER);
        } else if (v == adrianWebsite) {
            openUrl(ADRIAN_WEBSITE);
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
