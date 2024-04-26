package com.poupa.vinylmusicplayer.preferences;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.adapter.CategoryInfoAdapter;
import com.poupa.vinylmusicplayer.model.CategoryInfo;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;

import java.util.ArrayList;


public class LibraryPreferenceDialog extends DialogFragment {
    public static LibraryPreferenceDialog newInstance() {
        return new LibraryPreferenceDialog();
    }

    private CategoryInfoAdapter adapter;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.preference_dialog_library_categories, null);

        ArrayList<CategoryInfo> categoryInfos;
        if (savedInstanceState != null) {
            categoryInfos = savedInstanceState.getParcelableArrayList(PreferenceUtil.LIBRARY_CATEGORIES);
        } else {
            categoryInfos = PreferenceUtil.getInstance().getLibraryCategoryInfos();
        }
        adapter = new CategoryInfoAdapter(categoryInfos);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        adapter.attachToRecyclerView(recyclerView);

        return new AlertDialog.Builder(getContext())
                .setTitle(R.string.library_categories)
                .setView(view)
                .setPositiveButton(android.R.string.ok, (dialog, action) -> {
                    updateCategories(adapter.getCategoryInfos());
                    dismiss();
                })
                .setNegativeButton(android.R.string.cancel, (dialog, action) -> dismiss())
                .setNeutralButton(R.string.reset_action, (dialog, action) -> adapter.setCategoryInfos(PreferenceUtil.getInstance().getDefaultLibraryCategoryInfos()))
                .setCancelable(false)
                .create();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(PreferenceUtil.LIBRARY_CATEGORIES, adapter.getCategoryInfos());
    }

    private void updateCategories(ArrayList<CategoryInfo> categories) {
        if (getSelected(categories) == 0) return;

        PreferenceUtil.getInstance().setLibraryCategoryInfos(categories);
    }

    private int getSelected(ArrayList<CategoryInfo> categories) {
        int selected = 0;
        for (CategoryInfo categoryInfo : categories) {
            if (categoryInfo.visible)
                selected++;
        }
        return selected;
    }
}
