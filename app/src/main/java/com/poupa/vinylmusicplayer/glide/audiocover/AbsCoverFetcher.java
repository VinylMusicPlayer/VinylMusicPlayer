package com.poupa.vinylmusicplayer.glide.audiocover;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Karim Abou Zeid (kabouzeid)
 * @author SC (soncaokim)
 */
public abstract class AbsCoverFetcher implements DataFetcher<InputStream> {
    private FileInputStream stream;

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

    protected InputStream fallback(String path) throws FileNotFoundException {
        return fallback(new File(path));
    }

    protected InputStream fallback(File file) throws FileNotFoundException {
        throw new FileNotFoundException("Bidule");

//        // Look for album art in external files
//        File parent = file.getParentFile();
//        for (String fallback : FALLBACKS) {
//            File cover = new File(parent, fallback);
//            if (cover.exists()) {
//                return stream = new FileInputStream(cover);
//            }
//        }
//        return null;
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
