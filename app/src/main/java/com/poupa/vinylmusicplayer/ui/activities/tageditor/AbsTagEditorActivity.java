package com.poupa.vinylmusicplayer.ui.activities.tageditor;

import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.viewbinding.ViewBinding;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.util.ColorUtil;
import com.kabouzeid.appthemehelper.util.TintHelper;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.discog.Discography;
import com.poupa.vinylmusicplayer.discog.tagging.MultiValuesTagUtil;
import com.poupa.vinylmusicplayer.misc.DialogAsyncTask;
import com.poupa.vinylmusicplayer.misc.SimpleObservableScrollViewCallbacks;
import com.poupa.vinylmusicplayer.misc.UpdateToastMediaScannerCompletionListener;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.ui.activities.base.AbsBaseActivity;
import com.poupa.vinylmusicplayer.ui.activities.saf.SAFGuideActivity;
import com.poupa.vinylmusicplayer.util.AutoCloseAudioFile;
import com.poupa.vinylmusicplayer.util.AutoDeleteTempFile;
import com.poupa.vinylmusicplayer.util.OopsHandler;
import com.poupa.vinylmusicplayer.util.SAFUtil;
import com.poupa.vinylmusicplayer.util.SafeToast;
import com.poupa.vinylmusicplayer.util.Util;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.images.Artwork;
import org.jaudiotagger.tag.images.ArtworkFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public abstract class AbsTagEditorActivity extends AbsBaseActivity {

    public static final String EXTRA_ID = "extra_id";
    public static final String EXTRA_PALETTE = "extra_palette";

    FloatingActionButton fab;
    ObservableScrollView observableScrollView;
    Toolbar toolbar;
    ImageView image;
    LinearLayout header;

    private long id;
    int headerVariableSpace;
    int paletteColorPrimary;
    boolean isInNoImageMode;
    private final SimpleObservableScrollViewCallbacks observableScrollViewCallbacks = new SimpleObservableScrollViewCallbacks() {
        @Override
        public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
            float alpha;
            if (!isInNoImageMode) {
                alpha = 1 - (float) Math.max(0, headerVariableSpace - scrollY) / headerVariableSpace;
            } else {
                header.setTranslationY(scrollY);
                alpha = 1;
            }
            toolbar.setBackgroundColor(ColorUtil.withAlpha(paletteColorPrimary, alpha));
            image.setTranslationY(scrollY / 2.0f);
        }
    };
    private List<Song> songs;
    private List<Song> savedSongs;
    private Song currentSong;
    private Map<FieldKey, String> savedTags;
    private ArtworkInfo savedArtworkInfo;

    private ActivityResultLauncher<String> writeTagsApi19_SAFFilePicker;
    private ActivityResultLauncher<Intent> writeTagsApi21_SAFGuide;

    private ActivityResultLauncher<Uri> writeTagsApi21_SAFTreePicker;
    private ActivityResultLauncher<String> imagePicker;

    private ActivityResultLauncher<IntentSenderRequest> tagEditRequestApi30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(getViewBinding().getRoot());

        getIntentExtras();

        songs = getSongs();
        if (songs.isEmpty()) {
            finish();
            return;
        }

        headerVariableSpace = getResources().getDimensionPixelSize(R.dimen.tagEditorHeaderVariableSpace);

        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        writeTagsApi19_SAFFilePicker = registerForActivityResult(
                new ActivityResultContracts.CreateDocument("audio/*") {
                    @NonNull
                    @Override
                    public Intent createIntent(@NonNull Context context, @NonNull String input) {
                        return super.createIntent(context, input)
                                .addCategory(Intent.CATEGORY_OPENABLE)
                                .putExtra("android.content.extra.SHOW_ADVANCED", true);
                    }
                },
                resultUri -> writeTags(List.of(currentSong)));

        writeTagsApi21_SAFGuide = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK)
                    {
                        writeTagsApi21_SAFTreePicker.launch(null);
                    }
                });

        writeTagsApi21_SAFTreePicker = registerForActivityResult(
                new ActivityResultContracts.OpenDocumentTree() {
                    @NonNull
                    @Override
                    public Intent createIntent(@NonNull Context context, Uri input) {
                        return super.createIntent(context, input)
                                .putExtra("android.content.extra.SHOW_ADVANCED", true);
                    }
                },
                resultUri -> {
                    SAFUtil.saveTreeUri(this, resultUri);
                    writeTags(savedSongs);
                });

        if (Build.VERSION.SDK_INT >= VERSION_CODES.R) {
            tagEditRequestApi30 = registerForActivityResult(
                    new ActivityResultContracts.StartIntentSenderForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            setUpViews();
                        } else {
                            showFab();
                            SafeToast.show(this, getString(R.string.access_not_granted));
                        }
                    });
        }

        imagePicker = registerForActivityResult(
                new ActivityResultContracts.GetContent() {
                    @NonNull
                    @Override
                    public Intent createIntent(@NonNull Context context, @NonNull String input) {
                        return super.createIntent(context, input)
                                .setType("image/*");
                    }
                },
                this::loadImageFromFile);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            setUpViews();
        }
        else {
            List<Uri> urisToRead = new ArrayList<>();
            for (Song song : songs) {
                urisToRead.add(ContentUris.withAppendedId(MediaStore.Audio.Media.getContentUri("external"), song.id));
            }
            PendingIntent readPendingIntent = MediaStore.createWriteRequest(this.getContentResolver(), urisToRead);

            tagEditRequestApi30.launch(new IntentSenderRequest.Builder(readPendingIntent).build());
        }
    }

    protected void setUpViews() {
        setUpScrollView();
        setUpFab();
        setUpImageView();
    }

    private void setUpScrollView() {
        observableScrollView.setScrollViewCallbacks(observableScrollViewCallbacks);
    }

    private void setUpImageView() {
        loadCurrentImage();
        final CharSequence[] items = new CharSequence[]{
                getString(R.string.download_from_last_fm),
                getString(R.string.pick_from_local_storage),
                getString(R.string.web_search),
                getString(R.string.remove_cover)
        };
        image.setOnClickListener(v -> new MaterialDialog.Builder(AbsTagEditorActivity.this)
                .title(R.string.update_image)
                .items(items)
                .itemsCallback((dialog, view, which, text) -> {
                    switch (which) {
                        case 0:
                            getImageFromLastFM();
                            break;
                        case 1:
                            imagePicker.launch(getResources().getString(R.string.pick_from_local_storage));
                            break;
                        case 2:
                            searchImageOnWeb();
                            break;
                        case 3:
                            deleteImage();
                            break;
                    }
                }).show());
    }

    protected abstract void loadCurrentImage();

    protected abstract void getImageFromLastFM();

    protected abstract void searchImageOnWeb();

    protected abstract void deleteImage();

    private void setUpFab() {
        fab.setScaleX(0);
        fab.setScaleY(0);
        fab.setEnabled(false);
        fab.setOnClickListener(v -> save());

        TintHelper.setTintAuto(fab, ThemeStore.accentColor(this), true);
    }

    protected abstract void save();

    private void getIntentExtras() {
        Bundle intentExtras = getIntent().getExtras();
        if (intentExtras != null) {
            id = intentExtras.getLong(EXTRA_ID);
        }
    }

    @NonNull
    protected abstract ViewBinding getViewBinding();

    @NonNull
    protected abstract List<Song> getSongs();

    void searchWebFor(@NonNull String... keys) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String key : keys) {
            stringBuilder.append(key);
            stringBuilder.append(" ");
        }
        Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
        intent.putExtra(SearchManager.QUERY, stringBuilder.toString());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        String query = intent.getExtras().getString(SearchManager.QUERY, null);
        Intent browserIntent = new Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com/search?q="+Uri.encode(query))
        );
        try {
            startActivity(browserIntent);
            return;
        } catch (ActivityNotFoundException ignored) {}

        SafeToast.show(this, R.string.error_no_app_for_intent);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            super.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void setNoImageMode() {
        isInNoImageMode = true;
        image.setVisibility(View.GONE);
        image.setEnabled(false);
        observableScrollView.setPadding(0, Util.getActionBarSize(this), 0, 0);
        observableScrollViewCallbacks.onScrollChanged(observableScrollView.getCurrentScrollY(), false, false);

        setColors(getIntent().getIntExtra(EXTRA_PALETTE, ThemeStore.primaryColor(this)));
        toolbar.setBackgroundColor(paletteColorPrimary);
    }

    void dataChanged() {
        showFab();
    }

    private void showFab() {
        fab.animate()
                .setDuration(500)
                .setInterpolator(new OvershootInterpolator())
                .scaleX(1)
                .scaleY(1)
                .start();
        fab.setEnabled(true);
    }

    private void hideFab() {
        fab.animate()
                .setDuration(500)
                .setInterpolator(new OvershootInterpolator())
                .scaleX(0)
                .scaleY(0)
                .start();
        fab.setEnabled(false);
    }

    void setImageBitmap(@Nullable final Bitmap bitmap, int bgColor) {
        if (bitmap == null) {
            image.setImageResource(R.drawable.default_album_art);
        } else {
            image.setImageBitmap(bitmap);
        }
        setColors(bgColor);
    }

    protected void setColors(int color) {
        paletteColorPrimary = color;
        observableScrollViewCallbacks.onScrollChanged(observableScrollView.getCurrentScrollY(), false, false);
        header.setBackgroundColor(paletteColorPrimary);
        setStatusbarColor(paletteColorPrimary);
        setNavigationbarColor(paletteColorPrimary);
        setTaskDescriptionColor(paletteColorPrimary);
    }

    void writeValuesToFiles(@NonNull final Map<FieldKey, String> fieldKeyValueMap, @Nullable final ArtworkInfo artworkInfo) {
        Util.hideSoftKeyboard(this);

        hideFab();

        savedSongs = getSongs();
        savedTags = fieldKeyValueMap;
        savedArtworkInfo = artworkInfo;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            if (!SAFUtil.isSAFRequired(savedSongs)) {
                writeTags(savedSongs);
            } else {
                writeTagsApi19();
            }
        } else if (Build.VERSION.SDK_INT < VERSION_CODES.R) {
            if (!SAFUtil.isSAFRequired(savedSongs)) {
                writeTags(savedSongs);
            } else if (SAFUtil.isSDCardAccessGranted(this)) {
                writeTags(savedSongs);
            } else {
                writeTagsApi21_SAFGuide.launch(new Intent(this, SAFGuideActivity.class));
            }
        } else {
            writeTags(savedSongs);
        }
    }

    private void writeTags(List<Song> songs) {
        new AsyncTask(this).execute(new AsyncTask.LoadingInfo(songs, savedTags, savedArtworkInfo));
    }

    private void writeTagsApi19() {
        if (savedSongs.size() < 1) return;

        currentSong = savedSongs.remove(0);

        if (!SAFUtil.isSAFRequired(currentSong.data)) {
            writeTags(List.of(currentSong));
            writeTagsApi19();
        } else {
            final String message = getString(R.string.saf_pick_file, currentSong.data);
            SafeToast.show(this, message);
            writeTagsApi19_SAFFilePicker.launch(message);
        }
    }

    private static class AsyncTask extends DialogAsyncTask<AsyncTask.LoadingInfo, Integer, String[]> {
        private final WeakReference<Activity> activity;

        AsyncTask(Activity activity) {
            super(activity);
            this.activity = new WeakReference<>(activity);
        }

        @Override
        protected String[] doInBackground(LoadingInfo... params) {
            try {
                final LoadingInfo info = params[0];

                Artwork artwork = null;
                if (info.artworkInfo != null && info.artworkInfo.artwork != null) {
                    try (AutoDeleteTempFile albumArtFile = AutoDeleteTempFile.create(null, "png")){
                        info.artworkInfo.artwork.compress(Bitmap.CompressFormat.PNG, 0, new FileOutputStream(albumArtFile.get().getCanonicalFile()));
                        artwork = ArtworkFactory.createArtworkFromFile(albumArtFile.get());
                    } catch (IOException e) {
                        OopsHandler.copyStackTraceToClipboard(e);
                    }
                }

                int counter = 0;
                for (Song song : info.songs) {
                    publishProgress(++counter, info.songs.size());
                    try (AutoCloseAudioFile audioFile = SAFUtil.loadReadWriteableAudioFile(activity.get(), song)) {
                        final Tag tag = audioFile.get().getTagOrCreateAndSetDefault();

                        if (info.fieldKeyValueMap != null) {
                            for (final Map.Entry<FieldKey, String> entry : info.fieldKeyValueMap.entrySet()) {
                                try {
                                    if (entry.getValue().trim().isEmpty()) {
                                        tag.deleteField(entry.getKey());
                                    }
                                    else if (entry.getKey() == FieldKey.ARTIST || entry.getKey() == FieldKey.ALBUM_ARTIST) {
                                        tag.deleteField(entry.getKey());
                                        final List<String> values = MultiValuesTagUtil.tagEditorSplit(entry.getValue());
                                        for (final String value : values) {
                                            tag.addField(entry.getKey(), value);
                                        }
                                    }
                                    else {
                                        tag.setField(entry.getKey(), entry.getValue().trim());
                                    }
                                } catch (Exception e) {
                                    OopsHandler.copyStackTraceToClipboard(e);
                                }
                            }
                        }

                        if (info.artworkInfo != null) {
                            if (info.artworkInfo.artwork == null) {
                                tag.deleteArtworkField();
                            } else if (artwork != null) {
                                tag.deleteArtworkField();
                                tag.setField(artwork);
                            }
                        }

                        SAFUtil.write(activity.get(), audioFile, song);
                    } catch (@NonNull Exception | NoSuchMethodError | VerifyError e) {
                        OopsHandler.copyStackTraceToClipboard(e);
                    }
                }

                Collection<String> paths = new ArrayList<>();
                for (Song song : info.songs) {
                    paths.add(song.data);
                }

                return paths.toArray(new String[0]);
            } catch (Exception e) {
                OopsHandler.copyStackTraceToClipboard(e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String[] toBeScanned) {
            super.onPostExecute(toBeScanned);
            scan(toBeScanned);
        }

        @Override
        protected void onCancelled(String[] toBeScanned) {
            super.onCancelled(toBeScanned);
            scan(toBeScanned);
        }

        private void scan(String[] toBeScanned) {
            if (toBeScanned == null) {
                return;
            }
            Discography.getInstance().removeSongByPath(toBeScanned);

            Activity activity = this.activity.get();
            if (activity != null) {
                MediaScannerConnection.scanFile(activity, toBeScanned, null, new UpdateToastMediaScannerCompletionListener(activity, toBeScanned));
            }
        }

        @Override
        protected Dialog createDialog(@NonNull Context context) {
            return new MaterialDialog.Builder(context)
                    .title(R.string.saving_changes)
                    .cancelable(false)
                    .progress(false, 0)
                    .build();
        }

        @Override
        protected void onProgressUpdate(@NonNull Dialog dialog, Integer... values) {
            super.onProgressUpdate(dialog, values);
            ((MaterialDialog) dialog).setMaxProgress(values[1]);
            ((MaterialDialog) dialog).setProgress(values[0]);
        }

        static class LoadingInfo {
            final Collection<Song> songs;
            @Nullable
            final Map<FieldKey, String> fieldKeyValueMap;
            @Nullable
            private final ArtworkInfo artworkInfo;

            private LoadingInfo(Collection<Song> songs, @Nullable Map<FieldKey, String> fieldKeyValueMap, @Nullable ArtworkInfo artworkInfo) {
                this.songs = songs;
                this.fieldKeyValueMap = fieldKeyValueMap;
                this.artworkInfo = artworkInfo;
            }
        }
    }

    public static class ArtworkInfo {
        public final long albumId;
        public final Bitmap artwork;

        public ArtworkInfo(long albumId, Bitmap artwork) {
            this.albumId = albumId;
            this.artwork = artwork;
        }
    }

    protected long getId() {
        return id;
    }

    protected abstract void loadImageFromFile(Uri selectedFile);

    @Nullable
    AutoCloseAudioFile getAudioFile() {
        return getAudioFile(songs.get(0));
    }

    @Nullable
    private AutoCloseAudioFile getAudioFile(@NonNull Song song) {
        return SAFUtil.loadReadOnlyAudioFile(this, song);
    }

    @Nullable
    static String getSongTitle(@NonNull final AudioFile audio) {
        try {
            return audio.getTagOrCreateAndSetDefault().getFirst(FieldKey.TITLE);
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    static String getAlbumTitle(@NonNull final AudioFile audio) {
        try {
            return audio.getTagOrCreateAndSetDefault().getFirst(FieldKey.ALBUM);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    static String getArtistName(@NonNull final AudioFile audio) {
        try {
            List<String> tags = audio.getTagOrCreateAndSetDefault().getAll(FieldKey.ARTIST);
            return MultiValuesTagUtil.tagEditorMerge(tags);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    static String getAlbumArtistName(@NonNull final AudioFile audio) {
        try {
            List<String> tags = audio.getTagOrCreateAndSetDefault().getAll(FieldKey.ALBUM_ARTIST);
            return MultiValuesTagUtil.tagEditorMerge(tags);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    static String getGenreName(@NonNull final AudioFile audio) {
        try {
            return audio.getTagOrCreateAndSetDefault().getFirst(FieldKey.GENRE);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    static String getSongYear(@NonNull final AudioFile audio) {
        try {
            return audio.getTagOrCreateAndSetDefault().getFirst(FieldKey.YEAR);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    static String getDiscNumber(@NonNull final AudioFile audio) {
        try {
            return audio.getTagOrCreateAndSetDefault().getFirst(FieldKey.DISC_NO);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    static String getTrackNumber(@NonNull final AudioFile audio) {
        try {
            return audio.getTagOrCreateAndSetDefault().getFirst(FieldKey.TRACK);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    static String getLyrics(@NonNull final AudioFile audio) {
        try {
            return audio.getTagOrCreateAndSetDefault().getFirst(FieldKey.LYRICS);
        } catch (Exception ignored) {
            return null;
        }
    }
}
