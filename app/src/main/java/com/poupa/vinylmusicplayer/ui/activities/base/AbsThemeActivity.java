package com.poupa.vinylmusicplayer.ui.activities.base;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.annotation.ColorInt;

import com.kabouzeid.appthemehelper.ATH;
import com.kabouzeid.appthemehelper.ATHActivity;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.common.ATHToolbarActivity;
import com.kabouzeid.appthemehelper.util.ColorUtil;
import com.kabouzeid.appthemehelper.util.MaterialDialogsUtil;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.interfaces.ThemeEventListener;
import com.poupa.vinylmusicplayer.util.OopsHandler;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;
import com.poupa.vinylmusicplayer.util.Util;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */

public abstract class AbsThemeActivity extends ATHToolbarActivity implements ThemeEventListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Thread.setDefaultUncaughtExceptionHandler(new OopsHandler(this));

        setTheme(PreferenceUtil.getInstance().getGeneralTheme());
    }

    //@Override
    //protected void onResume() {
    //    super.onResume();
    //
    //    onThemeColorsChanged();
    //}

    @Override
    protected void onDestroy() {
        Thread.setDefaultUncaughtExceptionHandler(null);
        super.onDestroy();
    }

    protected void setDrawUnderStatusbar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Util.setAllowDrawUnderStatusBar(getWindow());
        }
        else {
            Util.setStatusBarTranslucent(getWindow());
        }
    }

    /**
     * This will set the color of the view with the id "status_bar".
     * On Lollipop if no such view is found it will set the statusbar color using the native method.
     *
     * @param color the new statusbar color (will be shifted down on Lollipop and above)
     */
    public static void static_setStatusbarColor(final Activity pActivity, int color) {
        final View statusBar = pActivity.getWindow().getDecorView().getRootView().findViewById(R.id.status_bar);
        if (statusBar != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                statusBar.setBackgroundColor(color);
                ATH.setLightStatusbar(pActivity, ColorUtil.isColorLight(color));
            } else {
                statusBar.setBackgroundColor(color);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            pActivity.getWindow().setStatusBarColor(ColorUtil.darkenColor(color));
            ATH.setLightStatusbar(pActivity, ColorUtil.isColorLight(color));
        }
    }

    protected void setStatusbarColor(int color) {
        final View statusBar = getWindow().getDecorView().getRootView().findViewById(R.id.status_bar);
        if (statusBar != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                statusBar.setBackgroundColor(color);
                setLightStatusbarAuto(color);
            } else {
                statusBar.setBackgroundColor(color);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ColorUtil.darkenColor(color));
            setLightStatusbarAuto(color);
        }
    }

    public void setStatusbarColorAuto() {
        // we don't want to use statusbar color because we are doing the color darkening on our own to support KitKat
        setStatusbarColor(ThemeStore.primaryColor(this));
    }

    protected void setTaskDescriptionColor(@ColorInt int color) {
        ATH.setTaskDescriptionColor(this, color);
    }

    public void setTaskDescriptionColorAuto() {
        setTaskDescriptionColor(ThemeStore.primaryColor(this));
    }

    protected void setNavigationbarColor(int color) {
        if (ThemeStore.coloredNavigationBar(this)) {
            ATH.setNavigationbarColor(this, color);
        } else {
            ATH.setNavigationbarColor(this, Color.BLACK);
        }
    }

    public void setNavigationbarColorAuto() {
        setNavigationbarColor(ThemeStore.navigationBarColor(this));
    }

    protected void setLightStatusbar(boolean enabled) {
        ATH.setLightStatusbar(this, enabled);
    }

    protected void setLightStatusbarAuto(int bgColor) {
        setLightStatusbar(ColorUtil.isColorLight(bgColor));
    }

    //@Override
    public void onThemeColorsChanged()
    {
        MaterialDialogsUtil.updateMaterialDialogsThemeSingleton(this);

        // Some activities, ie ArtistDetails and AlbumDetails, have the status bar colored in the same
        // palette as the covert art
        if (!overrideThemeColorsForStatusBar()) {setStatusbarColorAuto();}

        setNavigationbarColorAuto();
        setTaskDescriptionColorAuto();
    }

    @Override
    public boolean overrideThemeColorsForStatusBar() {
        return false;
    }
}
