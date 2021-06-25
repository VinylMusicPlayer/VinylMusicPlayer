package com.poupa.vinylmusicplayer.service;

import com.poupa.vinylmusicplayer.helper.StopWatch;
import com.poupa.vinylmusicplayer.model.Song;

class SongPlayCountHelper {
    public static final String TAG = com.poupa.vinylmusicplayer.service.SongPlayCountHelper.class.getSimpleName();

    private final StopWatch stopWatch = new StopWatch();
    private Song song = Song.EMPTY_SONG;

    public Song getSong() {
        return song;
    }

    boolean shouldBumpPlayCount() {
        return song.duration * 0.5d < stopWatch.getElapsedTime();
    }

    void notifySongChanged(Song song) {
        synchronized (this) {
            stopWatch.reset();
            this.song = song;
        }
    }

    void notifyPlayStateChanged(boolean isPlaying) {
        synchronized (this) {
            if (isPlaying) {
                stopWatch.start();
            } else {
                stopWatch.pause();
            }
        }
    }
}
