package com.poupa.vinylmusicplayer.util;

import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.util.ATHUtil;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.adapter.song.SongAdapter;
import com.poupa.vinylmusicplayer.glide.GlideApp;
import com.poupa.vinylmusicplayer.glide.VinylColoredTarget;
import com.poupa.vinylmusicplayer.glide.VinylGlideExtension;
import com.poupa.vinylmusicplayer.helper.MusicPlayerRemote;
import com.poupa.vinylmusicplayer.model.Song;

/**
 * @author SC (soncaokim)
 */
public class PlayingSongDecorationUtil {

    public static final int iconPlaying = R.drawable.ic_notification;

    public static Animation iconAnimation;
    static {
        iconAnimation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        iconAnimation.setDuration(ViewUtil.VINYL_MUSIC_PLAYER_ANIM_TIME);
        iconAnimation.setRepeatCount(Animation.INFINITE);
    }

    public static void decorate(
            @NonNull final SongAdapter songAdapter,
            @NonNull final SongAdapter.ViewHolder holder,
            Song song,
            @NonNull final AppCompatActivity activity)
    {
        PlayingSongDecorationUtil.decorate(holder.title, holder.image, holder.imageText, song, activity, songAdapter.isShowAlbumImage());

        final boolean isPlaying = MusicPlayerRemote.isPlaying(song);

        if ((holder.image != null) && !isPlaying && songAdapter.isShowAlbumImage()) {
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
                image.setScaleType(ImageView.ScaleType.CENTER);

                final int color = ATHUtil.resolveColor(activity, R.attr.iconColor, ThemeStore.textColorSecondary(activity));
                image.setColorFilter(color, PorterDuff.Mode.SRC_IN);

                final int size = (int)(24 * activity.getResources().getDisplayMetrics().density);

                GlideApp.with(activity)
                        .asBitmap()
                        .load(iconPlaying)
                        .override(size)
                        .transition(VinylGlideExtension.getDefaultTransition())
                        .songOptions(song)
                        .into(image);

                image.startAnimation(iconAnimation);
            }
            else {
                image.clearColorFilter();
                image.clearAnimation();
            }
        }

        if (imageText != null) {
            imageText.setVisibility((isPlaying || showAlbumImage) ? View.GONE : View.VISIBLE);
        }
    }

}
