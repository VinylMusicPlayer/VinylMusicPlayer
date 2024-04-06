package com.poupa.vinylmusicplayer.glide.audiocover;

import androidx.annotation.NonNull;

import com.bumptech.glide.Priority;
import com.poupa.vinylmusicplayer.util.OopsHandler;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author SC (soncaokim)
 */
public class FileCoverFetcher extends AbsCoverFetcher {
    private final FileCover model;

    FileCoverFetcher(@NonNull final FileCover value) {
        super();
        model = value;
    }

    @Override
    public void loadData(@NonNull final Priority priority, @NonNull final DataCallback<? super InputStream> callback) {
        try {
            InputStream input = loadCoverFromAudioTags(model.file);
            if (input == null) {
                input = loadCoverFromFolderImage(model.file);
            }
            if (input == null) {
                input = loadCoverFromMediaStore(model.file);
            }

            if (input == null) {
                callback.onLoadFailed(new IOException("Cannot load cover for file"));
            } else {
                callback.onDataReady(input);
            }
        } catch (final Exception e) {
            OopsHandler.collectStackTrace(e);
            callback.onLoadFailed(e);
        }
    }
}
