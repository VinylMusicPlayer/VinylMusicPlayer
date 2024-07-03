package com.poupa.vinylmusicplayer.ui.fragments.mainactivity.library.pager;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.GridLayoutManager;

import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.adapter.artist.ArtistAdapter;
import com.poupa.vinylmusicplayer.interfaces.LoaderIds;
import com.poupa.vinylmusicplayer.loader.ArtistLoader;
import com.poupa.vinylmusicplayer.misc.WrappedAsyncTaskLoader;
import com.poupa.vinylmusicplayer.model.Artist;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;

import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ArtistsFragment extends AbsLibraryPagerRecyclerViewCustomGridSizeFragment<ArtistAdapter, GridLayoutManager> implements LoaderManager.LoaderCallbacks<ArrayList<Artist>> {

    private static final int LOADER_ID = LoaderIds.ARTISTS_FRAGMENT;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LoaderManager.getInstance(this).initLoader(LOADER_ID, null, this);
    }

    @NonNull
    @Override
    protected GridLayoutManager createLayoutManager() {
        return new GridLayoutManager(getActivity(), getGridSize());
    }

    @NonNull
    @Override
    protected ArtistAdapter createAdapter() {
        int itemLayoutRes = getItemLayoutRes();
        notifyLayoutResChanged(itemLayoutRes);
        ArrayList<Artist> dataSet = getAdapter() == null ? new ArrayList<>() : getAdapter().getDataSet();
        return new ArtistAdapter(
                getLibraryFragment().getMainActivity(),
                dataSet,
                itemLayoutRes,
                loadUsePalette(),
                getLibraryFragment().getMainActivity());
    }

    @Override
    protected int getEmptyMessage() {
        return R.string.no_artists;
    }

    @Override
    protected String loadSortOrder() {
        return PreferenceUtil.getInstance().getArtistSortOrder();
    }

    @Override
    protected void saveSortOrder(String sortOrder) {
        PreferenceUtil.getInstance().setArtistSortOrder(sortOrder);
    }

    @Override
    protected int loadGridSize() {
        return PreferenceUtil.getInstance().getArtistGridSize(requireActivity());
    }

    @Override
    protected void saveGridSize(int gridSize) {
        PreferenceUtil.getInstance().setArtistGridSize(gridSize);
    }

    @Override
    protected int loadGridSizeLand() {
        return PreferenceUtil.getInstance().getArtistGridSizeLand(requireActivity());
    }

    @Override
    protected void saveGridSizeLand(int gridSize) {
        PreferenceUtil.getInstance().setArtistGridSizeLand(gridSize);
    }

    @Override
    protected void saveShowFooter(boolean showFooter) {
        // Stub
    }

    @Override
    public boolean loadShowFooter() {
        return true;
    }

    @Override
    protected void setShowFooter(boolean showFooter) {
        getAdapter().showFooter(showFooter);
    }

    @Override
    protected void saveUsePalette(boolean usePalette) {
        PreferenceUtil.getInstance().setArtistColoredFooters(usePalette);
    }

    @Override
    public boolean loadUsePalette() {
        return PreferenceUtil.getInstance().artistColoredFooters();
    }

    @Override
    protected void setUsePalette(boolean usePalette) {
        getAdapter().usePalette(usePalette);
    }

    @Override
    protected void setGridSize(int gridSize) {
        getLayoutManager().setSpanCount(gridSize);
        getAdapter().notifyDataSetChanged();
    }

    @Override
    @NonNull
    public Loader<ArrayList<Artist>> onCreateLoader(int id, Bundle args) {
        return new AsyncArtistLoader(getActivity());
    }

    @Override
    public void onLoadFinished(@NonNull Loader<ArrayList<Artist>> loader, ArrayList<Artist> data) {
        getAdapter().swapDataSet(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<ArrayList<Artist>> loader) {
        getAdapter().swapDataSet(new ArrayList<>());
    }

    private static class AsyncArtistLoader extends WrappedAsyncTaskLoader<ArrayList<Artist>> {
        public AsyncArtistLoader(Context context) {
            super(context);
        }

        @Override
        public ArrayList<Artist> loadInBackground() {
            return ArtistLoader.getAllArtists();
        }
    }

    @Override
    public void reload() {
        try {
            LoaderManager.getInstance(this).restartLoader(LOADER_ID, null, this);
        } catch (IllegalStateException ignored) {}
    }
}
