package com.poupa.vinylmusicplayer.ui.activities;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kabouzeid.appthemehelper.ThemeStore;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.adapter.SearchAdapter;
import com.poupa.vinylmusicplayer.databinding.ActivitySearchBinding;
import com.poupa.vinylmusicplayer.model.Album;
import com.poupa.vinylmusicplayer.model.Artist;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.util.ImageTheme.ThemeStyleUtil;
import com.poupa.vinylmusicplayer.util.StringUtil;
import com.poupa.vinylmusicplayer.interfaces.LoaderIds;
import com.poupa.vinylmusicplayer.loader.AlbumLoader;
import com.poupa.vinylmusicplayer.loader.ArtistLoader;
import com.poupa.vinylmusicplayer.loader.SongLoader;
import com.poupa.vinylmusicplayer.misc.WrappedAsyncTaskLoader;
import com.poupa.vinylmusicplayer.ui.activities.base.AbsMusicServiceActivity;
import com.poupa.vinylmusicplayer.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SearchActivity extends AbsMusicServiceActivity implements SearchView.OnQueryTextListener, LoaderManager.LoaderCallbacks<List<Object>> {

    public static final String QUERY = "query";
    private static final int LOADER_ID = LoaderIds.SEARCH_ACTIVITY;

    RecyclerView recyclerView;
    Toolbar toolbar;
    TextView empty;

    SearchView searchView;

    private SearchAdapter adapter;
    private String query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivitySearchBinding binding = ActivitySearchBinding.inflate(LayoutInflater.from(this));
        recyclerView = binding.recyclerView;
        toolbar = binding.toolbar;
        empty = binding.empty;
        setContentView(binding.getRoot());

        setDrawUnderStatusbar();

        applyThemeColors();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ThemeStyleUtil.getInstance().setHeaderPadding(recyclerView, getResources().getDisplayMetrics().density);

        //change scrollbar color to follow secondary color
        Drawable unwrappedDrawable = AppCompatResources.getDrawable(this, R.drawable.scrollbar_vertical_thumb);
        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, ThemeStore.accentColor(this));

        adapter = new SearchAdapter(this, Collections.emptyList());
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                empty.setVisibility(adapter.getItemCount() < 1 ? View.VISIBLE : View.GONE);
            }
        });
        recyclerView.setAdapter(adapter);

        recyclerView.setOnTouchListener((v, event) -> {
            hideSoftKeyboard();
            return false;
        });

        setUpToolBar();

        if (savedInstanceState != null) {
            query = savedInstanceState.getString(QUERY);
        }

        LoaderManager.getInstance(this).initLoader(LOADER_ID, null, this);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(QUERY, query);
    }

    private void setUpToolBar() {
        toolbar.setBackgroundColor(ThemeStore.primaryColor(this));
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        final MenuItem searchItem = menu.findItem(R.id.search);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getString(R.string.search_hint));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        searchItem.expandActionView();
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                onBackPressed();
                return false;
            }
        });

        searchView.setQuery(query, false);
        searchView.post(() -> searchView.setOnQueryTextListener(SearchActivity.this));

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void search(@NonNull String query) {
        this.query = query;
        LoaderManager.getInstance(this).restartLoader(LOADER_ID, null, this);
    }

    @Override
    public void onMediaStoreChanged() {
        super.onMediaStoreChanged();
        LoaderManager.getInstance(this).restartLoader(LOADER_ID, null, this);
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

    @Override
    public boolean onQueryTextSubmit(String query) {
        hideSoftKeyboard();
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        search(newText);
        return false;
    }

    private void hideSoftKeyboard() {
        Util.hideSoftKeyboard(SearchActivity.this);
        if (searchView != null) {
            searchView.clearFocus();
        }
    }

    @Override
    @NonNull
    public Loader<List<Object>> onCreateLoader(int id, Bundle args) {
        return new AsyncSearchResultLoader(this, query);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<Object>> loader, List<Object> data) {
        adapter.swapDataSet(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<Object>> loader) {
        adapter.swapDataSet(Collections.emptyList());
    }

    private static class AsyncSearchResultLoader extends WrappedAsyncTaskLoader<List<Object>> {
        private final String query;

        public AsyncSearchResultLoader(Context context, String query) {
            super(context);
            this.query = query;
        }

        @Override
        public List<Object> loadInBackground() {
            List<Object> results = new ArrayList<>();
            if (!TextUtils.isEmpty(query)) {
                final String normalizedQuery = StringUtil.unicodeNormalize(query.trim());

                List<Song> songs = SongLoader.getSongs(normalizedQuery);
                if (!songs.isEmpty()) {
                    results.add(getContext().getResources().getString(R.string.songs));
                    results.addAll(songs);
                }

                List<Artist> artists = ArtistLoader.getArtists(normalizedQuery);
                if (!artists.isEmpty()) {
                    results.add(getContext().getResources().getString(R.string.artists));
                    results.addAll(artists);
                }

                List<Album> albums = AlbumLoader.getAlbums(normalizedQuery);
                if (!albums.isEmpty()) {
                    results.add(getContext().getResources().getString(R.string.albums));
                    results.addAll(albums);
                }
            }
            return results;
        }
    }
}
