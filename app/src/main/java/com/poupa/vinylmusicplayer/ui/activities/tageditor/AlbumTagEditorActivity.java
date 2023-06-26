package com.poupa.vinylmusicplayer.ui.activities.tageditor;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;

import com.bumptech.glide.GenericTransitionOptions;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.transition.Transition;
import com.kabouzeid.appthemehelper.util.ATHUtil;
import com.kabouzeid.appthemehelper.util.ToolbarContentTintHelper;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.databinding.ActivityAlbumTagEditorBinding;
import com.poupa.vinylmusicplayer.glide.GlideApp;
import com.poupa.vinylmusicplayer.glide.VinylSimpleTarget;
import com.poupa.vinylmusicplayer.glide.palette.BitmapPaletteWrapper;
import com.poupa.vinylmusicplayer.lastfm.rest.LastFMRestClient;
import com.poupa.vinylmusicplayer.lastfm.rest.model.LastFmAlbum;
import com.poupa.vinylmusicplayer.loader.AlbumLoader;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.util.AutoDeleteAudioFile;
import com.poupa.vinylmusicplayer.util.ImageUtil;
import com.poupa.vinylmusicplayer.util.LastFMUtil;
import com.poupa.vinylmusicplayer.util.OopsHandler;
import com.poupa.vinylmusicplayer.util.VinylMusicPlayerColorUtil;

import org.jaudiotagger.tag.FieldKey;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlbumTagEditorActivity extends AbsTagEditorActivity implements TextWatcher {

    EditText albumTitle;
    EditText albumArtist;
    EditText genre;
    EditText year;

    private Bitmap albumArtBitmap;
    private boolean deleteAlbumArt;
    private LastFMRestClient lastFMRestClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        lastFMRestClient = new LastFMRestClient(this);
    }

    @Override
    protected void setUpViews() {
        super.setUpViews();

        fillViewsWithFileTags();
        albumTitle.addTextChangedListener(this);
        albumArtist.addTextChangedListener(this);
        genre.addTextChangedListener(this);
        year.addTextChangedListener(this);

        // Dont wrap text if line too long, make it scrollable
        // https://stackoverflow.com/questions/5146207/disable-word-wrap-in-an-android-multi-line-textview
        albumArtist.setHorizontallyScrolling(true);
    }


    private void fillViewsWithFileTags() {
        try (AutoDeleteAudioFile audio = getAudioFile()) {
            if (audio != null) {
                albumTitle.setText(getAlbumTitle(audio.get()));
                albumArtist.setText(getAlbumArtistName(audio.get()));
                genre.setText(getGenreName(audio.get()));
                year.setText(getSongYear(audio.get()));
            }
        } catch (Exception e) {
            OopsHandler.copyStackTraceToClipboard(this, e);
        }
    }

    @Override
    protected void loadCurrentImage() {
        try (AutoDeleteAudioFile audio = getAudioFile()) {
            Bitmap bitmap = getAlbumArt(audio.get());
            setImageBitmap(bitmap, VinylMusicPlayerColorUtil.getColor(VinylMusicPlayerColorUtil.generatePalette(bitmap), ATHUtil.resolveColor(this, R.attr.defaultFooterColor)));
            deleteAlbumArt = false;
        } catch (Exception e) {
            OopsHandler.copyStackTraceToClipboard(this, e);
        }
    }

    @Override
    protected void getImageFromLastFM() {
        String albumTitleStr = albumTitle.getText().toString();
        String albumArtistNameStr = albumArtist.getText().toString();
        if (albumArtistNameStr.trim().equals("") || albumTitleStr.trim().equals("")) {
            Toast.makeText(this, getResources().getString(R.string.album_or_artist_empty), Toast.LENGTH_SHORT).show();
            return;
        }
        lastFMRestClient.getApiService().getAlbumInfo(albumTitleStr, albumArtistNameStr, null).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<LastFmAlbum> call, @NonNull Response<LastFmAlbum> response) {
                LastFmAlbum lastFmAlbum = response.body();
                if (lastFmAlbum != null && lastFmAlbum.getAlbum() != null) {
                    String url = LastFMUtil.getLargestAlbumImageUrl(lastFmAlbum.getAlbum().getImage());
                    if (!TextUtils.isEmpty(url) && url.trim().length() > 0) {
                        GlideApp.with(AlbumTagEditorActivity.this)
                                .as(BitmapPaletteWrapper.class)
                                .load(url)
                                .apply(new RequestOptions()
                                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                        .error(R.drawable.default_album_art))
                                .transition(new GenericTransitionOptions<BitmapPaletteWrapper>().transition(android.R.anim.fade_in))
                                .into(new VinylSimpleTarget<BitmapPaletteWrapper>() {
                                    @Override
                                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                        super.onLoadFailed(errorDrawable);
                                    }

                                    @Override
                                    public void onResourceReady(@NonNull BitmapPaletteWrapper resource, Transition<? super BitmapPaletteWrapper> glideAnimation) {
                                        albumArtBitmap = ImageUtil.resizeBitmap(resource.getBitmap(), 2048);
                                        setImageBitmap(albumArtBitmap, VinylMusicPlayerColorUtil.getColor(resource.getPalette(), ATHUtil.resolveColor(AlbumTagEditorActivity.this, R.attr.defaultFooterColor)));
                                        deleteAlbumArt = false;
                                        dataChanged();
                                        setResult(RESULT_OK);
                                    }
                                });
                        return;
                    }
                }
                toastLoadingFailed();
            }

            @Override
            public void onFailure(@NonNull Call<LastFmAlbum> call, @NonNull Throwable t) {
                toastLoadingFailed();
            }

            private void toastLoadingFailed() {
                Toast.makeText(AlbumTagEditorActivity.this,
                        R.string.could_not_download_album_cover, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void searchImageOnWeb() {
        searchWebFor(albumTitle.getText().toString(), albumArtist.getText().toString());
    }

    @Override
    protected void deleteImage() {
        setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.default_album_art), ATHUtil.resolveColor(this, R.attr.defaultFooterColor));
        deleteAlbumArt = true;
        dataChanged();
    }

    @Override
    protected void save() {
        Map<FieldKey, String> fieldKeyValueMap = new EnumMap<>(FieldKey.class);
        fieldKeyValueMap.put(FieldKey.ALBUM, albumTitle.getText().toString());
        fieldKeyValueMap.put(FieldKey.ALBUM_ARTIST, albumArtist.getText().toString());
        fieldKeyValueMap.put(FieldKey.GENRE, genre.getText().toString());
        fieldKeyValueMap.put(FieldKey.YEAR, year.getText().toString());

        writeValuesToFiles(fieldKeyValueMap,
                deleteAlbumArt
                        ? new ArtworkInfo(getId(), null)
                        : albumArtBitmap == null
                                ? null
                                : new ArtworkInfo(getId(), albumArtBitmap));
    }

    @Override
    @NonNull
    protected ViewBinding getViewBinding() {
        ActivityAlbumTagEditorBinding binding = ActivityAlbumTagEditorBinding.inflate(LayoutInflater.from(this));

        albumTitle = binding.title;
        albumArtist = binding.albumArtist;
        genre = binding.genre;
        year = binding.year;

        fab = binding.playPauseFab;
        observableScrollView = binding.observableScrollView;
        toolbar = binding.toolbar;
        image = binding.image;
        header = binding.header;

        return binding;
    }

    @NonNull
    @Override
    protected List<Song> getSongs() {
        return AlbumLoader.getAlbum(getId()).songs;
    }

    @Override
    protected void loadImageFromFile(@NonNull final Uri selectedFileUri) {
        GlideApp.with(AlbumTagEditorActivity.this)
                .as(BitmapPaletteWrapper.class)
                .load(selectedFileUri)
                .transition(new GenericTransitionOptions<BitmapPaletteWrapper>().transition(android.R.anim.fade_in))
                .apply(new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true))
                .into(new VinylSimpleTarget<BitmapPaletteWrapper>() {
                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                    }

                    @Override
                    public void onResourceReady(@NonNull BitmapPaletteWrapper resource, Transition<? super BitmapPaletteWrapper> glideAnimation) {
                        VinylMusicPlayerColorUtil.getColor(resource.getPalette(), Color.TRANSPARENT);
                        albumArtBitmap = ImageUtil.resizeBitmap(resource.getBitmap(), 2048);
                        setImageBitmap(albumArtBitmap, VinylMusicPlayerColorUtil.getColor(resource.getPalette(), ATHUtil.resolveColor(AlbumTagEditorActivity.this, R.attr.defaultFooterColor)));
                        deleteAlbumArt = false;
                        dataChanged();
                        setResult(RESULT_OK);
                    }
                });
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        dataChanged();
    }

    @Override
    protected void setColors(int color) {
        super.setColors(color);
        albumTitle.setTextColor(ToolbarContentTintHelper.toolbarTitleColor(this, color));
    }
}
