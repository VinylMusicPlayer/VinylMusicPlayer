package com.poupa.vinylmusicplayer.views;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;

/**
 * @author Lincoln (theduffmaster)
 * @author Adrien Poupa
 *
 * A custom {@link HorizontalScrollView} that is only useful as the child of a
 * {@link TouchInterceptFrameLayout}. Allows for the layout to disable and enable scrolling in
 * addition to being able to know when a user is and is not interacting with the scrolling view.
 *
 * Must have a {@link AutoTruncateTextView} as its only child.
 */
public class TouchInterceptHorizontalScrollView extends HorizontalScrollView {

    private GestureDetector mDetector;
    private boolean mIsScrolling = false;

    public static final String TAG = TouchInterceptHorizontalScrollView.class.getSimpleName();

    /** Delay before triggering {@link OnEndScrollListener#onEndScroll} */
    private static final int ON_END_SCROLL_DELAY = 1000;

    private long lastScrollUpdate = -1;
    private boolean scrollable;
    private Rect scrollViewRect;
    private OnEndScrollListener onEndScrollListener;

    // Whether user is interacting with this again and to cancel text retruncate
    private boolean cancel;

    public TouchInterceptHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        lastScrollUpdate = -1;
        scrollable = true;
        scrollViewRect = new Rect();
        setLongClickable(false);
        setTag(TouchInterceptHorizontalScrollView.TAG);
        setHorizontalScrollBarEnabled(false);
        mDetector = new GestureDetector(context, new GestureListener());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChild(getTouchInterceptTextView(), widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void measureChild(View child, int parentWidthMeasureSpec,
                                int parentHeightMeasureSpec) {
        ViewGroup.LayoutParams lp = child.getLayoutParams();

        final int horizontalPadding = getPaddingLeft() + getPaddingRight();
        final int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
                Math.max(0, MeasureSpec.getSize(parentWidthMeasureSpec) - horizontalPadding),
                MeasureSpec.UNSPECIFIED);

        final int childHeightMeasureSpec = getChildMeasureSpec(parentHeightMeasureSpec,
                getPaddingTop() + getPaddingBottom(), lp.height);
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    /**
     * @return Returns the child {@link AutoTruncateTextView}.
     */
    public AutoTruncateTextView getTouchInterceptTextView() {
        return (AutoTruncateTextView) this.getChildAt(0);
    }

    /**
     * Disables and enables scrolling.
     *
     * @param scrollable Whether the view should be scrollable.
     */
    public void setScrollable(boolean scrollable) {
        this.scrollable = scrollable;
    }

    /**
     * Returns whether the view is scrollable.
     *
     * @return Whether the view is scrollable.
     */
    public boolean isScrollable() {
        return scrollable;
    }

    /**
     * Intercept the touch event here, we arrive here since this is a HorizontalScrollView
     * Force onTouchEvent to be fired
     * @param e
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        onTouchEvent(e);
        return false;
    }

    /**
     * Handle touch events
     * @param e
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // Fire the detector
        mDetector.onTouchEvent(e);

        // Detect if we have a scroll that just finished
        if (e.getAction() == MotionEvent.ACTION_UP && mIsScrolling) {
            scrollFinished();
        }

        return super.onTouchEvent(e);
    }

    /**
     * Sets the {@link OnEndScrollListener}.
     *
     * @param onEndScrollListener The listener to be set.
     */
    public void setOnEndScrollListener(OnEndScrollListener onEndScrollListener) {
        this.onEndScrollListener = onEndScrollListener;
    }

    /**
     * User is done interacting with the scroll view
     */
    private void scrollFinished() {
        mIsScrolling  = false;
        cancel = false;
        postDelayed(new ScrollStateHandler(), ON_END_SCROLL_DELAY);
        lastScrollUpdate = System.currentTimeMillis();
    }

    interface OnEndScrollListener {
        /**
         * Triggered when a user has stopped interacting with the
         * {@link TouchInterceptHorizontalScrollView}.
         */
        void onEndScroll();
    }

    private class ScrollStateHandler implements Runnable {
        @Override
        public void run() {
            if (!cancel) {
                // Hasn't been touched for some time
                long currentTime = System.currentTimeMillis();
                if ((currentTime - lastScrollUpdate) > ON_END_SCROLL_DELAY) {
                    lastScrollUpdate = -1;
                    if (onEndScrollListener != null) {
                        onEndScrollListener.onEndScroll();
                    }
                } else {
                    postDelayed(this, ON_END_SCROLL_DELAY);
                }
            }
        }
    }

    public Rect getScrollViewRect() {
        getGlobalVisibleRect(scrollViewRect);
        return scrollViewRect;
    }

    /**
     * Gesture Listener
     */
    class GestureListener extends GestureDetector.SimpleOnGestureListener {
        /**
         * Tapping the scrollview
         * Here we go back all the way up to the framelayout that contains the song,
         * and we click manually to play it
         * @param e
         * @return
         */
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            playSong();

            return false;
        }

        /**
         * Pass back long presses (adding to playlist, etc)
         * @param e
         */
        @Override
        public void onLongPress(MotionEvent e) {
            playSong();
        }

        /**
         * Scrolling the scrollview
         * @param e1
         * @param e2
         * @param distanceX
         * @param distanceY
         * @return
         */
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            mIsScrolling = true;

            getTouchInterceptTextView().untruncateText();

            return false;
        }

        /**
         * Play song on tap
         */
        private void playSong() {
            TouchInterceptFrameLayout touchInterceptFrameLayout = getTouchInterceptTextView().getTouchInterceptFrameLayout();

            if (touchInterceptFrameLayout != null) {
                ((ViewGroup) touchInterceptFrameLayout.getParent()).performClick();
            }
        }
    }
}
