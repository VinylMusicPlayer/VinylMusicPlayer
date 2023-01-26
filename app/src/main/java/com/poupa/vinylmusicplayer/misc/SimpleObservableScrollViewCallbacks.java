package com.poupa.vinylmusicplayer.misc;

import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public abstract class SimpleObservableScrollViewCallbacks implements ObservableScrollViewCallbacks {
    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {}

    @Override
    public void onDownMotionEvent() {}

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {}
}
