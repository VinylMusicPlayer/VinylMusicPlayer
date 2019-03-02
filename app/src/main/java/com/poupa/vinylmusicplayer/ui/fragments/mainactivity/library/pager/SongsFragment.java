package com.poupa.vinylmusicplayer.ui.fragments.mainactivity.library.pager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.RemoteException;
import android.service.media.MediaBrowserService;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.widget.GridLayoutManager;

import com.poupa.vinylmusicplayer.BuildConfig;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.adapter.song.ShuffleButtonSongAdapter;
import com.poupa.vinylmusicplayer.adapter.song.SongAdapter;
import com.poupa.vinylmusicplayer.auto.AutoMusicBrowserService;
import com.poupa.vinylmusicplayer.interfaces.LoaderIds;
import com.poupa.vinylmusicplayer.loader.SongLoader;
import com.poupa.vinylmusicplayer.misc.WrappedAsyncTaskLoader;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class SongsFragment extends AbsLibraryPagerRecyclerViewCustomGridSizeFragment<SongAdapter, GridLayoutManager> implements LoaderManager.LoaderCallbacks<ArrayList<Song>> {

    private static final int LOADER_ID = LoaderIds.SONGS_FRAGMENT;

    private MediaBrowserCompat mMediaBrowser;

    private MediaBrowserCompat.ConnectionCallback mConnectionCallbacks = new MediaBrowserCompat.ConnectionCallback() {
        @Override
        public void onConnected() {
            try {
                // Ah, here’s our Token again
                MediaSessionCompat.Token token =
                        mMediaBrowser.getSessionToken();
                // This is what gives us access to everything
                MediaControllerCompat controller =
                        new MediaControllerCompat(getContext(), token);

                // Convenience method to allow you to use
                // MediaControllerCompat.getMediaController() anywhere
                MediaControllerCompat.setMediaController(
                        getActivity(), controller);

                /*SongLoader.getSongs(mMediaBrowser).observe(getActivity(), songs -> {
                    //adapter.submitList(songs);
                    getAdapter().notifyDataSetChanged();
                });*/
            } catch (RemoteException e) {
                e.printStackTrace();
                int i = 0;
            }
        }

        @Override
        public void onConnectionSuspended() {
            // We were connected, but no longer :-(
            int i = 0;
        }

        @Override
        public void onConnectionFailed() {
            // The attempt to connect failed completely.
            // Check the ComponentName!
            int i = 0;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // The usual setContentView, etc
        // Now create the MediaBrowserCompat
        /*mMediaBrowser = new MediaBrowserCompat(
                getContext(), // a Context
                new ComponentName(getActivity(), AutoMusicBrowserService.class),
                // Which MediaBrowserService
                ,
                null); // optional Bundle
        mMediaBrowser.connect();

        mMediaBrowser = new MediaBrowserCompat(getContext(),
                new ComponentName(BuildConfig.APPLICATION_ID,
                        AutoMusicBrowserService.class.getName()), mConnectionCallbacks, null);
        */
        mMediaBrowser = new MediaBrowserCompat(getContext(),
                new ComponentName(BuildConfig.APPLICATION_ID,
                        AutoMusicBrowserService.class.getName()), mConnectionCallbacks, null);
        mMediaBrowser.connect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMediaBrowser.disconnect();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(LOADER_ID, null, this);
        SongLoader.getSongs(mMediaBrowser).observe(this, songs -> {
            //adapter.submitList(songs);
            getAdapter().notifyDataSetChanged();
        });
    }

    @NonNull
    @Override
    protected GridLayoutManager createLayoutManager() {
        return new GridLayoutManager(getActivity(), getGridSize());
    }

    @NonNull
    @Override
    protected SongAdapter createAdapter() {
        int itemLayoutRes = getItemLayoutRes();
        notifyLayoutResChanged(itemLayoutRes);
        boolean usePalette = loadUsePalette();
        ArrayList<Song> dataSet = getAdapter() == null ? new ArrayList<>() : getAdapter().getDataSet();

        if (getGridSize() <= getMaxGridSizeForList()) {
            return new ShuffleButtonSongAdapter(
                    getLibraryFragment().getMainActivity(),
                    dataSet,
                    itemLayoutRes,
                    usePalette,
                    getLibraryFragment());
        }
        return new SongAdapter(
                getLibraryFragment().getMainActivity(),
                dataSet,
                itemLayoutRes,
                usePalette,
                getLibraryFragment());
    }

    @Override
    protected int getEmptyMessage() {
        return R.string.no_songs;
    }

    @Override
    public void onMediaStoreChanged() {
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    protected String loadSortOrder() {
        return PreferenceUtil.getInstance().getSongSortOrder();
    }

    @Override
    protected void saveSortOrder(String sortOrder) {
        PreferenceUtil.getInstance().setSongSortOrder(sortOrder);
    }

    @Override
    protected void setSortOrder(String sortOrder) {
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    protected int loadGridSize() {
        return PreferenceUtil.getInstance().getSongGridSize(getActivity());
    }

    @Override
    protected void saveGridSize(int gridSize) {
        PreferenceUtil.getInstance().setSongGridSize(gridSize);
    }

    @Override
    protected int loadGridSizeLand() {
        return PreferenceUtil.getInstance().getSongGridSizeLand(getActivity());
    }

    @Override
    protected void saveGridSizeLand(int gridSize) {
        PreferenceUtil.getInstance().setSongGridSizeLand(gridSize);
    }

    @Override
    public void saveUsePalette(boolean usePalette) {
        PreferenceUtil.getInstance().setSongColoredFooters(usePalette);
    }

    @Override
    public boolean loadUsePalette() {
        return PreferenceUtil.getInstance().songColoredFooters();
    }

    @Override
    public void setUsePalette(boolean usePalette) {
        getAdapter().usePalette(usePalette);
    }

    @Override
    protected void setGridSize(int gridSize) {
        getLayoutManager().setSpanCount(gridSize);
        getAdapter().notifyDataSetChanged();
    }

    @Override
    public Loader<ArrayList<Song>> onCreateLoader(int id, Bundle args) {
        /*SongLoader.getSongs(mMediaBrowser).observe(this, songs -> {
            //adapter.submitList(songs);
            getAdapter().notifyDataSetChanged();
        });*/

        return new AsyncSongLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<Song>> loader, ArrayList<Song> data) {
        getAdapter().swapDataSet(data);
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<Song>> loader) {
        getAdapter().swapDataSet(new ArrayList<>());
    }

    private static class AsyncSongLoader extends WrappedAsyncTaskLoader<ArrayList<Song>> {
        public AsyncSongLoader(Context context) {
            super(context);
        }

        @Override
        public ArrayList<Song> loadInBackground() {
            return SongLoader.getAllSongs(getContext());
        }
    }
}
