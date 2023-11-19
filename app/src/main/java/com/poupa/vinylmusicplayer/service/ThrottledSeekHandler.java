package com.poupa.vinylmusicplayer.service;

import static com.poupa.vinylmusicplayer.service.MusicService.PLAY_STATE_CHANGED;

import android.os.Handler;

public class ThrottledSeekHandler implements Runnable {
    // milliseconds to throttle before calling run() to aggregate events
    private static final long THROTTLE = 100;
    private final MusicService mMusicService;
    private final Handler mHandler;

    public ThrottledSeekHandler(MusicService musicService, Handler handler) {
        mHandler = handler;
        mMusicService = musicService;
    }

    public void notifySeek() {
        synchronized (mMusicService) {
            if (mHandler.getLooper().getThread().isAlive()) {
                mHandler.removeCallbacks(this);
                mHandler.postDelayed(this, THROTTLE);
            }
        }
    }

    @Override
    public void run() {
        mMusicService.savePositionInTrack();
        mMusicService.notifyChange(PLAY_STATE_CHANGED);
        mMusicService.sendPublicIntent(PLAY_STATE_CHANGED); // for musixmatch synced lyrics
    }
}