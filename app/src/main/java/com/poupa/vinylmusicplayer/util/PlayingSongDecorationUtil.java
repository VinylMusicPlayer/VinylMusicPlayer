package com.poupa.vinylmusicplayer.util;

import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.adapter.song.SongAdapter;
import com.poupa.vinylmusicplayer.glide.VinylColoredTarget;
import com.poupa.vinylmusicplayer.helper.MusicPlayerRemote;
import com.poupa.vinylmusicplayer.glide.GlideApp;
import com.poupa.vinylmusicplayer.glide.VinylGlideExtension;
import com.poupa.vinylmusicplayer.model.Song;

/**
 * @author SC (soncaokim)
 */
public class PlayingSongDecorationUtil {
    public static void decorate(
            @NonNull final SongAdapter songAdapter,
            @NonNull final SongAdapter.ViewHolder holder,
            Song song,
            @NonNull final AppCompatActivity activity)
    {
        PlayingSongDecorationUtil.decorate(holder.title, holder.image, holder.imageText, song, activity, songAdapter.isShowAlbumImage());

        if ((holder.image != null) && songAdapter.isShowAlbumImage()) {
            GlideApp.with(activity)
                    .asBitmapPalette()
                    .load(VinylGlideExtension.getSongModel(song))
                    .transition(VinylGlideExtension.getDefaultTransition())
                    .songOptions(song)
                    .into(new VinylColoredTarget(holder.image) {
                        @Override
                        public void onLoadCleared(Drawable placeholder) {
                            super.onLoadCleared(placeholder);
                            songAdapter.setColors(getDefaultFooterColor(), holder);
                        }

                        @Override
                        public void onColorReady(int color) {
                            songAdapter.setColors(songAdapter.isUsePalette() ? color : getDefaultFooterColor(), holder);
                        }
                    });
        }
    }

    public static void decorate(
            @Nullable final TextView title,
            @Nullable final ImageView image,
            @Nullable final TextView imageText,
            Song song,
            @NonNull final AppCompatActivity activity,
            boolean showAlbumImage)
    {
        final boolean isPlaying = MusicPlayerRemote.isPlaying(song);

        if (title != null) {
            title.setTypeface(null, isPlaying ? Typeface.BOLD : Typeface.NORMAL);
        }

        if (image != null) {
            image.setVisibility((isPlaying || showAlbumImage) ? View.VISIBLE : View.GONE);

            if (isPlaying) {
                // TODO On white theme, the indicator icon is not visible
                // TODO Consider overlaying the icon over album art, or use an animated icon
                image.setScaleType(ImageView.ScaleType.CENTER);
                GlideApp.with(activity)
                        .asBitmap()
                        .load(R.drawable.ic_volume_up_white_24dp)
                        .transition(VinylGlideExtension.getDefaultTransition())
                        .songOptions(song)
                        .into(image);
            }
        }

        if (imageText != null) {
            imageText.setVisibility((isPlaying || showAlbumImage) ? View.GONE : View.VISIBLE);
        }
    }

}
