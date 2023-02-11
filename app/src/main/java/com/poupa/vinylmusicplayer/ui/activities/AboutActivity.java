package com.poupa.vinylmusicplayer.ui.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import com.google.android.material.resources.TextAppearance;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.databinding.ActivityAboutBinding;
import com.poupa.vinylmusicplayer.dialogs.ChangelogDialog;
import com.poupa.vinylmusicplayer.dialogs.MarkdownViewDialog;
import com.poupa.vinylmusicplayer.ui.activities.base.AbsBaseActivity;
import com.poupa.vinylmusicplayer.ui.activities.bugreport.BugReportActivity;
import com.poupa.vinylmusicplayer.ui.activities.intro.AppIntroActivity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.function.Function;

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

        applyThemeColors();

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

    // Needed as webview is interpreting pixels as dp
    private static int px2dip(Context context, float pxValue) {
        final float scale =  context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
    // Needed as webview doesn't understand #aarrggbb
    private static String hex2rgba(int color) {
        float a = Color.alpha(color) / 255.0f;
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        return "rgba("+r+","+g+","+b+","+String.format(Locale.US, "%.02f", a)+")";
    }

    @SuppressLint("RestrictedApi")
    private void setUpContributorsView()
    {
        final WebView webView = layoutBinding.content.cardContributors.viewContributors;
        try {
            StringBuilder buf = new StringBuilder();
            InputStream json = getAssets().open("credits.html");
            BufferedReader in = new BufferedReader(new InputStreamReader(json, StandardCharsets.UTF_8));
            String str;
            while ((str = in.readLine()) != null) {buf.append(str).append("\n");}
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

            final TextAppearance captionStyle = new TextAppearance(this, R.style.TextAppearance_AppCompat_Caption);
            final String captionTextColor = hex2rgba(captionStyle.getTextColor().getDefaultColor());
            final String captionSize = String.valueOf(px2dip(this, captionStyle.getTextSize()));

            final String titleTextColor = hex2rgba(ThemeStore.textColorSecondary(this));
            final TextAppearance titleStyle = new TextAppearance(this, R.style.TextAppearance_AppCompat_Body2);
            final String titleSize = String.valueOf(px2dip(this, titleStyle.getTextSize()));

            getTheme().resolveAttribute(R.attr.dividerColor, typedColor, true);
            String dividerColor = hex2rgba(typedColor.data);

            int margin_i = px2dip(this, getResources().getDimensionPixelSize(R.dimen.default_item_margin));
            String margin = String.valueOf(margin_i);
            String margin_2 = String.valueOf(margin_i/2);
            String margin_4 = String.valueOf(margin_i/4);

            String titleTopMargin = String.valueOf(px2dip(this, getResources().getDimensionPixelSize(R.dimen.title_top_margin)));

            final String recoloredBuf = buf.toString()
                    .replace("%{color}", contentColor)
                    .replace("%{background-color}", backgroundColor)
                    .replace("%{divider-color}", dividerColor)
                    .replace("%{caption-color}", captionTextColor)
                    .replace("%{caption-size}", captionSize)
                    .replace("%{title-color}", titleTextColor)
                    .replace("%{title-size}", titleSize)
                    .replace("%{link-color}", contentColor)
                    .replace("%{margin}", margin)
                    .replace("%{margin_2}", margin_2)
                    .replace("%{margin_4}", margin_4)
                    .replace("%{title-top-margin}", titleTopMargin)
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
            new ChangelogDialog.Builder(this).show();
        } else if (v == layoutBinding.content.cardAboutApp.licenses) {
            new MarkdownViewDialog.Builder(this).build()
                    .setMarkdownContentFromAsset(this,"LICENSES.md")
                    .show();
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
}
