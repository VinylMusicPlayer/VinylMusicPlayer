package com.poupa.vinylmusicplayer.glide.audiocover;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;
import com.poupa.vinylmusicplayer.util.OopsHandler;
import com.poupa.vinylmusicplayer.util.SAFUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.MissingResourceException;

/**
 * @author Karim Abou Zeid (kabouzeid)
 * @author SC (soncaokim)
 */
public abstract class AbsCoverFetcher implements DataFetcher<InputStream> {
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

    protected void fallback(String path, @NonNull DataCallback<? super InputStream> callback) throws FileNotFoundException {
        fallback(new File(path), callback);
    }

    public void fallback(@NonNull File file, @NonNull DataFetcher.DataCallback<? super InputStream> callback) throws FileNotFoundException {
        // Look for album art in external files
        final String[] FALLBACKS = {
                "cover.jpg", "album.jpg", "folder.jpg",
                "cover.png", "album.png", "folder.png"
        };

        File parent = file.getParentFile();
        boolean coverFound = false;
        for (String fallback : FALLBACKS) {
            File cover = new File(parent, fallback);
            if (cover.exists()) {
                try {
                    coverFound = true;
                    final FileInputStream imageData = SAFUtil.loadImageFile(cover);
                    callback.onDataReady(imageData);
                } catch (FileNotFoundException notFound) {
                    OopsHandler.copyStackTraceToClipboard(notFound);
                    callback.onLoadFailed(notFound);
                }
            }
        }
        if (!coverFound) {
            callback.onLoadFailed(new MissingResourceException("No artwork", "", ""));
        }
    }

    @Override
    public void cleanup() {
    }

    @Override
    public void cancel() {
        // cannot cancel
    }
}
