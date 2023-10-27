package com.poupa.vinylmusicplayer.glide.audiocover;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.bumptech.glide.signature.ObjectKey;

import java.io.InputStream;

/**
 * @author Karim Abou Zeid (kabouzeid)
 * @author SC (soncaokim)
 */

public class SongCoverLoader implements ModelLoader<SongCover, InputStream> {

    @Override
    public LoadData<InputStream> buildLoadData(@NonNull SongCover model, int width, int height,
                                               @NonNull Options options) {
        return new LoadData<>(new ObjectKey(model.song), new SongCoverFetcher(model));
    }

    @Override
    public boolean handles(@NonNull SongCover model) {
        return true;
    }

    public static class Factory implements ModelLoaderFactory<SongCover, InputStream> {
        @Override
        @NonNull
        public ModelLoader<SongCover, InputStream> build(@NonNull MultiModelLoaderFactory multiFactory) {
            return new SongCoverLoader();
        }

        @Override
        public void teardown() {
        }
    }
}

