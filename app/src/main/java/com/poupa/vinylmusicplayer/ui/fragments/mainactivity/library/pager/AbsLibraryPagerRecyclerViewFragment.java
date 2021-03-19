package com.poupa.vinylmusicplayer.ui.fragments.mainactivity.library.pager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.databinding.FragmentMainActivityRecyclerViewBinding;
import com.poupa.vinylmusicplayer.discog.Discography;
import com.poupa.vinylmusicplayer.helper.WeakMethodReference;
import com.poupa.vinylmusicplayer.util.ViewUtil;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public abstract class AbsLibraryPagerRecyclerViewFragment<A extends RecyclerView.Adapter, LM extends RecyclerView.LayoutManager> extends AbsLibraryPagerFragment implements OnOffsetChangedListener {
    View container;
    RecyclerView recyclerView;
    TextView empty;

    private A adapter;
    private LM layoutManager;

    private final WeakMethodReference<AbsLibraryPagerRecyclerViewFragment> onDiscographyChanged = new WeakMethodReference<>(this, AbsLibraryPagerRecyclerViewFragment::reload);

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentMainActivityRecyclerViewBinding binding = FragmentMainActivityRecyclerViewBinding.inflate(inflater, container, false);
        this.container = binding.container;
        recyclerView = binding.recyclerView;
        empty = binding.empty;

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getLibraryFragment().addOnAppBarOffsetChangedListener(this);
        Discography.getInstance().addChangedListener(onDiscographyChanged);

        initLayoutManager();
        initAdapter();
        setUpRecyclerView();
    }

    private void setUpRecyclerView() {
        if (recyclerView instanceof FastScrollRecyclerView) {
            ViewUtil.setUpFastScrollRecyclerViewColor(getActivity(), ((FastScrollRecyclerView) recyclerView), ThemeStore.accentColor(getActivity()));
        }
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    protected void invalidateLayoutManager() {
        initLayoutManager();
        recyclerView.setLayoutManager(layoutManager);
    }

    protected void invalidateAdapter() {
        initAdapter();
        checkIsEmpty();
        recyclerView.setAdapter(adapter);
    }

    private void initAdapter() {
        adapter = createAdapter();
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                checkIsEmpty();
            }
        });
    }

    private void initLayoutManager() {
        layoutManager = createLayoutManager();
    }

    protected A getAdapter() {
        return adapter;
    }

    protected LM getLayoutManager() {
        return layoutManager;
    }

    protected RecyclerView getRecyclerView() {
        return recyclerView;
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        container.setPadding(container.getPaddingLeft(), container.getPaddingTop(), container.getPaddingRight(), getLibraryFragment().getTotalAppBarScrollingRange() + i);
    }

    private void checkIsEmpty() {
        if (empty != null) {
            empty.setText(getEmptyMessage());
            empty.setVisibility(adapter == null || adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        }
    }

    @StringRes
    protected int getEmptyMessage() {
        return R.string.empty;
    }

    protected abstract LM createLayoutManager();

    @NonNull
    protected abstract A createAdapter();

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getLibraryFragment().removeOnAppBarOffsetChangedListener(this);
        Discography.getInstance().removeChangedListener(onDiscographyChanged);
    }

    @Override
    public void onMediaStoreChanged() {
        reload();
    }

    public abstract void reload();
}
