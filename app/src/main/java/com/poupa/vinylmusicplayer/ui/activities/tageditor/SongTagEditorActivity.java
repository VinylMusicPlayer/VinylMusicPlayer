package com.poupa.vinylmusicplayer.ui.activities.tageditor;

import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.viewbinding.ViewBinding;

import com.kabouzeid.appthemehelper.util.ToolbarContentTintHelper;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.databinding.ActivitySongTagEditorBinding;
import com.poupa.vinylmusicplayer.discog.Discography;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.util.AutoDeleteAudioFile;
import com.poupa.vinylmusicplayer.util.OopsHandler;

import org.jaudiotagger.tag.FieldKey;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class SongTagEditorActivity extends AbsTagEditorActivity implements TextWatcher {

    EditText songTitle;
    EditText albumTitle;
    EditText artist;
    EditText genre;
    EditText year;
    EditText trackNumber;
    EditText discNumber;
    EditText lyrics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setNoImageMode();

        //noinspection ConstantConditions
        getSupportActionBar().setTitle(R.string.action_tag_editor);
    }

    @Override
    protected void setUpViews() {
        super.setUpViews();

        fillViewsWithFileTags();
        songTitle.addTextChangedListener(this);
        albumTitle.addTextChangedListener(this);
        artist.addTextChangedListener(this);
        genre.addTextChangedListener(this);
        year.addTextChangedListener(this);
        trackNumber.addTextChangedListener(this);
        discNumber.addTextChangedListener(this);
        lyrics.addTextChangedListener(this);

        // Dont wrap text if line too long, make it scrollable
        // https://stackoverflow.com/questions/5146207/disable-word-wrap-in-an-android-multi-line-textview
        artist.setHorizontallyScrolling(true);
    }

    private void fillViewsWithFileTags() {
        try (AutoDeleteAudioFile audio = getAudioFile()) {
            if (audio != null) {
                songTitle.setText(getSongTitle(audio.get()));
                albumTitle.setText(getAlbumTitle(audio.get()));
                artist.setText(getArtistName(audio.get()));
                genre.setText(getGenreName(audio.get()));
                year.setText(getSongYear(audio.get()));
                trackNumber.setText(getTrackNumber(audio.get()));
                discNumber.setText(getDiscNumber(audio.get()));
                lyrics.setText(getLyrics(audio.get()));
            }
        } catch (Exception e) {
            OopsHandler.copyStackTraceToClipboard(e);
        }
    }

    @Override
    protected void loadCurrentImage() {

    }

    @Override
    protected void getImageFromLastFM() {

    }

    @Override
    protected void searchImageOnWeb() {

    }

    @Override
    protected void deleteImage() {

    }

    @Override
    protected void save() {
        Map<FieldKey, String> fieldKeyValueMap = new EnumMap<>(FieldKey.class);

        fieldKeyValueMap.put(FieldKey.TITLE, songTitle.getText().toString());
        fieldKeyValueMap.put(FieldKey.ALBUM, albumTitle.getText().toString());
        fieldKeyValueMap.put(FieldKey.ARTIST, artist.getText().toString());
        fieldKeyValueMap.put(FieldKey.GENRE, genre.getText().toString());
        fieldKeyValueMap.put(FieldKey.YEAR, year.getText().toString());
        fieldKeyValueMap.put(FieldKey.TRACK, trackNumber.getText().toString());
        fieldKeyValueMap.put(FieldKey.DISC_NO, discNumber.getText().toString());
        fieldKeyValueMap.put(FieldKey.LYRICS, lyrics.getText().toString());

        writeValuesToFiles(fieldKeyValueMap, null);
    }

    @Override
    @NonNull
    protected ViewBinding getViewBinding() {
        ActivitySongTagEditorBinding binding = ActivitySongTagEditorBinding.inflate(LayoutInflater.from(this));

        songTitle = binding.title1;
        albumTitle = binding.title2;
        artist = binding.artist;
        genre = binding.genre;
        year = binding.year;
        trackNumber = binding.trackNumber;
        discNumber = binding.discNumber;
        lyrics = binding.lyrics;

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
        ArrayList<Song> songs = new ArrayList<>(1);
        songs.add(Discography.getInstance().getSong(getId()));
        return songs;
    }

    @Override
    protected void loadImageFromFile(Uri imageFilePath) {

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
        int toolbarTitleColor = ToolbarContentTintHelper.toolbarTitleColor(this, color);
        songTitle.setTextColor(toolbarTitleColor);
        albumTitle.setTextColor(toolbarTitleColor);
    }
}
