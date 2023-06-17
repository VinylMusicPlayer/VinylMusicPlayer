package com.poupa.vinylmusicplayer.ui.activities.tageditor;

import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.viewbinding.ViewBinding;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.util.ColorUtil;
import com.kabouzeid.appthemehelper.util.TintHelper;
import com.poupa.vinylmusicplayer.App;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.discog.Discography;
import com.poupa.vinylmusicplayer.discog.tagging.MultiValuesTagUtil;
import com.poupa.vinylmusicplayer.misc.DialogAsyncTask;
import com.poupa.vinylmusicplayer.misc.SimpleObservableScrollViewCallbacks;
import com.poupa.vinylmusicplayer.misc.UpdateToastMediaScannerCompletionListener;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.ui.activities.base.AbsBaseActivity;
import com.poupa.vinylmusicplayer.ui.activities.saf.SAFGuideActivity;
import com.poupa.vinylmusicplayer.util.MusicUtil;
import com.poupa.vinylmusicplayer.util.OopsHandler;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;
import com.poupa.vinylmusicplayer.util.SAFUtil;
import com.poupa.vinylmusicplayer.util.Util;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.images.Artwork;
import org.jaudiotagger.tag.images.ArtworkFactory;

import java.io.File;
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
    private static final String TAG = AbsTagEditorActivity.class.getSimpleName();

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

    private ActivityResultLauncher<String> writeTagsKitkat_SAFFilePicker;
    private ActivityResultLauncher<Intent> writeTagsLollipop_SAFGuide;
    private ActivityResultLauncher<Uri> writeTagsAndroidR_SAFTreePicker;
    private ActivityResultLauncher<Intent> writeTagsAndroidR_SAFGuide;

    private ActivityResultLauncher<IntentSenderRequest> writeRequestAndroidR;
    private ActivityResultLauncher<Uri> writeTagsLollipop_SAFTreePicker;
    private ActivityResultLauncher<String> imagePicker;

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

        setUpViews();

        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        writeTagsKitkat_SAFFilePicker = registerForActivityResult(
                new ActivityResultContracts.CreateDocument("audio/*") {
                    @NonNull
                    @Override
                    public Intent createIntent(@NonNull Context context, @NonNull String input) {
                        return super.createIntent(context, input)
                                .addCategory(Intent.CATEGORY_OPENABLE)
                                .putExtra("android.content.extra.SHOW_ADVANCED", true);
                    }
                },
                resultUri -> writeTags(List.of(currentSong), true));

        writeTagsLollipop_SAFGuide = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK)
                    {
                        writeTagsLollipop_SAFTreePicker.launch(null);
                    }
                });

        writeTagsLollipop_SAFTreePicker = registerForActivityResult(
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
                    writeTags(savedSongs, false);
                });

        if (Build.VERSION.SDK_INT >= VERSION_CODES.R) {
            writeTagsAndroidR_SAFGuide = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            if (!PreferenceUtil.getInstance().getAlwaysAskWritePermission()) {
                                writeTagsAndroidR_SAFTreePicker.launch(Uri.parse(PreferenceUtil.getInstance().getStartDirectory().getAbsolutePath()));
                            } else if (!checkForWritingAccessForAndroid11()) {
                                askForWritingAccessForAndroid11();
                            }
                        } else {
                            showFab();
                        }
                    });

            writeTagsAndroidR_SAFTreePicker = registerForActivityResult(
                    new ActivityResultContracts.OpenDocumentTree() {
                        @NonNull
                        @Override
                        public Intent createIntent(@NonNull Context context, Uri input) {
                            return super.createIntent(context, input)
                                    .putExtra("android.content.extra.SHOW_ADVANCED", true);
                        }
                    },
                    resultUri -> {
                        if (resultUri != null) { //.getResultCode() == Activity.RESULT_OK) {
                            SAFUtil.saveTreeUri(this, resultUri);
                            writeTags(savedSongs, false);
                            if (!checkForWritingAccessForAndroid11()) {
                                askForWritingAccessForAndroid11();
                            } else {
                                writeTags(savedSongs, false);
                            }
                        } else {
                            showFab();
                            Toast.makeText(this, getString(R.string.access_not_granted), Toast.LENGTH_LONG).show();
                        }
                    });

            writeRequestAndroidR = registerForActivityResult(
                    new ActivityResultContracts.StartIntentSenderForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            writeTags(savedSongs, false);
                        } else {
                            showFab();
                            Toast.makeText(this, getString(R.string.access_not_granted), Toast.LENGTH_SHORT).show();
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
    }

    private void setUpViews() {
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

    void searchWebFor(String... keys) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String key : keys) {
            stringBuilder.append(key);
            stringBuilder.append(" ");
        }
        Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
        intent.putExtra(SearchManager.QUERY, stringBuilder.toString());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Start search intent if possible: https://stackoverflow.com/questions/36592450/unexpected-intent-with-action-web-search
        if (Intent.ACTION_WEB_SEARCH.equals(intent.getAction()) && intent.getExtras() != null) {
            String query = intent.getExtras().getString(SearchManager.QUERY, null);
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q="+Uri.encode(query)));
            boolean browserExists = intent.resolveActivityInfo(getPackageManager(), 0) != null;
            if (browserExists && query != null) {
                startActivity(browserIntent);
                return;
            }
        }

        Toast.makeText(this, R.string.error_no_app_for_intent, Toast.LENGTH_LONG).show();
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
                writeTags(savedSongs, false);
            } else {
                writeTagsKitkat();
            }
        } else if (Build.VERSION.SDK_INT < VERSION_CODES.R) {
            if (!SAFUtil.isSAFRequired(savedSongs)) {
                writeTags(savedSongs, false);
            } else if (SAFUtil.isSDCardAccessGranted(this)) {
                writeTags(savedSongs, false);
            } else {
                writeTagsLollipop_SAFGuide.launch(new Intent(this, SAFGuideActivity.class));
            }
        } else {
            if (SAFUtil.isSDCardAccessGranted(this)) {
                if (SAFUtil.isSAFRequired(savedSongs) && !checkForWritingAccessForAndroid11()) {
                    askForWritingAccessForAndroid11();
                } else {
                    writeTags(savedSongs, false);
                }
            } else if (!PreferenceUtil.getInstance().getAlwaysAskWritePermission()) {
                writeTagsAndroidR_SAFGuide.launch(new Intent(this, SAFGuideActivity.class));
            } else {
                askForWritingAccessForAndroid11();
            }
        }
    }

    private boolean checkForWritingAccessForAndroid11() {
        // TODO: find correct function to test if file can be access and better to change SAFUtil
        AudioFile test = null;
        try {
            test = AudioFileIO.read(new File(savedSongs.get(0).data));
        } catch (CannotReadException | IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException e) {
            OopsHandler.copyStackTraceToClipboard(this.getApplicationContext(), e);
        }

        return (SAFUtil.getUriFromAudio(this, test, null) != null && Build.VERSION.SDK_INT >= VERSION_CODES.R);
    }


    @RequiresApi(api = VERSION_CODES.R)
    private void askForWritingAccessForAndroid11() {
        List<Uri> urisToModify = new ArrayList<>();

        for (Song song : getSongs()) {
            // See: https://stackoverflow.com/questions/64472765/java-lang-illegalargumentexception-all-requested-items-must-be-referenced-by-sp
            urisToModify.add(ContentUris.withAppendedId(MediaStore.Audio.Media.getContentUri("external"), song.id));
        }
        PendingIntent editPendingIntent =
                MediaStore.createWriteRequest(this.getContentResolver(), urisToModify);

        // Launch a system prompt requesting user permission for the operation.
        writeRequestAndroidR.launch(new IntentSenderRequest.Builder(editPendingIntent).build());
    }

    private void writeTags(List<Song> songs, boolean kitkatPickFile) {
        new WriteTagsAsyncTask(this).execute(new WriteTagsAsyncTask.LoadingInfo(songs, savedTags, savedArtworkInfo, kitkatPickFile));
    }

    private void writeTagsKitkat() {
        if (savedSongs.size() < 1) return;

        currentSong = savedSongs.remove(0);

        if (!SAFUtil.isSAFRequired(currentSong.data)) {
            writeTags(List.of(currentSong), false);
            writeTagsKitkat();
        } else {
            final String message = getString(R.string.saf_pick_file, currentSong.data);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            writeTagsKitkat_SAFFilePicker.launch(message);
        }
    }

    private static class WriteTagsAsyncTask extends DialogAsyncTask<WriteTagsAsyncTask.LoadingInfo, Integer, String[]> {
        private final WeakReference<Activity> activity;

        WriteTagsAsyncTask(Activity activity) {
            super(activity);
            this.activity = new WeakReference<>(activity);
        }

        @Override
        protected String[] doInBackground(LoadingInfo... params) {
            try {
                final LoadingInfo info = params[0];

                Artwork artwork = null;
                File albumArtFile = null;
                final String albumArtMimeType = "image/png";
                if (info.artworkInfo != null && info.artworkInfo.artwork != null) {
                    try {
                        albumArtFile = MusicUtil.createAlbumArtFile().getCanonicalFile();
                        info.artworkInfo.artwork.compress(Bitmap.CompressFormat.PNG, 0, new FileOutputStream(albumArtFile));
                        artwork = ArtworkFactory.createArtworkFromFile(albumArtFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                int counter = 0;
                boolean wroteArtwork = false;
                boolean deletedArtwork = false;
                for (Song song : info.songs) {
                    String filePath = song.data;
                    publishProgress(++counter, info.songs.size());
                    try {
                        Uri safUri = null;

                        if (info.kitkatPickFile) {
                            safUri = Uri.parse("audio/*");
                        }

                        final AudioFile audioFile = AudioFileIO.read(new File(filePath));
                        final Tag tag = audioFile.getTagOrCreateAndSetDefault();

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
                                    e.printStackTrace();
                                }
                            }
                        }

                        if (info.artworkInfo != null) {
                            if (info.artworkInfo.artwork == null) {
                                tag.deleteArtworkField();
                                deletedArtwork = true;
                            } else if (artwork != null) {
                                tag.deleteArtworkField();
                                tag.setField(artwork);
                                wroteArtwork = true;
                            }
                        }

                        SAFUtil.write(activity.get(), audioFile, safUri);
                    } catch (@NonNull Exception | NoSuchMethodError | VerifyError e) {
                        e.printStackTrace();
                    }
                }

                final Context context = getContext();
                if (context != null) {
                    if (wroteArtwork) {
                        if (info.artworkInfo == null) {throw new AssertionError();}
                        MusicUtil.insertAlbumArt(
                                context,
                                info.artworkInfo.albumId,
                                albumArtFile.getPath(),
                                albumArtMimeType);
                    } else if (deletedArtwork) {
                        if (info.artworkInfo == null) {throw new AssertionError();}
                        MusicUtil.deleteAlbumArt(context, info.artworkInfo.albumId);
                    }
                }

                Collection<String> paths = new ArrayList<>();
                for (Song song : info.songs) {
                    paths.add(song.data);
                }

                return paths.toArray(new String[0]);
            } catch (Exception e) {
                e.printStackTrace();
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

            final boolean kitkatPickFile;

            private LoadingInfo(Collection<Song> songs, @Nullable Map<FieldKey, String> fieldKeyValueMap, @Nullable ArtworkInfo artworkInfo, boolean kitkatPickFile) {
                this.songs = songs;
                this.fieldKeyValueMap = fieldKeyValueMap;
                this.artworkInfo = artworkInfo;
                this.kitkatPickFile = kitkatPickFile;
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

    @NonNull
    private AudioFile getAudioFile(@NonNull String path) {
        try {
            return AudioFileIO.read(new File(path));
        } catch (@NonNull Exception | NoSuchMethodError | VerifyError e) {
            Log.e(TAG, "Could not read audio file " + path, e);
            return new AudioFile();
        }
    }

    @Nullable
    String getSongTitle() {
        try {
            return getAudioFile(songs.get(0).data).getTagOrCreateAndSetDefault().getFirst(FieldKey.TITLE);
        } catch (Exception e) {
            OopsHandler.copyStackTraceToClipboard(App.getStaticContext(), e);
            return null;
        }
    }

    @Nullable
    String getAlbumTitle() {
        try {
            return getAudioFile(songs.get(0).data).getTagOrCreateAndSetDefault().getFirst(FieldKey.ALBUM);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    String getArtistName() {
        try {
            List<String> tags = getAudioFile(songs.get(0).data).getTagOrCreateAndSetDefault().getAll(FieldKey.ARTIST);
            return MultiValuesTagUtil.tagEditorMerge(tags);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    String getAlbumArtistName() {
        try {
            List<String> tags = getAudioFile(songs.get(0).data).getTagOrCreateAndSetDefault().getAll(FieldKey.ALBUM_ARTIST);
            return MultiValuesTagUtil.tagEditorMerge(tags);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    String getGenreName() {
        try {
            return getAudioFile(songs.get(0).data).getTagOrCreateAndSetDefault().getFirst(FieldKey.GENRE);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    String getSongYear() {
        try {
            return getAudioFile(songs.get(0).data).getTagOrCreateAndSetDefault().getFirst(FieldKey.YEAR);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    String getDiscNumber() {
        try {
            return getAudioFile(songs.get(0).data).getTagOrCreateAndSetDefault().getFirst(FieldKey.DISC_NO);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    String getTrackNumber() {
        try {
            return getAudioFile(songs.get(0).data).getTagOrCreateAndSetDefault().getFirst(FieldKey.TRACK);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    String getLyrics() {
        try {
            return getAudioFile(songs.get(0).data).getTagOrCreateAndSetDefault().getFirst(FieldKey.LYRICS);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    Bitmap getAlbumArt() {
        try {
            final Artwork artworkTag = getAudioFile(songs.get(0).data).getTagOrCreateAndSetDefault().getFirstArtwork();
            if (artworkTag != null) {
                final byte[] artworkBinaryData = artworkTag.getBinaryData();
                return BitmapFactory.decodeByteArray(artworkBinaryData, 0, artworkBinaryData.length);
            }
            return null;
        } catch (Exception ignored) {
            return null;
        }
    }
}
