package com.poupa.vinylmusicplayer.adapter.base;

import android.content.Context;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.helper.menu.MenuHelper;
import com.poupa.vinylmusicplayer.interfaces.PaletteColorHolder;
import com.poupa.vinylmusicplayer.ui.activities.base.AbsThemeActivity;
import com.poupa.vinylmusicplayer.util.VinylMusicPlayerColorUtil;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public abstract class AbsMultiSelectAdapter<VH extends RecyclerView.ViewHolder, I>
        extends RecyclerView.Adapter<VH>
{
    @Nullable
    private final PaletteColorHolder palette;
    @Nullable
    private ActionMode actionMode;
    private final LinkedHashMap<Integer, I> checked;
    @MenuRes
    private int menuRes;
    @NonNull
    private final AbsThemeActivity activity;

    protected AbsMultiSelectAdapter(@NonNull final AbsThemeActivity activity, @Nullable final PaletteColorHolder holder, @MenuRes int menuRes) {
        palette = holder;
        checked = new LinkedHashMap<>();
        this.menuRes = menuRes;
        this.activity = activity;
    }

    protected void setMultiSelectMenuRes(@MenuRes final int menuRes) {
        this.menuRes = menuRes;
    }

    protected boolean toggleChecked(final int position) {
        final I identifier = getIdentifier(position);
        if (identifier == null) {return false;}

        if (checked.containsKey(position)) {checked.remove(position);}
        else {checked.put(position, identifier);}

        notifyItemChanged(position);
        startOrUpdateActionMode();

        return true;
    }

    private void checkAll() {
        checked.clear();
        final int itemCount = getItemCount();
        for (int i = 0; i < itemCount; i++) {
            final I identifier = getIdentifier(i);
            if (identifier != null) {
                checked.put(i, identifier);
            }
        }
        notifyDataSetChanged();
        startOrUpdateActionMode();
    }

    private void startOrUpdateActionMode() {
        if (actionMode == null) {
            if (palette != null) {
                @ColorInt final int color = palette.getPaletteColor();
                actionMode = ActionModeHelper.startActionMode(activity, menuRes, color, new ActionMode.Callback() {
                    @Override
                    public boolean onCreateActionMode(final ActionMode mode, final Menu menu) {
                        return true;
                    }

                    @Override
                    public boolean onPrepareActionMode(final ActionMode mode, final Menu menu) {
                        return false;
                    }

                    @Override
                    public boolean onActionItemClicked(final ActionMode mode, final MenuItem item) {
                        if (item.getItemId() == R.id.action_multi_select_adapter_check_all) {
                            checkAll();
                        } else {
                            onMultipleItemAction(item, checked);
                            mode.finish();
                            clearChecked();
                        }
                        return true;
                    }

                    @Override
                    public void onDestroyActionMode(final ActionMode mode) {
                        clearChecked();
                        actionMode = null;
                    }
                });
            }
        }
        ActionModeHelper.updateActionMode(activity, actionMode, checked.size());
    }

    private void clearChecked() {
        checked.clear();
        notifyDataSetChanged();
    }

    protected boolean isChecked(final int position) {
        return checked.containsKey(position);
    }

    protected boolean isInQuickSelectMode() {
        return actionMode != null;
    }

    @Nullable
    protected abstract I getIdentifier(int position);

    protected abstract void onMultipleItemAction(@NonNull final MenuItem menuItem, @NonNull final Map<Integer, I> selection);

    public final class ActionModeHelper {
        @Nullable
        public static ActionMode startActionMode(
                @NonNull final AbsThemeActivity activity,
                @MenuRes final int menuRes,
                @ColorInt final int backgroundColor,
                @NonNull final ActionMode.Callback callbacks)
        {
            return activity.startActionMode(
                    new ActionMode.Callback() {
                        @Override
                        public boolean onCreateActionMode(final ActionMode mode, final Menu menu) {
                            final MenuInflater inflater = mode.getMenuInflater();
                            inflater.inflate(menuRes, menu);
                            MenuHelper.decorateDestructiveItems(menu, activity);

                            return callbacks.onCreateActionMode(mode, menu);
                        }

                        @Override
                        public boolean onPrepareActionMode(final ActionMode mode, final Menu menu) {
                            final int adjustedColor = VinylMusicPlayerColorUtil.shiftBackgroundColorForLightText(backgroundColor);

                            final View view = activity.getWindow().getDecorView().findViewById(R.id.action_mode_bar);
                            if (view != null) {view.setBackgroundColor(adjustedColor);}

                            activity.setStatusbarColor(adjustedColor);

                            callbacks.onPrepareActionMode(mode, menu);
                            return true;
                        }

                        @Override
                        public boolean onActionItemClicked(final ActionMode mode, final MenuItem item) {
                            return callbacks.onActionItemClicked(mode, item);
                        }

                        @Override
                        public void onDestroyActionMode(final ActionMode mode) {
                            activity.setStatusbarColor(backgroundColor);

                            callbacks.onDestroyActionMode(mode);
                        }
                    }
            );
        }

        public static void updateActionMode(@NonNull final Context context, @Nullable final ActionMode mode, final int checkedCount) {
            if (mode == null) {return;}

            if (checkedCount <= 0) {
                mode.finish();
            } else {
                mode.setTitle(context.getString(R.string.x_selected, checkedCount));
            }
        }
    }
}
