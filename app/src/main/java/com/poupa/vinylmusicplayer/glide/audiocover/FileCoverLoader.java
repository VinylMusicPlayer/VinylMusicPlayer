package com.poupa.vinylmusicplayer.glide.audiocover;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.bumptech.glide.signature.ObjectKey;

import java.io.InputStream;

/**
 * @author SC (soncaokim)
 */

public class FileCoverLoader implements ModelLoader<FileCover, InputStream> {

    @Override
    public LoadData<InputStream> buildLoadData(@NonNull FileCover model, int width, int height,
                                               @NonNull Options options) {
        return new LoadData<>(new ObjectKey(model.file), new FileCoverFetcher(model));
    }

    @Override
    public boolean handles(@NonNull FileCover model) {
        return true;
    }

    public static class Factory implements ModelLoaderFactory<FileCover, InputStream> {
        @Override
        @NonNull
        public ModelLoader<FileCover, InputStream> build(@NonNull MultiModelLoaderFactory multiFactory) {
            return new FileCoverLoader();
        }

        @Override
        public void teardown() {
        }
    }
}

