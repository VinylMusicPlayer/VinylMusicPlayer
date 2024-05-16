package com.poupa.vinylmusicplayer.adapter.base;

import android.app.Activity;
import android.content.Context;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.poupa.vinylmusicplayer.R;
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
    private final AbsMultiSelectActionModeHolder actionModeHolder;
    @Nullable
    private ActionMode actionMode;
    private final LinkedHashMap<Integer, I> checked;
    private int menuRes;
    private final Context context;

    private int color;

    protected AbsMultiSelectAdapter(final Context context, @Nullable final AbsMultiSelectActionModeHolder actionModeHolder, @MenuRes int menuRes) {
        this.actionModeHolder = actionModeHolder;
        checked = new LinkedHashMap<>();
        this.menuRes = menuRes;
        this.context = context;
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
        updateMultiSelectActionMode();

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
        updateMultiSelectActionMode();
    }

    private void updateMultiSelectActionMode() {
        if (actionModeHolder != null) {
            if (actionMode == null) {
                actionMode = actionModeHolder.startActionMode(menuRes, new ActionMode.Callback() {
                    @Override
                    public boolean onCreateActionMode(final ActionMode mode, final Menu menu) {
                        return true;
                    }

                    @Override
                    public boolean onPrepareActionMode(final ActionMode mode, final Menu menu) {
                        AbsThemeActivity.static_setStatusbarColor(
                                (Activity) context,
                                VinylMusicPlayerColorUtil.shiftBackgroundColorForLightText(color)
                        );
                        return true;
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
                        AbsThemeActivity.static_setStatusbarColor((Activity) context, color);
                        clearChecked();
                        actionMode = null;
                    }
                });
            }
            AbsMultiSelectActionModeHolder.update(context, actionMode, checked.size());
        }
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

    public void setColor(int color) {
        this.color = color;
    }

    @Nullable
    protected abstract I getIdentifier(int position);

    protected abstract void onMultipleItemAction(@NonNull final MenuItem menuItem, @NonNull final Map<Integer, I> selection);
}
