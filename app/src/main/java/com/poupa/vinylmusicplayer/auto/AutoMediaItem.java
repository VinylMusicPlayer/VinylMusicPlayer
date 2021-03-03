package com.poupa.vinylmusicplayer.auto;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;

import androidx.annotation.NonNull;

import com.poupa.vinylmusicplayer.util.ImageUtil;

/**
 * @author SC (soncaokim)
 */

class AutoMediaItem {
    @NonNull
    static Builder with(@NonNull Context context) {
        return new Builder(context);
    }

    static class Builder {
        final Context mContext;
        MediaDescriptionCompat.Builder mBuilder;
        int mFlags = 0;

        Builder(@NonNull Context context) {
            mContext = context;
            mBuilder = new MediaDescriptionCompat.Builder();
        }

        @NonNull
        Builder path(@NonNull String fullPath) {
            mBuilder.setMediaId(fullPath);
            return this;
        }

        @NonNull
        Builder path(@NonNull String path, long id) {
            return path(AutoMediaIDHelper.createMediaID(String.valueOf(id), path));
        }

        @NonNull
        Builder title(@NonNull String title) {
            mBuilder.setTitle(title);
            return this;
        }

        @NonNull
        Builder subTitle(@NonNull String subTitle) {
            mBuilder.setSubtitle(subTitle);
            return this;
        }

        @NonNull
        Builder icon(Uri uri) {
            mBuilder.setIconUri(uri);
            return this;
        }

        @NonNull
        Builder icon(int iconDrawableId) {
            mBuilder.setIconBitmap(ImageUtil.createBitmap(ImageUtil.getVectorDrawable(
                    mContext.getResources(),
                    iconDrawableId,
                    mContext.getTheme()
            )));
            return this;
        }

        @NonNull
        Builder gridLayout(boolean isGrid) {
            // Hints - see https://developer.android.com/training/cars/media#default-content-style
            final String CONTENT_STYLE_SUPPORTED = "android.media.browse.CONTENT_STYLE_SUPPORTED";
            final String CONTENT_STYLE_BROWSABLE_HINT = "android.media.browse.CONTENT_STYLE_BROWSABLE_HINT";
            final String CONTENT_STYLE_PLAYABLE_HINT = "android.media.browse.CONTENT_STYLE_PLAYABLE_HINT";
            final int CONTENT_STYLE_LIST_ITEM_HINT_VALUE = 1;
            final int CONTENT_STYLE_GRID_ITEM_HINT_VALUE = 2;

            Bundle hints = new Bundle();
            hints.putBoolean(CONTENT_STYLE_SUPPORTED, true);
            hints.putInt(CONTENT_STYLE_BROWSABLE_HINT, isGrid ? CONTENT_STYLE_GRID_ITEM_HINT_VALUE : CONTENT_STYLE_LIST_ITEM_HINT_VALUE);
            hints.putInt(CONTENT_STYLE_PLAYABLE_HINT, isGrid ? CONTENT_STYLE_GRID_ITEM_HINT_VALUE : CONTENT_STYLE_LIST_ITEM_HINT_VALUE);

            mBuilder.setExtras(hints);
            return this;
        }

        @NonNull
        Builder asBrowsable() {
            mFlags |= MediaBrowserCompat.MediaItem.FLAG_BROWSABLE;
            return this;
        }

        @NonNull
        Builder asPlayable() {
            mFlags |= MediaBrowserCompat.MediaItem.FLAG_PLAYABLE;
            return this;
        }

        @NonNull
        MediaBrowserCompat.MediaItem build() {
            MediaBrowserCompat.MediaItem result = new MediaBrowserCompat.MediaItem(mBuilder.build(), mFlags);

            mBuilder = null;
            mFlags = 0;

            return result;
        }
    }
}
