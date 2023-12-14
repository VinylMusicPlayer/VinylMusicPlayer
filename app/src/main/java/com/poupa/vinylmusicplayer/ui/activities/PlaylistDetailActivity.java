package com.poupa.vinylmusicplayer.ui.activities;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialcab.attached.AttachedCab;
import com.afollestad.materialcab.attached.AttachedCabKt;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.util.ColorUtil;
import com.kabouzeid.appthemehelper.util.MaterialValueHelper;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.adapter.song.OrderablePlaylistSongAdapter;
import com.poupa.vinylmusicplayer.adapter.song.PlaylistSongAdapter;
import com.poupa.vinylmusicplayer.adapter.song.SongAdapter;
import com.poupa.vinylmusicplayer.databinding.ActivityPlaylistDetailBinding;
import com.poupa.vinylmusicplayer.databinding.SlidingMusicPanelLayoutBinding;
import com.poupa.vinylmusicplayer.helper.MusicPlayerRemote;
import com.poupa.vinylmusicplayer.helper.menu.MenuHelper;
import com.poupa.vinylmusicplayer.helper.menu.PlaylistMenuHelper;
import com.poupa.vinylmusicplayer.interfaces.CabCallbacks;
import com.poupa.vinylmusicplayer.interfaces.CabHolder;
import com.poupa.vinylmusicplayer.interfaces.LoaderIds;
import com.poupa.vinylmusicplayer.misc.WrappedAsyncTaskLoader;
import com.poupa.vinylmusicplayer.model.AbsCustomPlaylist;
import com.poupa.vinylmusicplayer.model.Playlist;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.model.smartplaylist.NotRecentlyPlayedPlaylist;
import com.poupa.vinylmusicplayer.provider.StaticPlaylist;
import com.poupa.vinylmusicplayer.ui.activities.base.AbsSlidingMusicPanelActivity;
import com.poupa.vinylmusicplayer.util.PlaylistsUtil;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;
import com.poupa.vinylmusicplayer.util.ViewUtil;

import java.util.ArrayList;

public class PlaylistDetailActivity
        extends AbsSlidingMusicPanelActivity
        implements
            CabHolder,
            LoaderManager.LoaderCallbacks<ArrayList<Song>>
{

    private static final int LOADER_ID = LoaderIds.PLAYLIST_DETAIL_ACTIVITY;

    @NonNull
    public static final String EXTRA_PLAYLIST = "extra_playlist";

    private ActivityPlaylistDetailBinding layoutBinding;

    private Playlist playlist;

    private AttachedCab cab;
    private SongAdapter adapter;

    private RecyclerView.Adapter wrappedAdapter;
    private RecyclerViewDragDropManager recyclerViewDragDropManager;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setDrawUnderStatusbar();

        setStatusbarColorAuto();
        setNavigationbarColorAuto();
        setTaskDescriptionColorAuto();

        playlist = getIntent().getExtras().getParcelable(EXTRA_PLAYLIST);

        setUpRecyclerView();

        setUpToolbar();

        LoaderManager.getInstance(this).initLoader(LOADER_ID, null, this);
    }

    @Override
    protected View createContentView() {
        final SlidingMusicPanelLayoutBinding slidingPanelBinding = createSlidingMusicPanel();
        layoutBinding = ActivityPlaylistDetailBinding.inflate(
                getLayoutInflater(),
                slidingPanelBinding.contentContainer,
                true);

        return slidingPanelBinding.getRoot();
    }

    private void setUpRecyclerView() {
        ViewUtil.setUpFastScrollRecyclerViewColor(
                this,
                layoutBinding.recyclerView,
                ThemeStore.accentColor(this));
        layoutBinding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        if (playlist instanceof AbsCustomPlaylist) {
            adapter = new PlaylistSongAdapter(this, new ArrayList<>(), false, this);
            layoutBinding.recyclerView.setAdapter(adapter);
        } else {
            recyclerViewDragDropManager = new RecyclerViewDragDropManager();
            final GeneralItemAnimator animator = new RefactoredDefaultItemAnimator();
            adapter = new OrderablePlaylistSongAdapter(
                    this,
                    playlist.id,
                    new ArrayList<>(),
                    false,
                    this,
                    (fromPosition, toPosition) -> {
                        if (PlaylistsUtil.moveItem(playlist.id, fromPosition, toPosition)) {
                            Song song = adapter.getDataSet().remove(fromPosition);
                            adapter.getDataSet().add(toPosition, song);
                            adapter.notifyItemMoved(fromPosition, toPosition);
                        }
                    });
            wrappedAdapter = recyclerViewDragDropManager.createWrappedAdapter(adapter);

            layoutBinding.recyclerView.setAdapter(wrappedAdapter);
            layoutBinding.recyclerView.setItemAnimator(animator);

            recyclerViewDragDropManager.attachRecyclerView(layoutBinding.recyclerView);
        }

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                checkIsEmpty();
            }
        });
    }

    private void setUpToolbar() {
        layoutBinding.toolbar.setBackgroundColor(ThemeStore.primaryColor(this));
        setSupportActionBar(layoutBinding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setToolbarTitle(null);
        layoutBinding.title.setText(playlist.name);
        layoutBinding.title.setTextColor(MaterialValueHelper.getPrimaryTextColor(this, ColorUtil.isColorLight(ThemeStore.primaryColor(this))));
    }

    private void setToolbarTitle(final String title) {
        getSupportActionBar().setTitle(title);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(playlist instanceof AbsCustomPlaylist ? R.menu.menu_smart_playlist_detail : R.menu.menu_playlist_detail, menu);

        // "Group by album" option
        if (playlist instanceof NotRecentlyPlayedPlaylist) {
            final MenuItem item = menu.add(Menu.NONE, R.id.action_song_sort_group_by_album, Menu.NONE, R.string.sort_order_group_by_album);
            item.setCheckable(true).setEnabled(true)
                    .setChecked(PreferenceUtil.getInstance().getNotRecentlyPlayedSortOrder().equals(PreferenceUtil.ALBUM_SORT_ORDER));
        }

        MenuHelper.decorateDestructiveItems(menu, this);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        final int id = item.getItemId();
        if (id == R.id.action_shuffle_playlist) {
            MusicPlayerRemote.openAndShuffleQueue(adapter.getDataSet(), true);
            return true;
        } else if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_song_sort_group_by_album) {
            item.setChecked(!item.isChecked()); // toggle
            PreferenceUtil.getInstance().setNotRecentlyPlayedSortOrder(item.isChecked() ? PreferenceUtil.ALBUM_SORT_ORDER : PreferenceUtil.SONG_SORT_ORDER);
            reload();
        }
        return PlaylistMenuHelper.handleMenuClick(this, playlist, item);
    }

    @NonNull
    @Override
    public AttachedCab openCab(final int menu, final CabCallbacks callbacks) {
        AttachedCabKt.destroy(cab);

        @ColorInt final int color = ThemeStore.primaryColor(this);
        adapter.setColor(color);
        cab = CabHolder.openCabImpl(this, menu, color, callbacks);
        return cab;
    }

    @Override
    public void onBackPressed() {
        if (cab != null && AttachedCabKt.isActive(cab)) {AttachedCabKt.destroy(cab);}
        else {
            layoutBinding.recyclerView.stopScroll();
            super.onBackPressed();
        }
    }

    @Override
    public void onMediaStoreChanged() {
        super.onMediaStoreChanged();

        if (!(playlist instanceof AbsCustomPlaylist)) {
            final StaticPlaylist existingPlaylist = StaticPlaylist.getPlaylist(playlist.id);

            // Playlist deleted
            if (existingPlaylist == null) {
                finish();
                return;
            }

            // Playlist renamed
            if (!TextUtils.equals(existingPlaylist.getName(), playlist.name)) {
                playlist = existingPlaylist.asPlaylist();
                setToolbarTitle(playlist.name);
            }

            // Playlist changed (song added/removed)
            reload();
        }
    }

    @Override
    public void onPlayingMetaChanged() {
        super.onPlayingMetaChanged();

        // give the adapter a chance to update the decoration
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onPlayStateChanged() {
        super.onPlayStateChanged();

        // give the adapter a chance to update the decoration
        adapter.notifyDataSetChanged();
    }

    void checkIsEmpty() {
        layoutBinding.empty.setVisibility(
                adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE
        );
    }

    @Override
    public void onPause() {
        if (recyclerViewDragDropManager != null) {
            recyclerViewDragDropManager.cancelDrag();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (recyclerViewDragDropManager != null) {
            recyclerViewDragDropManager.release();
            recyclerViewDragDropManager = null;
        }

        layoutBinding.recyclerView.setItemAnimator(null);
        layoutBinding.recyclerView.setAdapter(null);

        if (wrappedAdapter != null) {
            WrapperAdapterUtils.releaseAll(wrappedAdapter);
            wrappedAdapter = null;
        }
        adapter = null;

        super.onDestroy();
    }

    @Override
    @NonNull
    public Loader<ArrayList<Song>> onCreateLoader(int id, final Bundle args) {
        return new AsyncPlaylistSongLoader(this, playlist);
    }

    @Override
    public void onLoadFinished(@NonNull final Loader<ArrayList<Song>> loader, final ArrayList<Song> data) {
        if (adapter != null) {
            adapter.swapDataSet(data);
        }
    }

    @Override
    public void onLoaderReset(@NonNull final Loader<ArrayList<Song>> loader) {
        if (adapter != null) {
            adapter.swapDataSet(new ArrayList<>());
        }
    }

    @Override
    protected void reload() {
        LoaderManager.getInstance(this).restartLoader(LOADER_ID, null, this);
    }

    private static class AsyncPlaylistSongLoader extends WrappedAsyncTaskLoader<ArrayList<Song>> {
        private final Playlist playlist;

        AsyncPlaylistSongLoader(final Context context, final Playlist playlist) {
            super(context);
            this.playlist = playlist;
        }

        @NonNull
        @Override
        public ArrayList<Song> loadInBackground() {
            return playlist.getSongs(getContext());
        }
    }
}
