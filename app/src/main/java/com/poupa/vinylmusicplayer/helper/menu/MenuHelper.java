package com.poupa.vinylmusicplayer.helper.menu;

import android.content.Context;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import com.poupa.vinylmusicplayer.R;

public class MenuHelper {
    public static void decorateDestructiveItems(@NonNull final Menu menu, final Context context) {
        // All delete element inside of menu should have emphasis colored (ie. red) text to better differentiate them
        MenuItem liveItem = menu.findItem(R.id.action_delete_playlist);
        if (liveItem == null) {
            liveItem = menu.findItem(R.id.action_delete_from_device);
        }

        if (liveItem != null) {
            final SpannableString span = new SpannableString(liveItem.getTitle().toString());

            @ColorInt int color = ThemeStore.accentColor(context);

            span.setSpan(new ForegroundColorSpan(color), 0, span.length(), 0);
            liveItem.setTitle(span);
        }
    }
}
