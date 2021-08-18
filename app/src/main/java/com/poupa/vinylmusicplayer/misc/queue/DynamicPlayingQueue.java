package com.poupa.vinylmusicplayer.misc.queue;


import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import com.poupa.vinylmusicplayer.model.Song;


public class DynamicPlayingQueue extends StaticPlayingQueue {

    private DynamicQueueLoader queueLoader;

    public DynamicPlayingQueue(DynamicQueueLoader queueLoader) {
        super();

        this.queueLoader = queueLoader;
    }

    public DynamicPlayingQueue(StaticPlayingQueue queue, DynamicQueueLoader queueLoader) {
        super(queue);

        this.queueLoader = queueLoader;
    }

    public DynamicPlayingQueue(DynamicPlayingQueue queue) {
        super(queue);

        queueLoader = queue.queueLoader;
    }

    public boolean restoreQueue(Context context, int restoredPosition) {
        return super.restoreQueue(context, restoredPosition) && queueLoader.restoreQueue(context, lastSong());
    }

    private void loadNextQueue() {
        clear();
        addAll(queueLoader.getNextQueue());
        this.songsIsStale = true;
    }

    private Song lastSong() {
        if (queue.size() != 0)
            return queue.get(queue.size()-1);
        return Song.EMPTY_SONG;
    }

    @NonNull
    public DynamicElement getDynamicElement(Context context) {
        return queueLoader.getDynamicElement(context);
    }

    public void setNextDynamicQueue(Bundle criteria, Context context) {
        queueLoader.setNextDynamicQueue(criteria, context, lastSong(), true);
    }

    public ArrayList<Song> getPlayingQueueSongOnly() {
        queueLoader.setNextDynamicQueue(null, lastSong(), false); // best way of updating this element (method is always called when queue is updated)
        return super.getPlayingQueueSongOnly();
    }

    public int setCurrentPosition(int position) {
        if (position >= queue.size()) {
            if (queueLoader.isNextQueueEmpty())
                return INVALID_POSITION;

            loadNextQueue();
            currentPosition = 0;
            return QUEUE_HAS_CHANGED;
        } else {
            currentPosition = position;
            return VALID_POSITION;
        }
    }

    public boolean isLastTrack() {
        return getCurrentPosition() == queue.size();
    }

    public DynamicQueueItemAdapter getAdapter() {
        return queueLoader.getAdapter();
    }
}
