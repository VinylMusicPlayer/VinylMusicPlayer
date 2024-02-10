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

import java.util.List;

public class MenuHelper {
    public static void decorateDestructiveItems(@NonNull final Menu menu, final Context context) {
        // All delete element inside of menu should have emphasis colored (ie. red) text to better differentiate them
        List<Integer> destructiveItems = List.of(
                R.id.action_delete_playlist,
                R.id.action_delete_from_device,
                R.id.action_clear_playlist
        );
        for (int itemId : destructiveItems) {
            MenuItem liveItem = menu.findItem(itemId);
            if (liveItem != null) {
                final SpannableString span = new SpannableString(liveItem.getTitle().toString());

                // Get delete color from context's theme
                final TypedValue typedColorBackground = new TypedValue();
                context.getTheme().resolveAttribute(R.attr.md_delete, typedColorBackground, true);
                @ColorInt int color = typedColorBackground.data;

                span.setSpan(new ForegroundColorSpan(color), 0, span.length(), 0);
                liveItem.setTitle(span);
            }
        }
    }
}
