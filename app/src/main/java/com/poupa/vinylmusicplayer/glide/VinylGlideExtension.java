package com.poupa.vinylmusicplayer.glide;

import androidx.annotation.NonNull;

import com.bumptech.glide.GenericTransitionOptions;
import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.annotation.GlideExtension;
import com.bumptech.glide.annotation.GlideOption;
import com.bumptech.glide.annotation.GlideType;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.BaseRequestOptions;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.MediaStoreSignature;
import com.poupa.vinylmusicplayer.App;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.glide.artistimage.ArtistImage;
import com.poupa.vinylmusicplayer.glide.audiocover.AudioFileCover;
import com.poupa.vinylmusicplayer.glide.palette.BitmapPaletteWrapper;
import com.poupa.vinylmusicplayer.model.Artist;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.util.ArtistSignatureUtil;
import com.poupa.vinylmusicplayer.util.CustomArtistImageUtil;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */

@GlideExtension
public final class VinylGlideExtension {
    private VinylGlideExtension() {
    }

    @GlideType(BitmapPaletteWrapper.class)
    @NonNull
    public static RequestBuilder<BitmapPaletteWrapper> asBitmapPalette(@NonNull RequestBuilder<BitmapPaletteWrapper> requestBuilder) {
        return requestBuilder;
    }

    @GlideOption
    @NonNull
    public static BaseRequestOptions<?> artistOptions(@NonNull BaseRequestOptions<?> requestOptions, Artist artist) {
        return requestOptions
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .error(R.drawable.default_artist_image)
                .placeholder(R.drawable.default_artist_image)
                .priority(Priority.LOW)
                .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .signature(createSignature(artist));
    }

    @GlideOption
    @NonNull
    public static BaseRequestOptions<?> songOptions(@NonNull BaseRequestOptions<?> requestOptions, Song song) {
        return requestOptions
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .error(R.drawable.default_album_art)
                .placeholder(R.drawable.default_album_art)
                .signature(createSignature(song));
    }

    public static Key createSignature(Artist artist) {
        return ArtistSignatureUtil.getInstance().getArtistSignature(artist.getName());
    }

    public static Key createSignature(Song song) {
        return new MediaStoreSignature("", song.dateModified, 0);
    }

    public static Object getArtistModel(Artist artist) {
        return getArtistModel(artist, CustomArtistImageUtil.getInstance(App.getInstance()).hasCustomArtistImage(artist), false);
    }

    public static Object getArtistModel(Artist artist, boolean forceDownload) {
        return getArtistModel(artist, CustomArtistImageUtil.getInstance(App.getInstance()).hasCustomArtistImage(artist), forceDownload);
    }

    public static Object getArtistModel(Artist artist, boolean hasCustomImage, boolean forceDownload) {
        if (!hasCustomImage) {
            return new ArtistImage(artist.getName(), forceDownload);
        } else {
            return CustomArtistImageUtil.getFile(artist);
        }
    }

    public static Object getSongModel(@NonNull final Song song) {
        return new AudioFileCover(song);
    }

    public static <TranscodeType> GenericTransitionOptions<TranscodeType> getDefaultTransition() {
        return new GenericTransitionOptions<TranscodeType>().transition(android.R.anim.fade_in);
    }
}
