package com.poupa.vinylmusicplayer.interfaces;

import android.content.Context;

import androidx.annotation.ColorInt;
import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.afollestad.materialcab.MaterialCabKt;
import com.afollestad.materialcab.attached.AttachedCab;
import com.afollestad.materialcab.attached.AttachedCabKt;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.helper.menu.MenuHelper;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;
import com.poupa.vinylmusicplayer.util.VinylMusicPlayerColorUtil;

import java.util.function.Supplier;

import kotlin.Unit;

@FunctionalInterface
public interface CabHolder {
    long ANIMATION_DELAY_MS = 500L;

    @NonNull
    AttachedCab openCab(final int menuRes, final CabCallbacks callbacks);

    @NonNull
    static AttachedCab openCabImpl(
            @NonNull final AppCompatActivity context,
            @MenuRes final int menuRes,
            @ColorInt final int backgroundColor,
            @NonNull final CabCallbacks callbacks)
    {
        final AttachedCab attachedCab = MaterialCabKt.createCab(
                context,
                R.id.cab_holder,
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
                    cab.slideDown(ANIMATION_DELAY_MS);

                    return Unit.INSTANCE;
                });

        MenuHelper.decorateDestructiveItems(attachedCab.getMenu(), context);

        return attachedCab;
    }

    @NonNull
    static AttachedCab updateCab(@NonNull Context context, @NonNull AttachedCab cab,
                                 @NonNull final Supplier<AttachedCab> openCabFunction,
                                 int checkedCount) {
        if (checkedCount <= 0) {
            AttachedCabKt.destroy(cab);
        }
        else {
            if (!AttachedCabKt.isActive(cab)) {
                cab = openCabFunction.get();
            }
            cab.title(ResourcesCompat.ID_NULL,
                    (checkedCount == 1)
                            ? context.getString(R.string.x_selected, 1)
                            : context.getString(R.string.x_selected, checkedCount)
            );
        }
        return cab;
    }
}
