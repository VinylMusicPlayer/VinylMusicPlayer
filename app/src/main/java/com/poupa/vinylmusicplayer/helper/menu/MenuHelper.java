package com.poupa.vinylmusicplayer.helper.menu;


import android.content.Context;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.ColorInt;
import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.afollestad.materialcab.MaterialCab;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;
import com.poupa.vinylmusicplayer.util.VinylMusicPlayerColorUtil;


public class MenuHelper {

    public static void decorateDestructiveItems(Menu menu, Context context) {
        // All delete element inside of menu should have red text to better differentiate them
        MenuItem liveItem = menu.findItem(R.id.action_delete_playlist);
        if (liveItem == null)
            liveItem = menu.findItem(R.id.action_delete_from_device);

        if (liveItem != null) {
            SpannableString s = new SpannableString(liveItem.getTitle().toString());

            // Get delete color from context's theme
            final TypedValue typedColorBackground = new TypedValue();
            context.getTheme().resolveAttribute(R.attr.md_delete, typedColorBackground, true);
            @ColorInt int color = typedColorBackground.data;

            s.setSpan(new ForegroundColorSpan(color), 0, s.length(), 0);
            liveItem.setTitle(s);
        }
    }

    public static MaterialCab setOverflowMenu(@NonNull AppCompatActivity context, @MenuRes int menuRes, @ColorInt int backgroundColor) {
        return new MaterialCab(context, R.id.cab_stub)
                .setMenu(menuRes)
                .setCloseDrawableRes(R.drawable.ic_close_white_24dp)
                .setBackgroundColor(VinylMusicPlayerColorUtil.shiftBackgroundColorForLightText(backgroundColor))
                .setPopupMenuTheme(PreferenceUtil.getInstance().getGeneralTheme());
    }
}
