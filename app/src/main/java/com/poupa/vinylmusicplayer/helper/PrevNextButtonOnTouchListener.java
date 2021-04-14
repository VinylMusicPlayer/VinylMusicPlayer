package com.poupa.vinylmusicplayer.helper;

import android.view.MotionEvent;
import android.view.View;
import android.os.Handler;
import android.view.View.OnClickListener;

public class PrevNextButtonOnTouchListener implements View.OnTouchListener {
    private Handler handler = new Handler();

    private int skipTriggerInitialIntervalMillis = 1000;
    private final int skipTriggerNormalIntervalMillis = 250;
    private final View.OnGenericMotionListener genericMotionListener;
    private View touchedView;
    private boolean wasHeld;

    private Runnable handlerRunnable = new Runnable() {
        @Override
        public void run() {
            if(touchedView.isEnabled()) {
                wasHeld = true;
                handler.postDelayed(this, skipTriggerNormalIntervalMillis);
                genericMotionListener.onGenericMotion(touchedView, MotionEvent.obtain(0,0,MotionEvent.ACTION_DOWN,0,0,0));
            } else {
                // if the view was disabled by the clickListener, remove the callback
                handler.removeCallbacks(handlerRunnable);
                touchedView.setPressed(false);
                touchedView = null;
                wasHeld = false;
            }
        }
    };

    public PrevNextButtonOnTouchListener(View.OnGenericMotionListener genericMotionListener) {
        this.genericMotionListener = genericMotionListener;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch(motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                handler.removeCallbacks(handlerRunnable);
                handler.postDelayed(handlerRunnable, skipTriggerInitialIntervalMillis);
                touchedView = view;
                touchedView.setPressed(true);
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if(!wasHeld) {
                    genericMotionListener.onGenericMotion(touchedView, MotionEvent.obtain(0,0,MotionEvent.ACTION_CANCEL,0,0,0));
                }
                handler.removeCallbacks(handlerRunnable);
                touchedView.setPressed(false);
                touchedView = null;
                wasHeld = false;
                return true;
        }
        return false;
    }
}