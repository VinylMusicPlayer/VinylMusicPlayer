package com.poupa.vinylmusicplayer.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.util.DialogUtils;
import com.kabouzeid.appthemehelper.util.ColorUtil;
import com.kabouzeid.appthemehelper.util.MaterialValueHelper;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.adapter.album.HorizontalAlbumAdapter;
import com.poupa.vinylmusicplayer.adapter.song.ArtistSongAdapter;
import com.poupa.vinylmusicplayer.databinding.ActivityArtistDetailBinding;
import com.poupa.vinylmusicplayer.databinding.SlidingMusicPanelLayoutBinding;
import com.poupa.vinylmusicplayer.dialogs.AddToPlaylistDialog;
import com.poupa.vinylmusicplayer.dialogs.MarkdownViewDialog;
import com.poupa.vinylmusicplayer.dialogs.SleepTimerDialog;
import com.poupa.vinylmusicplayer.glide.GlideApp;
import com.poupa.vinylmusicplayer.glide.VinylColoredTarget;
import com.poupa.vinylmusicplayer.glide.VinylGlideExtension;
import com.poupa.vinylmusicplayer.helper.MusicPlayerRemote;
import com.poupa.vinylmusicplayer.interfaces.LoaderIds;
import com.poupa.vinylmusicplayer.interfaces.PaletteColorHolder;
import com.poupa.vinylmusicplayer.lastfm.rest.LastFMRestClient;
import com.poupa.vinylmusicplayer.lastfm.rest.model.LastFmArtist;
import com.poupa.vinylmusicplayer.loader.ArtistLoader;
import com.poupa.vinylmusicplayer.misc.SimpleObservableScrollViewCallbacks;
import com.poupa.vinylmusicplayer.misc.WrappedAsyncTaskLoader;
import com.poupa.vinylmusicplayer.model.Artist;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.ui.activities.base.AbsSlidingMusicPanelActivity;
import com.poupa.vinylmusicplayer.util.CustomArtistImageUtil;
import com.poupa.vinylmusicplayer.util.ImageTheme.ThemeStyleUtil;
import com.poupa.vinylmusicplayer.util.MusicUtil;
import com.poupa.vinylmusicplayer.util.NavigationUtil;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;
import com.poupa.vinylmusicplayer.util.SafeToast;

import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ArtistDetailActivity
        extends AbsSlidingMusicPanelActivity
        implements PaletteColorHolder, LoaderManager.LoaderCallbacks<Artist> {

    private static final int LOADER_ID = LoaderIds.ARTIST_DETAIL_ACTIVITY;
    private static final int REQUEST_CODE_SELECT_IMAGE = 1000;

    public static final String EXTRA_ARTIST_ID = "extra_artist_id";

    ActivityArtistDetailBinding layoutBinding;

    private View songListHeader;
    private RecyclerView albumRecyclerView;

    int headerViewHeight;
    int toolbarColor;

    private Artist artist;
    @Nullable
    String biography;
    MarkdownViewDialog biographyDialog;
    HorizontalAlbumAdapter albumAdapter;
    private ArtistSongAdapter songAdapter;

    private LastFMRestClient lastFMRestClient;

    private boolean forceDownload;
    private final SimpleObservableScrollViewCallbacks observableScrollViewCallbacks = new SimpleObservableScrollViewCallbacks() {
        @Override
        public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
            final int y = scrollY + headerViewHeight;

            // Change alpha of overlay
            final float headerAlpha = Math.max(0, Math.min(1, (float) 2 * y / headerViewHeight));
            layoutBinding.headerOverlay.setBackgroundColor(ColorUtil.withAlpha(toolbarColor, headerAlpha));

            // Translate name text
            layoutBinding.header.setTranslationY(Math.max(-y, -headerViewHeight));
            layoutBinding.headerOverlay.setTranslationY(Math.max(-y, -headerViewHeight));
            layoutBinding.imageBorderTheme.setTranslationY(Math.max(-y, -headerViewHeight));
        }
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setDrawUnderStatusbar();

        lastFMRestClient = new LastFMRestClient(this);
        usePalette = PreferenceUtil.getInstance().albumArtistColoredFooters();

        initViews();
        setUpObservableListViewParams();
        setUpToolbar();
        setUpViews();

        LoaderManager.getInstance(this).initLoader(LOADER_ID, getIntent().getExtras(), this);
    }

    @Override
    protected View createContentView() {
        final SlidingMusicPanelLayoutBinding slidingPanelBinding = createSlidingMusicPanel();
        layoutBinding = ActivityArtistDetailBinding.inflate(
                getLayoutInflater(),
                slidingPanelBinding.contentContainer,
                true);

        return slidingPanelBinding.getRoot();
    }

    private boolean usePalette;

    private void setUpObservableListViewParams() {
        headerViewHeight = getResources().getDimensionPixelSize(R.dimen.detail_header_height);
    }

    private void initViews() {
        songListHeader = LayoutInflater.from(this).inflate(R.layout.artist_detail_header, layoutBinding.list, false);
        albumRecyclerView = songListHeader.findViewById(R.id.recycler_view);
    }

    private void setUpViews() {
        setUpSongListView();
        setUpAlbumRecyclerView();
        setColors(DialogUtils.resolveColor(this, R.attr.defaultFooterColor));
    }

    private void setUpSongListView() {
        setUpSongListPadding();
        layoutBinding.list.setScrollViewCallbacks(observableScrollViewCallbacks);
        layoutBinding.list.addHeaderView(songListHeader);

        songAdapter = new ArtistSongAdapter(this, getArtist().getSongs(), this);
        layoutBinding.list.setAdapter(songAdapter);

        final View contentView = getWindow().getDecorView().findViewById(android.R.id.content);
        contentView.post(() -> observableScrollViewCallbacks.onScrollChanged(-headerViewHeight, false, false));
    }

    private void setUpSongListPadding() {
        layoutBinding.list.setPadding(0, headerViewHeight, 0, 0);
    }

    private void setUpAlbumRecyclerView() {
        albumRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        albumAdapter = new HorizontalAlbumAdapter(this, getArtist().albums, usePalette, this);
        albumRecyclerView.setAdapter(albumAdapter);
        albumAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if (albumAdapter.getItemCount() == 0) {finish();}
            }
        });
    }

    private void setUsePalette(boolean usePalette) {
        albumAdapter.usePalette(usePalette);
        PreferenceUtil.getInstance().setAlbumArtistColoredFooters(usePalette);
        this.usePalette = usePalette;
    }

    @Override
    protected void reload() {
        LoaderManager.getInstance(this).restartLoader(LOADER_ID, getIntent().getExtras(), this);
    }

    private void loadBiography() {
        loadBiography(Locale.getDefault().getLanguage());
    }

    void loadBiography(@Nullable final String lang) {
        biography = null;

        lastFMRestClient.getApiService()
                .getArtistInfo(getArtist().getName(), lang, null)
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull final Call<LastFmArtist> call, @NonNull final Response<LastFmArtist> response) {
                        final LastFmArtist lastFmArtist = response.body();
                        if (lastFmArtist != null && lastFmArtist.getArtist() != null && lastFmArtist.getArtist().getBio() != null) {
                            final String bioContent = lastFmArtist.getArtist().getBio().getContent();
                            if (bioContent != null && !bioContent.trim().isEmpty()) {
                                biography = bioContent;
                            }
                        }

                        // If the "lang" parameter is set and no biography is given, retry with default language
                        if (biography == null && lang != null) {
                            loadBiography(null);
                            return;
                        }

                        if (!PreferenceUtil.isAllowedToDownloadMetadata(ArtistDetailActivity.this)) {
                            if (biography != null) {
                                biographyDialog.setMarkdownContent(ArtistDetailActivity.this, biography);
                            } else {
                                biographyDialog.dismiss();
                                SafeToast.show(ArtistDetailActivity.this, getResources().getString(R.string.biography_unavailable));
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull final Call<LastFmArtist> call, @NonNull final Throwable t) {
                        t.printStackTrace();
                        biography = null;
                    }
                });
    }

    private void loadArtistImage() {
        GlideApp.with(this)
                .asBitmapPalette()
                .load(VinylGlideExtension.getArtistModel(artist, forceDownload))
                .transition(VinylGlideExtension.getDefaultTransition())
                .artistOptions(artist)
                .dontAnimate()
                .into(new VinylColoredTarget(layoutBinding.image) {
                    @Override
                    public void onColorReady(int color) {
                        setColors(color);
                    }
                });
        forceDownload = false;
        layoutBinding.imageBorderTheme.setRadius(ThemeStyleUtil.getInstance().getAlbumRadiusImage(ArtistDetailActivity.this));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE) {
            if (resultCode == RESULT_OK) {
                CustomArtistImageUtil.getInstance(this)
                        .setCustomArtistImage(artist, data.getData(), this::loadArtistImage);
            }
        } else {
            if (resultCode == RESULT_OK) {
                reload();
            }
        }
    }

    @Override
    public int getPaletteColor() {
        return toolbarColor;
    }

    void setColors(int color) {
        toolbarColor = color;
        layoutBinding.header.setBackgroundColor(color);

        setNavigationbarColor(color);
        setTaskDescriptionColor(color);

        layoutBinding.toolbar.setBackgroundColor(color);
        setSupportActionBar(layoutBinding.toolbar); // needed to auto readjust the toolbar content color
        statusBarCollapsedColor = color; // needed to match the palette when the playing screen is collapsed
        setStatusbarColor(color);

        final int secondaryTextColor = MaterialValueHelper.getSecondaryTextColor(this, ColorUtil.isColorLight(color));
        layoutBinding.durationIcon.setColorFilter(secondaryTextColor, PorterDuff.Mode.SRC_IN);
        layoutBinding.songCountIcon.setColorFilter(secondaryTextColor, PorterDuff.Mode.SRC_IN);
        layoutBinding.albumCountIcon.setColorFilter(secondaryTextColor, PorterDuff.Mode.SRC_IN);
        layoutBinding.durationText.setTextColor(secondaryTextColor);
        layoutBinding.songCountText.setTextColor(secondaryTextColor);
        layoutBinding.albumCountText.setTextColor(secondaryTextColor);
        layoutBinding.title.setTextColor(MaterialValueHelper.getPrimaryTextColor(this, ColorUtil.isColorLight(color)));
    }

    private void setUpToolbar() {
        setSupportActionBar(layoutBinding.toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_artist_detail, menu);
        menu.findItem(R.id.action_colored_footers).setChecked(usePalette);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        final int id = item.getItemId();
        final ArrayList<Song> songs = songAdapter.getDataSet();
        if (id == R.id.action_sleep_timer) {
            new SleepTimerDialog().show(getSupportFragmentManager(), "SET_SLEEP_TIMER");
            return true;
        } else if (id == R.id.action_equalizer) {
            NavigationUtil.openEqualizer(this);
            return true;
        } else if (id == R.id.action_shuffle_artist) {
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
        } else if (id == android.R.id.home) {
            super.onBackPressed();
            return true;
        } else if (id == R.id.action_biography) {
            if (biographyDialog == null) {
                biographyDialog = new MarkdownViewDialog.Builder(this)
                        .title(artist.getName())
                        .build();
            }
            if (PreferenceUtil.isAllowedToDownloadMetadata(this)) { // wiki should've been already downloaded
                if (biography != null) {
                    biographyDialog.setMarkdownContent(this, biography);
                    biographyDialog.show();
                } else {
                    SafeToast.show(this, getResources().getString(R.string.biography_unavailable));
                }
            } else { // force download
                biographyDialog.show();
                loadBiography();
            }
            return true;
        } else if (id == R.id.action_set_artist_image) {
            final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, getString(R.string.pick_from_local_storage)), REQUEST_CODE_SELECT_IMAGE);
            return true;
        } else if (id == R.id.action_reset_artist_image) {
            SafeToast.show(ArtistDetailActivity.this, getResources().getString(R.string.updating));
            CustomArtistImageUtil.getInstance(ArtistDetailActivity.this)
                    .resetCustomArtistImage(artist, this::loadArtistImage);
            forceDownload = true;
            return true;
        } else if (id == R.id.action_colored_footers) {
            item.setChecked(!item.isChecked());
            setUsePalette(item.isChecked());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        albumRecyclerView.stopScroll();
        super.onBackPressed();
    }

    @Override
    public void setStatusbarColor(int color) {
        super.setStatusbarColor(color);
        setLightStatusbar(false);
    }

    private void setArtist(final Artist artist) {
        this.artist = artist;
        loadArtistImage();

        if (PreferenceUtil.isAllowedToDownloadMetadata(this)) {
            loadBiography();
        }

        layoutBinding.title.setText(artist.getName());
        layoutBinding.songCountText.setText(MusicUtil.getSongCountString(this, artist.getSongCount()));
        layoutBinding.albumCountText.setText(MusicUtil.getAlbumCountString(this, artist.getAlbumCount()));
        layoutBinding.durationText.setText(MusicUtil.getReadableDurationString(MusicUtil.getTotalDuration(artist.getSongs())));

        songAdapter.swapDataSet(artist.getSongs());
        albumAdapter.swapDataSet(artist.albums);
    }

    private Artist getArtist() {
        if (artist == null) {artist = Artist.EMPTY;}
        return artist;
    }

    @Override
    @NonNull
    public Loader<Artist> onCreateLoader(int id, @NonNull final Bundle args) {
        return new AsyncArtistDataLoader(this, args.getLong(EXTRA_ARTIST_ID));
    }

    @Override
    public void onPlayingMetaChanged() {
        super.onPlayingMetaChanged();

        // give the adapter a chance to update the decoration
        songAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPlayStateChanged() {
        super.onPlayStateChanged();

        // give the adapter a chance to update the decoration
        songAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoadFinished(@NonNull final Loader<Artist> loader, final Artist data) {
        setArtist(data);
    }

    @Override
    public void onLoaderReset(@NonNull final Loader<Artist> loader) {
        this.artist = Artist.EMPTY;
        songAdapter.swapDataSet(artist.getSongs());
        albumAdapter.swapDataSet(artist.albums);
    }

    private static class AsyncArtistDataLoader extends WrappedAsyncTaskLoader<Artist> {
        private final long artistId;

        AsyncArtistDataLoader(final Context context, long artistId) {
            super(context);
            this.artistId = artistId;
        }

        @Override
        public Artist loadInBackground() {
            return ArtistLoader.getArtist(artistId);
        }
    }
}
