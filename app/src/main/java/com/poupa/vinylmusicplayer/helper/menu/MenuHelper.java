package com.poupa.vinylmusicplayer.helper.menu;

import android.content.Context;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import com.kabouzeid.appthemehelper.ThemeStore;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.helper.MusicPlayerRemote;

public class MenuHelper {
    public static void decorateDestructiveItems(@NonNull final Menu menu, final Context context) {
        // All destructive element inside of menu should have emphasis colored (ie. red) text to better differentiate them
        MenuItem liveItem = menu.findItem(R.id.action_delete_playlist);
        if (liveItem == null) {
            liveItem = menu.findItem(R.id.action_delete_from_device);
        }

        if (liveItem != null) {
            final SpannableString span = new SpannableString(liveItem.getTitle().toString());

            // TODO the primary color used for menu text/shuffle item is not chosen with
            //      consideration of dark/light theme (ie not visible on the theme background)
            @ColorInt int color = ThemeStore.accentColor(context);

            span.setSpan(new ForegroundColorSpan(color), 0, span.length(), 0);
            liveItem.setTitle(span);
        }
    }
}
