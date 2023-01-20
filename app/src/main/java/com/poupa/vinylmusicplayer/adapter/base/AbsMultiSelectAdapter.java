package com.poupa.vinylmusicplayer.adapter.base;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialcab.attached.AttachedCab;
import com.afollestad.materialcab.attached.AttachedCabKt;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.interfaces.CabCallbacks;
import com.poupa.vinylmusicplayer.interfaces.CabHolder;
import com.poupa.vinylmusicplayer.ui.activities.base.AbsThemeActivity;
import com.poupa.vinylmusicplayer.util.VinylMusicPlayerColorUtil;

import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public abstract class AbsMultiSelectAdapter<VH extends RecyclerView.ViewHolder, I>
        extends RecyclerView.Adapter<VH>
        implements CabCallbacks {
    @Nullable
    private final CabHolder cabHolder;
    private AttachedCab cab;
    private final ArrayList<I> checked;
    private int menuRes;
    private final Context context;

    private int color;

    protected AbsMultiSelectAdapter(final Context context, @Nullable final CabHolder cabHolder, @MenuRes int menuRes) {
        this.cabHolder = cabHolder;
        checked = new ArrayList<>();
        this.menuRes = menuRes;
        this.context = context;
    }

    protected void setMultiSelectMenuRes(@MenuRes final int menuRes) {
        this.menuRes = menuRes;
    }

    protected boolean toggleChecked(final int position) {
        final I identifier = getIdentifier(position);
        if (identifier == null) {return false;}

        if (!checked.remove(identifier)) {checked.add(identifier);}

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
                checked.add(identifier);
            }
        }
        notifyDataSetChanged();
        updateCab();
    }

    private void updateCab() {
        if (cabHolder != null) {
            if (cab == null || !AttachedCabKt.isActive(cab)) {
                cab = cabHolder.openCab(menuRes, this);
            }
            final int size = checked.size();
            if (size <= 0) {AttachedCabKt.destroy(cab);}
            else if (size == 1) {
                String name = getName(checked.get(0));
                if (TextUtils.isEmpty(name)) {name = context.getString(R.string.x_selected, 1);}
                cab.title(ResourcesCompat.ID_NULL, name);
            }
            else {cab.title(ResourcesCompat.ID_NULL, context.getString(R.string.x_selected, size));}
        }
    }

    private void clearChecked() {
        checked.clear();
        notifyDataSetChanged();
    }

    protected boolean isChecked(final I identifier) {
        return checked.contains(identifier);
    }

    protected boolean isInQuickSelectMode() {
        return cab != null && AttachedCabKt.isActive(cab);
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public void onCabCreate(final AttachedCab cab, final Menu menu) {
        AbsThemeActivity.static_setStatusbarColor((Activity) context, VinylMusicPlayerColorUtil.shiftBackgroundColorForLightText(color));
    }

    @Override
    public boolean onCabSelection(@NonNull final MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.action_multi_select_adapter_check_all) {
            checkAll();
        } else {
            onMultipleItemAction(menuItem, new ArrayList<>(checked));
            AttachedCabKt.destroy(cab);
            clearChecked();
        }
        return true;
    }

    @Override
    public boolean onCabDestroy(final AttachedCab cab) {
        AbsThemeActivity.static_setStatusbarColor((Activity) context, color);
        clearChecked();
        return true;
    }

    protected String getName(@NonNull final I object) {
        return object.toString();
    }

    @Nullable
    protected abstract I getIdentifier(int position);

    protected abstract void onMultipleItemAction(MenuItem menuItem, ArrayList<I> selection);
}
