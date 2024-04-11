package com.poupa.vinylmusicplayer.glide.audiocover;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Priority;
import com.poupa.vinylmusicplayer.util.OopsHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Karim Abou Zeid (kabouzeid)
 * @author SC (soncaokim)
 */
public class SongCoverFetcher extends AbsCoverFetcher {
    private final SongCover model;

    public SongCoverFetcher(@NonNull final SongCover value) {
        super();
        model = value;
    }

    @Override
    public void loadData(@NonNull final Priority priority, @NonNull final DataCallback<? super InputStream> callback) {
        try {
            final InputStream input = loadData();
            if (input == null) {
                callback.onLoadFailed(new IOException("Cannot load cover for song"));
            } else {
                callback.onDataReady(input);
            }
        } catch (final Exception e) {
            OopsHandler.collectStackTrace(e);
            callback.onLoadFailed(e);
        }
    }

    @Nullable
    public InputStream loadData() {
        InputStream input = loadCoverFromAudioTags(model.song);
        if (input == null) {
            input = loadCoverFromMediaStore(model.song);
        }
        if (input == null) {
            input = loadCoverFromFolderImage(new File(model.song.data));
        }
        return input;
    }
}
