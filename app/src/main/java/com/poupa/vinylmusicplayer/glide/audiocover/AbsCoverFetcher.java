package com.poupa.vinylmusicplayer.glide.audiocover;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;
import com.poupa.vinylmusicplayer.App;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.util.AutoCloseAudioFile;
import com.poupa.vinylmusicplayer.util.OopsHandler;
import com.poupa.vinylmusicplayer.util.SAFUtil;
import com.poupa.vinylmusicplayer.util.Util;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.tag.images.Artwork;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
    private InputStream stream;

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

    @Nullable
    InputStream loadCoverFromAudioTags(@NonNull final Song song) {
        try (final AutoCloseAudioFile audio = SAFUtil.loadReadOnlyAudioFile(App.getStaticContext(), song)) {
            if (audio == null) {return null;}
            return loadCoverFromAudioTags(audio.get());
        } catch (final Exception e) {
            OopsHandler.collectStackTrace(e);
        }
        return null;
    }

    @Nullable
    InputStream loadCoverFromAudioTags(@NonNull final File file) {
        try {
            final AudioFile audio = SAFUtil.loadAudioFile(file);
            if (audio == null) {return null;}
            return loadCoverFromAudioTags(audio);
        } catch (final Exception e) {
            OopsHandler.collectStackTrace(e);
        }
        return null;
    }

    @Nullable
    private InputStream loadCoverFromAudioTags(@NonNull final AudioFile audio) {
        final Artwork art = audio.getTag().getFirstArtwork();
        if (art != null) {
            final byte[] imageData = art.getBinaryData();
            return stream = new ByteArrayInputStream(imageData);
        }
        return null;
    }

    @Nullable
    InputStream loadCoverFromMediaStore(@NonNull final Song song) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            final Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, song.albumId);
            try {
                final Context context = App.getStaticContext();
                final Point screenSize = Util.getScreenSize(context);
                final int coverSize = Math.min(screenSize.x, screenSize.y);
                final Bitmap cover = context.getContentResolver().loadThumbnail(uri, new Size(coverSize, coverSize), null);

                // Hideous way to transfer the Bitmap data to an InputStream
                ByteArrayOutputStream ostream = new ByteArrayOutputStream();
                cover.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
                return stream = new ByteArrayInputStream(ostream.toByteArray());
            } catch (final FileNotFoundException ignored) {
                // No album
            } catch (final IOException error) {
                OopsHandler.collectStackTrace(error);
            }
        }
        return null;
    }

    private static final String[] FOLDER_IMAGE_FALLBACKS = {
            "cover.jpg", "album.jpg", "folder.jpg",
            "cover.png", "album.png", "folder.png"
    };

    @Nullable
    InputStream loadCoverFromFolderImage(@NonNull final File file) {
        // Look for album art in external files
        try {
            final File parent = file.getParentFile();
            for (final String fallback : FOLDER_IMAGE_FALLBACKS) {
                final File cover = new File(parent, fallback);
                if (cover.exists()) {
                    return stream = new FileInputStream(cover);
                }
            }
        } catch (final FileNotFoundException ignored) {}
        return null;
    }

    @Override
    public void cleanup() {
        // already cleaned up in loadData and ByteArrayInputStream will be GC'd
        if (stream != null) {
            try {
                stream.close();
            } catch (final IOException ignore) {
                // can't do much about it
            }
        }
    }

    @Override
    public void cancel() {
        // cannot cancel
    }
}
