package com.poupa.vinylmusicplayer.glide.audiocover;

import android.content.Context;
import android.content.UriPermission;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;
import com.poupa.vinylmusicplayer.App;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

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
        // Look for album art in external files
        File parent = file.getParentFile();
        for (String fallback : FALLBACKS) {
            File cover = new File(parent, fallback);
            if (cover.exists()) {
                // TODO FileNotFoundException: open failed EACCESS(PermissionDenied)
                if (cover.canRead()) { // !SAFUtil.isSAFRequired(cover)) {
                    return stream = new FileInputStream(cover);
                } else {
                    final Context context = App.getStaticContext();
                    String message = String.format("No access to file=%s", cover.getPath());
                    final List<UriPermission> perms = context.getContentResolver().getPersistedUriPermissions();
                    for (final UriPermission perm : perms) {
                        message += "\nAllowed=";
                        message += perm.getUri().toString();
                    }
                    throw new UnsupportedOperationException(message);
                }
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
