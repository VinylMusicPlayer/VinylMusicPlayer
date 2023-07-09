package com.poupa.vinylmusicplayer.glide.audiocover;

import androidx.annotation.NonNull;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;
import com.poupa.vinylmusicplayer.App;
import com.poupa.vinylmusicplayer.util.AutoDeleteAudioFile;
import com.poupa.vinylmusicplayer.util.SAFUtil;

import org.jaudiotagger.tag.images.Artwork;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.MissingResourceException;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class AudioFileCoverFetcher implements DataFetcher<InputStream> {
    private final AudioFileCover model;
    private FileInputStream stream;

    public AudioFileCoverFetcher(AudioFileCover model) {
        this.model = model;
    }

    @Override
    public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super InputStream> callback) {
        try (AutoDeleteAudioFile audio = SAFUtil.loadAudioFile(App.getStaticContext(), model.song)) {
            Artwork art = audio.get().getTag().getFirstArtwork();
            if (art != null) {
                byte[] imageData = art.getBinaryData();
                callback.onDataReady(new ByteArrayInputStream(imageData));
            } else {
                InputStream data = fallback(model.song.data);
                if (data != null) {callback.onDataReady(data);}
                else {callback.onLoadFailed(new MissingResourceException("No artwork", "", ""));}
            }
        } catch (Exception e) {
            callback.onLoadFailed(e);
        }
    }

    @NonNull
    @Override
    public Class<InputStream> getDataClass() {
        return InputStream.class;
    }

    @NonNull
    @Override
    public DataSource getDataSource() {
        return DataSource.LOCAL;
    }

    private static final String[] FALLBACKS =
            {"cover.jpg", "album.jpg", "folder.jpg", "cover.png", "album.png", "folder.png"};

    private InputStream fallback(String path) throws FileNotFoundException {
        // Look for album art in external files
        // TODO This probably wont work on Android 13 (or at least requires explicit permission UI)
        File parent = new File(path).getParentFile();
        for (String fallback : FALLBACKS) {
            File cover = new File(parent, fallback);
            if (cover.exists()) {
                return stream = new FileInputStream(cover);
            }
        }
        return null;
    }

    @Override
    public void cleanup() {
        // already cleaned up in loadData and ByteArrayInputStream will be GC'd
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException ignore) {
                // can't do much about it
            }
        }
    }

    @Override
    public void cancel() {
        // cannot cancel
    }
}
