package com.poupa.vinylmusicplayer.adapter.base;

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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.helper.menu.MenuHelper;
import com.poupa.vinylmusicplayer.ui.activities.base.AbsThemeActivity;
import com.poupa.vinylmusicplayer.util.VinylMusicPlayerColorUtil;

@FunctionalInterface
public interface AbsMultiSelectActionModeHolder {
    @Nullable
    ActionMode startActionMode(final int menuRes, @NonNull final ActionMode.Callback callbacks);

    @Nullable
    static ActionMode startActionModeImpl(
            @NonNull final AppCompatActivity context,
            @MenuRes final int menuRes,
            @ColorInt final int backgroundColor,
            @NonNull final ActionMode.Callback callbacks)
    {
        return context.startActionMode(
                new ActionMode.Callback() {
                    @Override
                    public boolean onCreateActionMode(final ActionMode mode, final Menu menu) {
                        final MenuInflater inflater = mode.getMenuInflater();
                        inflater.inflate(menuRes, menu);
                        MenuHelper.decorateDestructiveItems(menu, context);

                        return callbacks.onCreateActionMode(mode, menu);
                    }

                    @Override
                    public boolean onPrepareActionMode(final ActionMode mode, final Menu menu) {
                        final int cabColor = VinylMusicPlayerColorUtil.shiftBackgroundColorForLightText(backgroundColor);

                        final ViewGroup decorView = (ViewGroup) context.getWindow().getDecorView();
                        final View view = decorView.findViewById(R.id.action_mode_bar);
                        if (view != null) {
                            view.setBackgroundColor(cabColor);
                        }

                        AbsThemeActivity.static_setStatusbarColor(context, cabColor);

                        callbacks.onPrepareActionMode(mode, menu);
                        return true;
                    }

                    @Override
                    public boolean onActionItemClicked(final ActionMode mode, final MenuItem item) {
                        return callbacks.onActionItemClicked(mode, item);
                    }

                    @Override
                    public void onDestroyActionMode(final ActionMode mode) {
                        AbsThemeActivity.static_setStatusbarColor(context, backgroundColor);

                        callbacks.onDestroyActionMode(mode);
                    }
                }
        );
    }

    static void update(@NonNull final Context context, @Nullable final ActionMode mode, final int checkedCount) {
        if (mode == null) {return;}

        if (checkedCount <= 0) {
            mode.finish();
        } else {
            mode.setTitle(context.getString(R.string.x_selected, checkedCount));
        }
    }
}
