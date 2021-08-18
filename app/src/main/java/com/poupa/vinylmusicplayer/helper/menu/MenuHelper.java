package com.poupa.vinylmusicplayer.helper.menu;


import android.content.Context;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;

import androidx.core.content.ContextCompat;
import com.poupa.vinylmusicplayer.R;


public class MenuHelper {

    public static void setDeleteMenuItemRed(Menu menu, Context context) {
        // All delete element inside of menu should have red text to better differentiate them
        MenuItem liveItem = menu.findItem(R.id.action_delete_playlist);
        if (liveItem == null)
            liveItem = menu.findItem(R.id.action_delete_from_device);

        if (liveItem != null) {
            SpannableString s = new SpannableString(liveItem.getTitle().toString());
            int color = ContextCompat.getColor(context, R.color.delete);
            s.setSpan(new ForegroundColorSpan(color), 0, s.length(), 0);
            liveItem.setTitle(s);
        }
    }
}
