package com.poupa.vinylmusicplayer.service.notification;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.media.app.NotificationCompat.MediaStyle;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.request.transition.Transition;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.glide.GlideApp;
import com.poupa.vinylmusicplayer.glide.VinylGlideExtension;
import com.poupa.vinylmusicplayer.glide.VinylSimpleTarget;
import com.poupa.vinylmusicplayer.glide.palette.BitmapPaletteWrapper;
import com.poupa.vinylmusicplayer.helper.PendingIntentCompat;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.service.MusicService;
import com.poupa.vinylmusicplayer.ui.activities.MainActivity;
import com.poupa.vinylmusicplayer.util.MusicUtil;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;

import static com.poupa.vinylmusicplayer.service.MusicService.ACTION_REWIND;
import static com.poupa.vinylmusicplayer.service.MusicService.ACTION_SKIP;
import static com.poupa.vinylmusicplayer.service.MusicService.ACTION_TOGGLE_PAUSE;
import static com.poupa.vinylmusicplayer.service.MusicService.TOGGLE_FAVORITE;

public class PlayingNotificationImpl24 extends PlayingNotification {

    @Override
    public synchronized void update() {
        stopped = false;

        final Song song = service.getCurrentSong();

        final boolean isPlaying = service.isPlaying();
        final boolean isFavorite = MusicUtil.isFavorite(service, song);
        final String text = MusicUtil.getSongInfoString(song);

        final int playButtonResId = isPlaying
                ? R.drawable.ic_pause_white_24dp : R.drawable.ic_play_arrow_white_24dp;
        final int favButtonResId = isFavorite ? R.drawable.ic_favorite_white_24dp : R.drawable.ic_favorite_border_white_24dp;

        Intent action = new Intent(service, MainActivity.class);
        action.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        final PendingIntent clickIntent = PendingIntentCompat.getActivity(service, 0, action, 0);

        final ComponentName serviceName = new ComponentName(service, MusicService.class);
        Intent intent = new Intent(MusicService.ACTION_QUIT);
        intent.setComponent(serviceName);
        final PendingIntent deleteIntent = PendingIntentCompat.getService(service, 0, intent, 0);

        final int bigNotificationImageSize = service.getResources().getDimensionPixelSize(R.dimen.notification_big_image_size);
        service.runOnUiThread(() -> GlideApp.with(service)
                .asBitmapPalette()
                .load(VinylGlideExtension.getSongModel(song))
                .transition(VinylGlideExtension.getDefaultTransition())
                .songOptions(song)
                .into(new VinylSimpleTarget<BitmapPaletteWrapper>(bigNotificationImageSize, bigNotificationImageSize) {
                    @Override
                    public void onResourceReady(@NonNull BitmapPaletteWrapper resource, Transition<? super BitmapPaletteWrapper> glideAnimation) {
                        Palette palette = resource.getPalette();
                        update(resource.getBitmap(), palette.getVibrantColor(palette.getMutedColor(Color.TRANSPARENT)));
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        update(null, Color.TRANSPARENT);
                    }

                    void update(Bitmap bitmap, int color) {
                        if (bitmap == null)
                            bitmap = BitmapFactory.decodeResource(service.getResources(), R.drawable.default_album_art);
                        NotificationCompat.Action playPauseAction = new NotificationCompat.Action(playButtonResId,
                                service.getString(R.string.action_play_pause),
                                retrievePlaybackAction(ACTION_TOGGLE_PAUSE));
                        NotificationCompat.Action previousAction = new NotificationCompat.Action(R.drawable.ic_skip_previous_white_24dp,
                                service.getString(R.string.action_previous),
                                retrievePlaybackAction(ACTION_REWIND));
                        NotificationCompat.Action nextAction = new NotificationCompat.Action(R.drawable.ic_skip_next_white_24dp,
                                service.getString(R.string.action_next),
                                retrievePlaybackAction(ACTION_SKIP));
                        NotificationCompat.Action favoriteAction = new NotificationCompat.Action(favButtonResId,
                                service.getString(R.string.action_toggle_favorite),
                                retrievePlaybackAction(TOGGLE_FAVORITE));
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(service, NOTIFICATION_CHANNEL_ID)
                                .setSmallIcon(R.drawable.ic_notification)
                                .setLargeIcon(bitmap)
                                .setContentIntent(clickIntent)
                                .setDeleteIntent(deleteIntent)
                                .setContentTitle(song.title)
                                .setContentText(text)
                                .setOngoing(isPlaying)
                                .setShowWhen(false)
                                .addAction(previousAction)
                                .addAction(playPauseAction)
                                .addAction(nextAction)
                                .addAction(favoriteAction);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            builder.setStyle(new MediaStyle().setMediaSession(service.getMediaSession().getSessionToken()).setShowActionsInCompactView(0, 1, 2))
                                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
                            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O && PreferenceUtil.getInstance().coloredNotification())
                                builder.setColor(color);
                        }

                        if (stopped)
                            return; // notification has been stopped before loading was finished
                        updateImpl(builder.build());
                    }
                }));
    }

    private PendingIntent retrievePlaybackAction(final String action) {
        final ComponentName serviceName = new ComponentName(service, MusicService.class);
        Intent intent = new Intent(action);
        intent.setComponent(serviceName);

        return PendingIntentCompat.getService(service, 0, intent, 0);
    }
}
