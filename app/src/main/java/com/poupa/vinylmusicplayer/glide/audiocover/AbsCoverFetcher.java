package com.poupa.vinylmusicplayer.glide.audiocover;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;
import com.poupa.vinylmusicplayer.App;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.util.AutoCloseAudioFile;
import com.poupa.vinylmusicplayer.util.FileUtil;
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
import java.util.List;

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

            stream = loadCoverFromAudioTags(audio.get());
            return stream;
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

            stream = loadCoverFromAudioTags(audio);
            return stream;
        } catch (final Exception e) {
            OopsHandler.collectStackTrace(e);
        }
        return null;
    }

    @Nullable
    private static InputStream loadCoverFromAudioTags(@NonNull final AudioFile audio) {
        final Artwork art = audio.getTag().getFirstArtwork();
        if (art != null) {
            final byte[] imageData = art.getBinaryData();
            return new ByteArrayInputStream(imageData);
        }
        return null;
    }

    @Nullable
    InputStream loadCoverFromMediaStore(@NonNull final Song song) {
        try {
            final long albumId = song.albumId;
            stream = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    ? loadCoverFromMediaStoreApi29(albumId)
                    : loadCoverFromMediaStoreApi19(albumId);
            return stream;
        } catch (final Exception e) {
            OopsHandler.collectStackTrace(e);
        }
        return null;
    }

    @Nullable
    InputStream loadCoverFromMediaStore(@NonNull final File file) {
        final List<Song> matchingSongs = FileUtil.matchFilesWithMediaStore(List.of(file));
        if (matchingSongs.size() != 1) {return null;} // non unique, abandon

        final Song song = matchingSongs.get(0);
        if (song.id == Song.EMPTY_SONG.id) {return null;} // not found, abandon

        stream = loadCoverFromMediaStore(song);
        return stream;
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    @Nullable
    private static InputStream loadCoverFromMediaStoreApi29(final long albumId) throws IOException {
        try {
            final Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, albumId);
            final Context context = App.getStaticContext();
            final Point screenSize = Util.getScreenSize(context);
            final int coverSize = Math.min(screenSize.x, screenSize.y);
            final Bitmap cover = context.getContentResolver().loadThumbnail(uri, new Size(coverSize, coverSize), null);

            return bitmap2InputStream(cover);
        } catch (final FileNotFoundException ignored) {}
        return null;
    }

    @Nullable
    private static InputStream loadCoverFromMediaStoreApi19(final long albumId) throws FileNotFoundException {
        final Context context = App.getStaticContext();
        try (final Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                new String[] {BaseColumns._ID, MediaStore.Audio.AlbumColumns.ALBUM_ART},
                BaseColumns._ID + " = " + albumId,
                null,
                null))
        {
            if (cursor == null) {return null;}

            final int columnIndex = cursor.getColumnIndex(MediaStore.Audio.AlbumColumns.ALBUM_ART);
            if (cursor.moveToNext()) {
                final String path = cursor.getString(columnIndex);
                if (path == null) {return null;}

                return new FileInputStream(path);
            }
        }

        return null;
    }

    @NonNull
    private static InputStream bitmap2InputStream(@NonNull final Bitmap bitmap) {
        // Hideous way to transfer the Bitmap data to an InputStream
        final ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
        return new ByteArrayInputStream(ostream.toByteArray());
    }

    private static final String[] FOLDER_IMAGE_FALLBACKS = {
            "cover.jpg", "album.jpg", "folder.jpg",
            "cover.jpeg", "album.jpeg", "folder.jpeg",
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
                    stream = new FileInputStream(cover);
                    return stream;
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
