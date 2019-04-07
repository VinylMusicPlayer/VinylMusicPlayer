package com.poupa.vinylmusicplayer.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * @author Lincoln (theduffmaster)
 * 
 * A custom {@link FrameLayout} that intercepts touch events and decides whether to consume them or
 * pass them on to a child {@link TouchInterceptHorizontalScrollView} and its
 * {@link AutoTruncateTextView}.
 *
 * This only needs to be used if the layout containing the {@link TouchInterceptHorizontalScrollView}
 * is clickable.
 */
public class TouchInterceptFrameLayout extends FrameLayout {

    public static final String TAG = TouchInterceptFrameLayout.class.getSimpleName();

    public TouchInterceptFrameLayout(@NonNull Context context) {
        this(context, null);
        init();
    }

    public TouchInterceptFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
        init();
    }

    public TouchInterceptFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setTag(TouchInterceptFrameLayout.TAG);
    }
}
