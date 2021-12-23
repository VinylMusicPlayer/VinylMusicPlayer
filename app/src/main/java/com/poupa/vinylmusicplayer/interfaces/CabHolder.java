package com.poupa.vinylmusicplayer.interfaces;

import androidx.annotation.NonNull;

import com.afollestad.materialcab.attached.AttachedCab;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public interface CabHolder {
    @NonNull
    AttachedCab openCab(final int menuRes);
}
