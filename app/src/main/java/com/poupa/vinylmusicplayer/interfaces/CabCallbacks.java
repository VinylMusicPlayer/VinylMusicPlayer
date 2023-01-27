package com.poupa.vinylmusicplayer.interfaces;

import android.view.Menu;
import android.view.MenuItem;

import com.afollestad.materialcab.attached.AttachedCab;

public interface CabCallbacks {
    void onCabCreate(AttachedCab cab, Menu menu);

    boolean onCabDestroy(AttachedCab cab);

    boolean onCabSelection(MenuItem menuItem);
}
