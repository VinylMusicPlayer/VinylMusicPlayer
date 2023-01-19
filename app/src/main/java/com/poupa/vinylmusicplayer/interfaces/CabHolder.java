package com.poupa.vinylmusicplayer.interfaces;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.afollestad.materialcab.attached.AttachedCab;

@FunctionalInterface
public interface CabHolder {
    long ANIMATION_DELAY_MS = 500L;
    @ColorRes int UNDEFINED_COLOR_RES = 0;
    @StringRes int UNDEFINED_STRING_RES = 0;

    @NonNull
    AttachedCab openCab(final int menuRes, final CabCallbacks callbacks);
}
