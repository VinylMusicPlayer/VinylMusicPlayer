package com.poupa.vinylmusicplayer.appwidgets;

import android.widget.RemoteViews;

import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.appwidgets.base.BaseAppWidget;
import com.poupa.vinylmusicplayer.service.MusicService;

public class AppWidgetCard extends BaseAppWidget {
    public static final String NAME = "app_widget_card";

    private static AppWidgetCard mInstance;

    public static synchronized AppWidgetCard getInstance() {
        if (mInstance == null) {
            mInstance = new AppWidgetCard();
        }
        return mInstance;
    }

    /**
     * Update all active widget instances by pushing changes
     */
    public void performUpdate(final MusicService service, final int[] appWidgetIds) {
        appWidgetView = new RemoteViews(service.getPackageName(), R.layout.app_widget_card);

        // Set the titles and artwork
        setTitlesArtwork(service);

        // Set the buttons
        setButtons(service);

        // Link actions buttons to intents
        linkButtons(service);

        // Load the album cover async and push the update on completion
        loadAlbumCover(service, appWidgetIds);
    }

    public int getLayout() {
        return R.id.app_widget_card;
    }

    public int getImageSize(final MusicService service) {
        return service.getResources().getDimensionPixelSize(R.dimen.app_widget_card_image_size);
    }

    public float getCardRadius(final MusicService service) {
        return service.getResources().getDimension(R.dimen.app_widget_card_radius);
    }
}
