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
import com.poupa.vinylmusicplayer.interfaces.CabCallbacks;
import com.poupa.vinylmusicplayer.interfaces.CabHolder;
import com.poupa.vinylmusicplayer.ui.activities.base.AbsThemeActivity;
import com.poupa.vinylmusicplayer.util.VinylMusicPlayerColorUtil;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public abstract class AbsMultiSelectAdapter<VH extends RecyclerView.ViewHolder, I>
        extends RecyclerView.Adapter<VH>
        implements CabCallbacks {
    @Nullable
    private final CabHolder cabHolder;
    @Nullable
    private ActionMode cab;
    private final LinkedHashMap<Integer, I> checked;
    private int menuRes;
    private final Context context;

    private int color;

    protected AbsMultiSelectAdapter(final Context context, @Nullable final CabHolder cabHolder, @MenuRes int menuRes) {
        this.cabHolder = cabHolder;
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
        updateCab();

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
        updateCab();
    }

    private void updateCab() {
        if (cabHolder != null) {
            cab = CabHolder.updateCab(context, cab, () -> cabHolder.openCab(menuRes, this), checked.size());
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
        return cab != null;
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public void onCabCreate(@NonNull final ActionMode cab, @NonNull final Menu menu) {
        AbsThemeActivity.static_setStatusbarColor((Activity) context, VinylMusicPlayerColorUtil.shiftBackgroundColorForLightText(color));
    }

    @Override
    public boolean onCabSelection(@NonNull final MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.action_multi_select_adapter_check_all) {
            checkAll();
        } else {
            onMultipleItemAction(menuItem, checked);
            cab.finish();
            cab = null;
            clearChecked();
        }
        return true;
    }

    @Override
    public boolean onCabDestroy(@NonNull final ActionMode cab) {
        AbsThemeActivity.static_setStatusbarColor((Activity) context, color);
        clearChecked();
        return true;
    }

    @Nullable
    protected abstract I getIdentifier(int position);

    protected abstract void onMultipleItemAction(@NonNull final MenuItem menuItem, @NonNull final Map<Integer, I> selection);
}
