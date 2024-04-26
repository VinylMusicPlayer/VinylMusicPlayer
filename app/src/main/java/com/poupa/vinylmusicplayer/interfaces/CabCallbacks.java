package com.poupa.vinylmusicplayer.interfaces;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;

public interface CabCallbacks {
    void onCabCreate(@NonNull final ActionMode cab, @NonNull final Menu menu);

    boolean onCabDestroy(@NonNull final ActionMode cab);

    boolean onCabSelection(@NonNull final MenuItem menuItem);
}
