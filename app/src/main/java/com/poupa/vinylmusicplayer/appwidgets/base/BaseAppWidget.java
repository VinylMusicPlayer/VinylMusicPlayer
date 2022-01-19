package com.poupa.vinylmusicplayer.appwidgets.base;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.kabouzeid.appthemehelper.util.MaterialValueHelper;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.discog.tagging.MultiValuesTagUtil;
import com.poupa.vinylmusicplayer.glide.GlideApp;
import com.poupa.vinylmusicplayer.glide.VinylGlideExtension;
import com.poupa.vinylmusicplayer.glide.VinylSimpleTarget;
import com.poupa.vinylmusicplayer.glide.palette.BitmapPaletteWrapper;
import com.poupa.vinylmusicplayer.helper.PendingIntentCompat;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.service.MusicService;
import com.poupa.vinylmusicplayer.ui.activities.MainActivity;
import com.poupa.vinylmusicplayer.util.ImageUtil;
import com.poupa.vinylmusicplayer.util.MusicUtil;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;

public abstract class BaseAppWidget extends AppWidgetProvider {
    public static final String NAME = "app_widget";

    protected Target target; // for cancellation
    protected RemoteViews appWidgetView;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onUpdate(final Context context, final AppWidgetManager appWidgetManager,
                         final int[] appWidgetIds) {
        defaultAppWidget(context, appWidgetIds);
        final Intent updateIntent = new Intent(MusicService.APP_WIDGET_UPDATE);
        updateIntent.putExtra(MusicService.EXTRA_APP_WIDGET_NAME, NAME);
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        updateIntent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
        context.sendBroadcast(updateIntent);
    }

    /**
     * Initialize given widgets to default state, where we launch Music on
     * default click and hide actions if service not running.
     */
    protected void defaultAppWidget(final Context context, final int[] appWidgetIds) {
        appWidgetView = new RemoteViews(context.getPackageName(), getLayout());

        setBackground(context.getResources());
        appWidgetView.setViewVisibility(R.id.media_titles, View.INVISIBLE);
        appWidgetView.setImageViewResource(R.id.image, R.drawable.default_album_art);
        appWidgetView.setImageViewBitmap(R.id.button_next, ImageUtil.createBitmap(ImageUtil.getTintedVectorDrawable(context, R.drawable.ic_skip_next_white_24dp, MaterialValueHelper.getSecondaryTextColor(context, true))));
        appWidgetView.setImageViewBitmap(R.id.button_prev, ImageUtil.createBitmap(ImageUtil.getTintedVectorDrawable(context, R.drawable.ic_skip_previous_white_24dp, MaterialValueHelper.getSecondaryTextColor(context, true))));
        appWidgetView.setImageViewBitmap(R.id.button_toggle_play_pause, ImageUtil.createBitmap(ImageUtil.getTintedVectorDrawable(context, R.drawable.ic_play_arrow_white_24dp, MaterialValueHelper.getSecondaryTextColor(context, true))));

        linkButtons(context);
        pushUpdate(context, appWidgetIds);
    }

    /**
     * Link up various button actions using {@link PendingIntent}.
     */
    protected void linkButtons(final Context context) {
        Intent action;
        PendingIntent pendingIntent;

        final ComponentName serviceName = new ComponentName(context, MusicService.class);

        // Home
        action = new Intent(context, MainActivity.class);
        action.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        pendingIntent = PendingIntentCompat.getActivity(context, 0, action, 0);
        appWidgetView.setOnClickPendingIntent(R.id.image, pendingIntent);
        appWidgetView.setOnClickPendingIntent(R.id.media_titles, pendingIntent);

        // Previous track
        pendingIntent = buildPendingIntent(context, MusicService.ACTION_REWIND, serviceName);
        appWidgetView.setOnClickPendingIntent(R.id.button_prev, pendingIntent);

        // Play and pause
        pendingIntent = buildPendingIntent(context, MusicService.ACTION_TOGGLE_PAUSE, serviceName);
        appWidgetView.setOnClickPendingIntent(R.id.button_toggle_play_pause, pendingIntent);

        // Next track
        pendingIntent = buildPendingIntent(context, MusicService.ACTION_SKIP, serviceName);
        appWidgetView.setOnClickPendingIntent(R.id.button_next, pendingIntent);
    }

    /**
     * Handle a change notification coming over from
     * {@link MusicService}
     */
    public void notifyChange(final MusicService service, final String what) {
        if (hasInstances(service)) {
            if (MusicService.META_CHANGED.equals(what) || MusicService.PLAY_STATE_CHANGED.equals(what)) {
                performUpdate(service, null);
            }
        }
    }

    protected void pushUpdate(final Context context, final int[] appWidgetIds) {
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        if (appWidgetIds != null) {
            appWidgetManager.updateAppWidget(appWidgetIds, appWidgetView);
        } else {
            appWidgetManager.updateAppWidget(new ComponentName(context, getClass()), appWidgetView);
        }
    }

    /**
     * Check against {@link AppWidgetManager} if there are any instances of this
     * widget.
     */
    protected boolean hasInstances(final Context context) {
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        final int[] mAppWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context,
                getClass()));
        return mAppWidgetIds.length > 0;
    }

    protected PendingIntent buildPendingIntent(Context context, final String action, final ComponentName serviceName) {
        Intent intent = new Intent(action);
        intent.setComponent(serviceName);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return PendingIntent.getForegroundService(context, 0, intent, 0);
        } else {
            return PendingIntent.getService(context, 0, intent, 0);
        }
    }

    protected static Bitmap createRoundedBitmap(Drawable drawable, int width, int height, float tl, float bl) {
        if (drawable == null) return null;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(c);

        Bitmap rounded = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(rounded);
        Paint paint = new Paint();
        paint.setShader(new BitmapShader(bitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
        paint.setAntiAlias(true);
        canvas.drawPath(composeRoundedRectPath(new RectF(0, 0, width, height), tl, 0f, bl, 0f), paint);

        return rounded;
    }

    protected static Path composeRoundedRectPath(RectF rect, float tl, float tr, float bl, float br) {
        Path path = new Path();
        tl = tl < 0 ? 0 : tl;
        tr = tr < 0 ? 0 : tr;
        bl = bl < 0 ? 0 : bl;
        br = br < 0 ? 0 : br;

        path.moveTo(rect.left + tl, rect.top);
        path.lineTo(rect.right - tr, rect.top);
        path.quadTo(rect.right, rect.top, rect.right, rect.top + tr);
        path.lineTo(rect.right, rect.bottom - br);
        path.quadTo(rect.right, rect.bottom, rect.right - br, rect.bottom);
        path.lineTo(rect.left + bl, rect.bottom);
        path.quadTo(rect.left, rect.bottom, rect.left, rect.bottom - bl);
        path.lineTo(rect.left, rect.top + tl);
        path.quadTo(rect.left, rect.top, rect.left + tl, rect.top);
        path.close();

        return path;
    }

    protected Drawable getAlbumArtDrawable(final Resources resources, final Bitmap bitmap) {
        Drawable image;
        if (bitmap == null) {
            image = resources.getDrawable(R.drawable.default_album_art);
        } else {
            image = new BitmapDrawable(resources, bitmap);
        }
        return image;
    }

    protected String getSongArtistAndAlbum(final Song song) {
        return MusicUtil.getSongInfoString(song);
    }

    protected void setBackground(final Resources resources) {
        if (PreferenceUtil.getInstance().transparentBackgroundWidget()) {
            appWidgetView.setInt(getId(), "setBackgroundResource", android.R.color.transparent);
        } else {
            int currentTheme = PreferenceUtil.getInstance().getGeneralTheme();
            if(currentTheme == R.style.Theme_VinylMusicPlayer || currentTheme == R.style.Theme_VinylMusicPlayer_Black){
                appWidgetView.setInt(getId(), "setBackgroundResource", R.color.cardview_dark_background);
                appWidgetView.setTextColor(R.id.title, resources.getColor(R.color.ate_primary_text_dark));
                appWidgetView.setTextColor(R.id.text, resources.getColor(R.color.ate_primary_text_dark));
            }else{
                appWidgetView.setInt(getId(), "setBackgroundResource", R.color.md_grey_50);
                appWidgetView.setTextColor(R.id.title, resources.getColor(R.color.ate_primary_text_light));
                appWidgetView.setTextColor(R.id.text, resources.getColor(R.color.ate_primary_text_light));
            }
        }
    }

    protected void loadAlbumCover(final MusicService service, final int[] appWidgetIds) {
        final Context appContext = service.getApplicationContext();

        service.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (target != null) {
                    GlideApp.with(appContext).clear(target);
                }
                final Song song = service.getCurrentSong();
                final boolean isPlaying = service.isPlaying();

                final int imageSize = getImageSize(service);

                target = GlideApp.with(appContext)
                        .asBitmapPalette()
                        .load(VinylGlideExtension.getSongModel(song))
                        .transition(VinylGlideExtension.getDefaultTransition())
                        .songOptions(song)
                        .into(new VinylSimpleTarget<BitmapPaletteWrapper>(imageSize, imageSize) {
                            @Override
                            public void onResourceReady(@NonNull BitmapPaletteWrapper resource, Transition<? super BitmapPaletteWrapper> glideAnimation) {
                                Palette palette = resource.getPalette();
                                update(resource.getBitmap(), palette.getVibrantColor(palette.getMutedColor(MaterialValueHelper.getSecondaryTextColor(appContext, true))));
                            }

                            @Override
                            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                super.onLoadFailed(errorDrawable);
                                update(null, MaterialValueHelper.getSecondaryTextColor(appContext, true));
                            }

                            private void update(@Nullable Bitmap bitmap, int color) {
                                final int imageSize = getImageSize(service);
                                final float cardRadius = getCardRadius(service);

                                // Set correct drawable for pause state
                                int playPauseRes = isPlaying ? R.drawable.ic_pause_white_24dp : R.drawable.ic_play_arrow_white_24dp;
                                appWidgetView.setImageViewBitmap(R.id.button_toggle_play_pause, ImageUtil.createBitmap(ImageUtil.getTintedVectorDrawable(appContext, playPauseRes, color)));

                                // Set prev/next button drawables
                                appWidgetView.setImageViewBitmap(R.id.button_next, ImageUtil.createBitmap(ImageUtil.getTintedVectorDrawable(appContext, R.drawable.ic_skip_next_white_24dp, color)));
                                appWidgetView.setImageViewBitmap(R.id.button_prev, ImageUtil.createBitmap(ImageUtil.getTintedVectorDrawable(appContext, R.drawable.ic_skip_previous_white_24dp, color)));

                                final Drawable image = getAlbumArtDrawable(appContext.getResources(), bitmap);
                                final Bitmap roundedBitmap = createRoundedBitmap(image, imageSize, imageSize, cardRadius, cardRadius);
                                appWidgetView.setImageViewBitmap(R.id.image, roundedBitmap);

                                setBackground(appContext.getResources());

                                pushUpdate(appContext, appWidgetIds);
                            }
                        });
            }
        });
    }

    protected void setTitlesArtwork(final MusicService service) {
        final Song song = service.getCurrentSong();
        if (TextUtils.isEmpty(song.title) && TextUtils.isEmpty(MultiValuesTagUtil.infoString(song.artistNames))) {
            appWidgetView.setViewVisibility(R.id.media_titles, View.INVISIBLE);
        } else {
            appWidgetView.setViewVisibility(R.id.media_titles, View.VISIBLE);
            appWidgetView.setTextViewText(R.id.title, song.title);
            appWidgetView.setTextViewText(R.id.text, getSongArtistAndAlbum(song));
        }
    }

    protected void setButtons(final MusicService service) {
        final boolean isPlaying = service.isPlaying();
        // Set correct drawable for pause state
        int playPauseRes = isPlaying ? R.drawable.ic_pause_white_24dp : R.drawable.ic_play_arrow_white_24dp;
        appWidgetView.setImageViewBitmap(R.id.button_toggle_play_pause, ImageUtil.createBitmap(ImageUtil.getTintedVectorDrawable(service, playPauseRes, MaterialValueHelper.getSecondaryTextColor(service, false))));

        // Set prev/next button drawables
        appWidgetView.setImageViewBitmap(R.id.button_next, ImageUtil.createBitmap(ImageUtil.getTintedVectorDrawable(service, R.drawable.ic_skip_next_white_24dp, MaterialValueHelper.getSecondaryTextColor(service, false))));
        appWidgetView.setImageViewBitmap(R.id.button_prev, ImageUtil.createBitmap(ImageUtil.getTintedVectorDrawable(service, R.drawable.ic_skip_previous_white_24dp, MaterialValueHelper.getSecondaryTextColor(service, false))));
    }

    public abstract int getImageSize(final MusicService service);

    public abstract float getCardRadius(final MusicService service);

    public abstract void performUpdate(final MusicService service, final int[] appWidgetIds);

    public abstract int getLayout();

    public abstract int getId();
}
