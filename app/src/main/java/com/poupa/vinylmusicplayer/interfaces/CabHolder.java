package com.poupa.vinylmusicplayer.interfaces;

import android.content.Context;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.helper.menu.MenuHelper;
import com.poupa.vinylmusicplayer.util.VinylMusicPlayerColorUtil;

import java.util.function.Supplier;

@FunctionalInterface
public interface CabHolder {
    @NonNull
    ActionMode openCab(final int menuRes, @NonNull final CabCallbacks callbacks);

    @NonNull
    static ActionMode openCabImpl(
            @NonNull final AppCompatActivity context,
            @MenuRes final int menuRes,
            @ColorInt final int backgroundColor,
            @NonNull final CabCallbacks callbacks)
    {
        final ActionMode attachedCab = context.startActionMode(
                new ActionMode.Callback() {
                    @Override
                    public boolean onCreateActionMode(final ActionMode mode, final Menu menu) {
                        final MenuInflater inflater = mode.getMenuInflater();
                        inflater.inflate(menuRes, menu);
                        MenuHelper.decorateDestructiveItems(menu, context);

                        return true;
                    }

                    @Override
                    public boolean onPrepareActionMode(final ActionMode mode, final Menu menu) {
                        final ViewGroup decorView = (ViewGroup) context.getWindow().getDecorView();
                        decorView.postDelayed(() -> {
                            final View cabView = context.getWindow().getDecorView().findViewById(R.id.action_mode_bar);
                            final int cabColor = VinylMusicPlayerColorUtil.shiftBackgroundColorForLightText(backgroundColor);
                            if (cabView != null) {
                                cabView.setBackgroundColor(cabColor);
                            }
                        }, 10L);

                        callbacks.onCabCreate(mode, menu);
                        return false;
                    }

                    @Override
                    public boolean onActionItemClicked(final ActionMode mode, final MenuItem item) {
                        callbacks.onCabSelection(item);
                        return true;
                    }

                    @Override
                    public void onDestroyActionMode(final ActionMode mode) {
                        callbacks.onCabDestroy(mode);
                    }
                }
        );

        return attachedCab;
    }

    @NonNull
    static ActionMode updateCab(@NonNull final Context context, @NonNull ActionMode cab,
                                @NonNull final Supplier<ActionMode> openCabFunction,
                                final int checkedCount) {
        if (checkedCount <= 0) {
            cab.finish();
            return null;
        } else {
            if (cab == null) {
                cab = openCabFunction.get();
            }
            cab.setTitle(context.getString(R.string.x_selected, checkedCount));
            return cab;
        }
    }
}
