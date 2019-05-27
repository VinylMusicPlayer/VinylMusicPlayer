package com.poupa.vinylmusicplayer.glide.palette;

import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.bumptech.glide.request.target.ImageViewTarget;

public class BitmapPaletteTarget extends ImageViewTarget<BitmapPaletteWrapper> {
    public BitmapPaletteTarget(ImageView view) {
        super(view);
    }

    @Override
    protected void setResource(@Nullable BitmapPaletteWrapper bitmapPaletteWrapper) {
        if (bitmapPaletteWrapper != null) {
            view.setImageBitmap(bitmapPaletteWrapper.getBitmap());
        }
    }
}
