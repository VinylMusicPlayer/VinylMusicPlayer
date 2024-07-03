package com.poupa.vinylmusicplayer.ui.fragments.mainactivity.library.pager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.adapter.PlaylistAdapter;
import com.poupa.vinylmusicplayer.interfaces.LoaderIds;
import com.poupa.vinylmusicplayer.misc.WrappedAsyncTaskLoader;
import com.poupa.vinylmusicplayer.model.Playlist;
import com.poupa.vinylmusicplayer.model.smartplaylist.HistoryPlaylist;
import com.poupa.vinylmusicplayer.model.smartplaylist.LastAddedPlaylist;
import com.poupa.vinylmusicplayer.model.smartplaylist.MyTopTracksPlaylist;
import com.poupa.vinylmusicplayer.model.smartplaylist.NotRecentlyPlayedPlaylist;
import com.poupa.vinylmusicplayer.provider.StaticPlaylist;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;

import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class PlaylistsFragment
        extends AbsLibraryPagerRecyclerViewFragment<PlaylistAdapter, LinearLayoutManager>
        implements LoaderManager.LoaderCallbacks<ArrayList<Playlist>>,
                SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int LOADER_ID = LoaderIds.PLAYLISTS_FRAGMENT;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceUtil.getInstance().registerOnSharedPreferenceChangedListener(this);

        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public void onDestroy() {
        PreferenceUtil.getInstance().unregisterOnSharedPreferenceChangedListener(this);
        super.onDestroy();
    }

    @NonNull
    @Override
    protected LinearLayoutManager createLayoutManager() {
        return new LinearLayoutManager(getActivity());
    }

    @NonNull
    @Override
    protected PlaylistAdapter createAdapter() {
        ArrayList<Playlist> dataSet = getAdapter() == null ? new ArrayList<>() : getAdapter().getDataSet();
        return new PlaylistAdapter(getLibraryFragment().getMainActivity(), dataSet, getLibraryFragment().getMainActivity());
    }

    @Override
    protected int getEmptyMessage() {
        return R.string.no_playlists;
    }

    @Override
    @NonNull
    public Loader<ArrayList<Playlist>> onCreateLoader(int id, Bundle args) {
        return new AsyncPlaylistLoader(getActivity());
    }

    @Override
    public void onLoadFinished(@NonNull Loader<ArrayList<Playlist>> loader, ArrayList<Playlist> data) {
        getAdapter().swapDataSet(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<ArrayList<Playlist>> loader) {
        getAdapter().swapDataSet(new ArrayList<>());
    }

    private static class AsyncPlaylistLoader extends WrappedAsyncTaskLoader<ArrayList<Playlist>> {
        public AsyncPlaylistLoader(Context context) {
            super(context);
        }

        private static ArrayList<Playlist> getAllPlaylists(Context context) {
            ArrayList<Playlist> playlists = new ArrayList<>();

            PreferenceUtil prefs = PreferenceUtil.getInstance();
            if (prefs.getLastAddedCutoffTimeSecs() > 0) {
                playlists.add(new LastAddedPlaylist(context));
            }
            if (prefs.getRecentlyPlayedCutoffTimeMillis() > 0) {
                playlists.add(new HistoryPlaylist(context));
            }
            if (prefs.getNotRecentlyPlayedCutoffTimeMillis() > 0) {
                playlists.add(new NotRecentlyPlayedPlaylist(context));
            }
            if (prefs.maintainTopTrackPlaylist()) {
                playlists.add(new MyTopTracksPlaylist(context));
            }

            for (StaticPlaylist playlist : StaticPlaylist.getAllPlaylists()) {
                playlists.add(playlist.asPlaylist());
            }
            return playlists;
        }

        @Override
        public ArrayList<Playlist> loadInBackground() {
            return getAllPlaylists(getContext());
        }
    }

    @Override
    public void onPlayingMetaChanged() {
        // Update the smart playlists (played, not played, etc)
        reload();
    }

    public void reload() {
        try {
            LoaderManager.getInstance(this).restartLoader(LOADER_ID, null, this);
        } catch (IllegalStateException ignored) {}
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (TextUtils.equals(key, PreferenceUtil.LAST_ADDED_CUTOFF_V2)
                || TextUtils.equals(key, PreferenceUtil.RECENTLY_PLAYED_CUTOFF_V2)
                || TextUtils.equals(key, PreferenceUtil.NOT_RECENTLY_PLAYED_CUTOFF_V2)
                || TextUtils.equals(key, PreferenceUtil.MAINTAIN_TOP_TRACKS_PLAYLIST)
        ) {
            // This event can be called when the fragment is is detached mode
            // In such situation, cannot call the reload (i.e. crash)
            //     E AndroidRuntime: java.lang.IllegalStateException: Can't access ViewModels from detached fragment
            //     E AndroidRuntime:        at androidx.fragment.app.Fragment.getViewModelStore(Unknown Source:32)
            //     E AndroidRuntime:        at androidx.loader.app.LoaderManager.getInstance(Unknown Source:5)
            // -> circumvent by delaying the reload
            new Handler().postDelayed(this::reload, 100);
        }
    }
}
