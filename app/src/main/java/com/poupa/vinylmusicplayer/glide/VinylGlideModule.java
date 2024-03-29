package com.poupa.vinylmusicplayer.glide;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import com.poupa.vinylmusicplayer.glide.artistimage.ArtistImage;
import com.poupa.vinylmusicplayer.glide.artistimage.ArtistImageLoader;
import com.poupa.vinylmusicplayer.glide.audiocover.FileCover;
import com.poupa.vinylmusicplayer.glide.audiocover.FileCoverLoader;
import com.poupa.vinylmusicplayer.glide.audiocover.SongCover;
import com.poupa.vinylmusicplayer.glide.audiocover.SongCoverLoader;
import com.poupa.vinylmusicplayer.glide.palette.BitmapPaletteTranscoder;
import com.poupa.vinylmusicplayer.glide.palette.BitmapPaletteWrapper;

import java.io.InputStream;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */

@GlideModule
public class VinylGlideModule extends AppGlideModule {
    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide,
                                   @NonNull Registry registry) {
        registry.append(FileCover.class, InputStream.class, new FileCoverLoader.Factory());
        registry.append(SongCover.class, InputStream.class, new SongCoverLoader.Factory());
        registry.append(ArtistImage.class, InputStream.class, new ArtistImageLoader.Factory(context));
        registry.register(Bitmap.class, BitmapPaletteWrapper.class, new BitmapPaletteTranscoder());
    }

    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }

    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        // Set to warn to mostly avoid the missing cover art exception dumps
        builder.setLogLevel(Log.WARN);
    }
}
