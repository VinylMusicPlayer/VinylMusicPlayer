package com.poupa.vinylmusicplayer.interfaces;

import androidx.annotation.NonNull;

import com.afollestad.materialcab.attached.AttachedCab;

@FunctionalInterface
public interface CabHolder {
    long ANIMATION_DELAY_MS = 500L;

    @NonNull
    AttachedCab openCab(final int menuRes, final CabCallbacks callbacks);
}
