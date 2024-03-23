package com.poupa.vinylmusicplayer.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialcab.attached.AttachedCab;
import com.afollestad.materialcab.attached.AttachedCabKt;
import com.afollestad.materialdialogs.util.DialogUtils;
import com.kabouzeid.appthemehelper.util.ColorUtil;
import com.kabouzeid.appthemehelper.util.MaterialValueHelper;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.adapter.song.AlbumSongAdapter;
import com.poupa.vinylmusicplayer.databinding.ActivityAlbumDetailBinding;
import com.poupa.vinylmusicplayer.databinding.SlidingMusicPanelLayoutBinding;
import com.poupa.vinylmusicplayer.dialogs.AddToPlaylistDialog;
import com.poupa.vinylmusicplayer.dialogs.MarkdownViewDialog;
import com.poupa.vinylmusicplayer.dialogs.SleepTimerDialog;
import com.poupa.vinylmusicplayer.dialogs.helper.DeleteSongsHelper;
import com.poupa.vinylmusicplayer.discog.tagging.MultiValuesTagUtil;
import com.poupa.vinylmusicplayer.glide.GlideApp;
import com.poupa.vinylmusicplayer.glide.VinylColoredTarget;
import com.poupa.vinylmusicplayer.glide.VinylGlideExtension;
import com.poupa.vinylmusicplayer.helper.MusicPlayerRemote;
import com.poupa.vinylmusicplayer.helper.menu.MenuHelper;
import com.poupa.vinylmusicplayer.interfaces.CabCallbacks;
import com.poupa.vinylmusicplayer.interfaces.CabHolder;
import com.poupa.vinylmusicplayer.interfaces.LoaderIds;
import com.poupa.vinylmusicplayer.interfaces.PaletteColorHolder;
import com.poupa.vinylmusicplayer.lastfm.rest.LastFMRestClient;
import com.poupa.vinylmusicplayer.lastfm.rest.model.LastFmAlbum;
import com.poupa.vinylmusicplayer.loader.AlbumLoader;
import com.poupa.vinylmusicplayer.misc.SimpleObservableScrollViewCallbacks;
import com.poupa.vinylmusicplayer.misc.WrappedAsyncTaskLoader;
import com.poupa.vinylmusicplayer.model.Album;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.ui.activities.base.AbsSlidingMusicPanelActivity;
import com.poupa.vinylmusicplayer.ui.activities.tageditor.AbsTagEditorActivity;
import com.poupa.vinylmusicplayer.ui.activities.tageditor.AlbumTagEditorActivity;
import com.poupa.vinylmusicplayer.util.ImageTheme.ThemeStyleUtil;
import com.poupa.vinylmusicplayer.util.MusicUtil;
import com.poupa.vinylmusicplayer.util.NavigationUtil;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;
import com.poupa.vinylmusicplayer.util.SafeToast;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlbumDetailActivity
        extends AbsSlidingMusicPanelActivity
        implements PaletteColorHolder, CabHolder, LoaderManager.LoaderCallbacks<Album> {

    private static final int TAG_EDITOR_REQUEST = 2001;
    private static final int LOADER_ID = LoaderIds.ALBUM_DETAIL_ACTIVITY;

    public static final String EXTRA_ALBUM_ID = "extra_album_id";

    private Album album;

    ActivityAlbumDetailBinding layoutBinding;
    AlbumSongAdapter adapter;
    private AttachedCab cab;
    int headerViewHeight;
    int toolbarColor;

    @Nullable
    String wiki;
    MarkdownViewDialog wikiDialog;
    private LastFMRestClient lastFMRestClient;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setDrawUnderStatusbar();

        lastFMRestClient = new LastFMRestClient(this);

        setUpObservableListViewParams();
        setUpToolBar();
        setUpViews();

        LoaderManager.getInstance(this).initLoader(LOADER_ID, getIntent().getExtras(), this);
    }

    @Override
    protected View createContentView() {
        final SlidingMusicPanelLayoutBinding slidingPanelBinding = createSlidingMusicPanel();
        layoutBinding = ActivityAlbumDetailBinding.inflate(
                getLayoutInflater(),
                slidingPanelBinding.contentContainer,
                true);

        return slidingPanelBinding.getRoot();
    }

    private final SimpleObservableScrollViewCallbacks observableScrollViewCallbacks = new SimpleObservableScrollViewCallbacks() {
        @Override
        public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
            int y = scrollY + headerViewHeight;

            // Change alpha of overlay
            final float headerAlpha = Math.max(0, Math.min(1, (float) 2 * y / headerViewHeight));
            layoutBinding.headerOverlay.setBackgroundColor(ColorUtil.withAlpha(toolbarColor, headerAlpha));

            // Translate name text
            layoutBinding.header.setTranslationY(Math.max(-y, -headerViewHeight));
            layoutBinding.headerOverlay.setTranslationY(Math.max(-y, -headerViewHeight));
            layoutBinding.imageBorderTheme.setTranslationY(Math.max(-y, -headerViewHeight));
        }
    };

    private void setUpObservableListViewParams() {
        headerViewHeight = getResources().getDimensionPixelSize(R.dimen.detail_header_height);
    }

    private void setUpViews() {
        setUpRecyclerView();
        setUpSongsAdapter();
        layoutBinding.artistText.setOnClickListener(v -> {
            if (album != null) {
                NavigationUtil.goToArtist(this, album.getArtistNames());
            }
        });
        setColors(DialogUtils.resolveColor(this, R.attr.defaultFooterColor));
    }

    private void loadAlbumCover() {
        GlideApp.with(this)
                .asBitmapPalette()
                .load(VinylGlideExtension.getSongModel(getAlbum().safeGetFirstSong()))
                .transition(VinylGlideExtension.getDefaultTransition())
                .songOptions(getAlbum().safeGetFirstSong())
                .dontAnimate()
                .into(new VinylColoredTarget(layoutBinding.image) {
                    @Override
                    public void onColorReady(int color) {
                        setColors(color);
                    }
                });

        layoutBinding.imageBorderTheme.setRadius(ThemeStyleUtil.getInstance().getAlbumRadiusImage(AlbumDetailActivity.this));
    }

    void setColors(int color) {
        toolbarColor = color;
        layoutBinding.header.setBackgroundColor(color);

        setNavigationbarColor(color);
        setTaskDescriptionColor(color);

        layoutBinding.toolbar.setBackgroundColor(color);
        setSupportActionBar(layoutBinding.toolbar); // needed to auto readjust the toolbar content color
        setStatusbarColor(color);

        final int secondaryTextColor = MaterialValueHelper.getSecondaryTextColor(this, ColorUtil.isColorLight(color));
        layoutBinding.artistIcon.setColorFilter(secondaryTextColor, PorterDuff.Mode.SRC_IN);
        layoutBinding.durationIcon.setColorFilter(secondaryTextColor, PorterDuff.Mode.SRC_IN);
        layoutBinding.songCountIcon.setColorFilter(secondaryTextColor, PorterDuff.Mode.SRC_IN);
        layoutBinding.albumYearIcon.setColorFilter(secondaryTextColor, PorterDuff.Mode.SRC_IN);
        layoutBinding.artistText.setTextColor(MaterialValueHelper.getPrimaryTextColor(this, ColorUtil.isColorLight(color)));
        layoutBinding.durationText.setTextColor(secondaryTextColor);
        layoutBinding.songCountText.setTextColor(secondaryTextColor);
        layoutBinding.albumYearText.setTextColor(secondaryTextColor);
        layoutBinding.title.setTextColor(MaterialValueHelper.getPrimaryTextColor(this, ColorUtil.isColorLight(color)));
    }

    @Override
    public int getPaletteColor() {
        return toolbarColor;
    }

    private void setUpRecyclerView() {
        setUpRecyclerViewPadding();
        layoutBinding.list.setScrollViewCallbacks(observableScrollViewCallbacks);
        final View contentView = getWindow().getDecorView().findViewById(android.R.id.content);
        contentView.post(() -> observableScrollViewCallbacks.onScrollChanged(-headerViewHeight, false, false));
    }

    private void setUpRecyclerViewPadding() {
        layoutBinding.list.setPadding(0, headerViewHeight, 0, 0);
    }

    private void setUpToolBar() {
        setSupportActionBar(layoutBinding.toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setUpSongsAdapter() {
        adapter = new AlbumSongAdapter(this, getAlbum().songs, false, this);
        layoutBinding.list.setLayoutManager(new GridLayoutManager(this, 1));
        layoutBinding.list.setAdapter(adapter);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if (adapter.getItemCount() == 0) {finish();}
            }
        });
    }

    @Override
    protected void reload() {
        LoaderManager.getInstance(this).restartLoader(LOADER_ID, getIntent().getExtras(), this);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_album_detail, menu);

        MenuHelper.decorateDestructiveItems(menu, this);

        return super.onCreateOptionsMenu(menu);
    }

    private void loadWiki() {
        loadWiki(Locale.getDefault().getLanguage());
    }

    void loadWiki(@Nullable final String lang) {
        wiki = null;

        final List<String> artistNames = getAlbum().getArtistNames();
        final String artistName = artistNames.isEmpty() ? "" : artistNames.get(0);
        lastFMRestClient.getApiService()
                .getAlbumInfo(getAlbum().getTitle(), artistName, lang)
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<LastFmAlbum> call, @NonNull Response<LastFmAlbum> response) {
                        final LastFmAlbum lastFmAlbum = response.body();
                        if (lastFmAlbum != null && lastFmAlbum.getAlbum() != null && lastFmAlbum.getAlbum().getWiki() != null) {
                            final String wikiContent = lastFmAlbum.getAlbum().getWiki().getContent();
                            if (wikiContent != null && !wikiContent.trim().isEmpty()) {
                                wiki = wikiContent;
                            }
                        }

                        // If the "lang" parameter is set and no wiki is given, retry with default language
                        if (wiki == null && lang != null) {
                            loadWiki(null);
                            return;
                        }

                        if (!PreferenceUtil.isAllowedToDownloadMetadata(AlbumDetailActivity.this)) {
                            if (wiki != null) {
                                wikiDialog.setMarkdownContent(AlbumDetailActivity.this, wiki);
                            } else {
                                wikiDialog.dismiss();
                                SafeToast.show(AlbumDetailActivity.this, getResources().getString(R.string.wiki_unavailable));
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<LastFmAlbum> call, @NonNull Throwable t) {
                        t.printStackTrace();
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        final int id = item.getItemId();
        final List<? extends Song> songs = adapter.getDataSet();
        if (id == R.id.action_sleep_timer) {
            new SleepTimerDialog().show(getSupportFragmentManager(), "SET_SLEEP_TIMER");
            return true;
        } else if (id == R.id.action_equalizer) {
            NavigationUtil.openEqualizer(this);
            return true;
        } else if (id == R.id.action_shuffle_album) {
            MusicPlayerRemote.openAndShuffleQueue(songs, true);
            return true;
        } else if (id == R.id.action_play_next) {
            MusicPlayerRemote.playNext(songs);
            return true;
        } else if (id == R.id.action_add_to_current_playing) {
            MusicPlayerRemote.enqueue(songs);
            return true;
        } else if (id == R.id.action_add_to_playlist) {
            AddToPlaylistDialog.create(songs).show(getSupportFragmentManager(), "ADD_PLAYLIST");
            return true;
        } else if (id == R.id.action_delete_from_device) {
            DeleteSongsHelper.delete(songs, getSupportFragmentManager(), "DELETE_SONGS");
            return true;
        } else if (id == android.R.id.home) {
            super.onBackPressed();
            return true;
        } else if (id == R.id.action_tag_editor) {
            final Intent intent = new Intent(this, AlbumTagEditorActivity.class);
            intent.putExtra(AbsTagEditorActivity.EXTRA_ID, getAlbum().getId());
            startActivityForResult(intent, TAG_EDITOR_REQUEST);
            return true;
        } else if (id == R.id.action_go_to_artist) {
            NavigationUtil.goToArtist(this, getAlbum().getArtistNames());
            return true;
        } else if (id == R.id.action_wiki) {
            if (wikiDialog == null) {
                wikiDialog = new MarkdownViewDialog.Builder(this)
                        .title(album.getTitle())
                        .build();
            }
            if (PreferenceUtil.isAllowedToDownloadMetadata(this)) {
                if (wiki != null) {
                    wikiDialog.setMarkdownContent(this, wiki);
                    wikiDialog.show();
                } else {
                    SafeToast.show(this, getResources().getString(R.string.wiki_unavailable));
                }
            } else {
                wikiDialog.show();
                loadWiki();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TAG_EDITOR_REQUEST) {
            reload();
            setResult(RESULT_OK);
        }
    }

    @NonNull
    @Override
    public AttachedCab openCab(int menuRes, @NonNull final CabCallbacks callbacks) {
        AttachedCabKt.destroy(cab);

        @ColorInt final int color = getPaletteColor();
        adapter.setColor(color);
        cab = CabHolder.openCabImpl(this, menuRes, color, callbacks);
        return cab;
    }

    @Override
    public void onBackPressed() {
        if (cab != null && AttachedCabKt.isActive(cab)) {AttachedCabKt.destroy(cab);}
        else {
            layoutBinding.list.stopScroll();
            super.onBackPressed();
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

    @Override
    public void setStatusbarColor(int color) {
        super.setStatusbarColor(color);
        setLightStatusbar(false);
    }

    private void setAlbum(final Album album) {
        this.album = album;
        loadAlbumCover();

        if (PreferenceUtil.isAllowedToDownloadMetadata(this)) {
            loadWiki();
        }

        final List<String> artistNames = album.getArtistNames();
        final String artistName = artistNames.isEmpty()
                ? getResources().getString(R.string.no_artists)
                : MultiValuesTagUtil.infoStringAsArtists(artistNames);

        layoutBinding.title.setText(album.getTitle());
        layoutBinding.artistText.setText(artistName);
        layoutBinding.songCountText.setText(MusicUtil.getSongCountString(this, album.getSongCount()));
        layoutBinding.durationText.setText(MusicUtil.getReadableDurationString(MusicUtil.getTotalDuration(album.songs)));
        layoutBinding.albumYearText.setText(MusicUtil.getYearString(album.getYear()));

        adapter.swapDataSet(album.songs);
    }

    private Album getAlbum() {
        if (album == null) {album = new Album();}
        return album;
    }

    @Override
    @NonNull
    public Loader<Album> onCreateLoader(int id, @Nullable final Bundle args) {
        return new AsyncAlbumLoader(this, args.getLong(EXTRA_ALBUM_ID));
    }

    @Override
    public void onLoadFinished(@NonNull final Loader<Album> loader, final Album data) {
        setAlbum(data);
    }

    @Override
    public void onLoaderReset(@NonNull final Loader<Album> loader) {
        this.album = new Album();
        adapter.swapDataSet(album.songs);
    }

    private static class AsyncAlbumLoader extends WrappedAsyncTaskLoader<Album> {
        private final long albumId;

        AsyncAlbumLoader(final Context context, long albumId) {
            super(context);
            this.albumId = albumId;
        }

        @NonNull
        @Override
        public Album loadInBackground() {
            return AlbumLoader.getAlbum(albumId);
        }
    }
}
