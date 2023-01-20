package com.poupa.vinylmusicplayer.helper.menu;

import android.content.Context;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.ColorInt;
import androidx.annotation.IdRes;
import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.afollestad.materialcab.MaterialCabKt;
import com.afollestad.materialcab.attached.AttachedCab;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.interfaces.CabCallbacks;
import com.poupa.vinylmusicplayer.interfaces.CabHolder;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;
import com.poupa.vinylmusicplayer.util.VinylMusicPlayerColorUtil;

import kotlin.Unit;

public class MenuHelper {
    public static void decorateDestructiveItems(@NonNull final Menu menu, final Context context) {
        // All delete element inside of menu should have emphasis colored (ie. red) text to better differentiate them
        MenuItem liveItem = menu.findItem(R.id.action_delete_playlist);
        if (liveItem == null) {
            liveItem = menu.findItem(R.id.action_delete_from_device);
        }

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

    @NonNull
    public static AttachedCab createAndOpenCab(
            @NonNull final AppCompatActivity context,
            @IdRes final int cabRes,
            @MenuRes final int menuRes,
            @ColorInt final int backgroundColor,
            @NonNull final CabCallbacks callbacks)
    {
        final AttachedCab attachedCab = MaterialCabKt.createCab(
                context,
                cabRes,
                cab -> {
                    cab.menu(menuRes);
                    cab.closeDrawable(R.drawable.ic_close_white_24dp);
                    cab.backgroundColor(
                            ResourcesCompat.ID_NULL,
                            VinylMusicPlayerColorUtil.shiftBackgroundColorForLightText(backgroundColor));
                    cab.popupTheme(PreferenceUtil.getInstance().getGeneralTheme());

                    cab.onCreate((attachedCab1, menu) -> {
                        callbacks.onCabCreate(attachedCab1, menu);
                        return Unit.INSTANCE;
                    });
                    cab.onDestroy(callbacks::onCabDestroy);
                    cab.onSelection(callbacks::onCabSelection);
                    cab.slideDown(CabHolder.ANIMATION_DELAY_MS);

                    return Unit.INSTANCE;
                });

        decorateDestructiveItems(attachedCab.getMenu(), context);

        return attachedCab;
    }
}
