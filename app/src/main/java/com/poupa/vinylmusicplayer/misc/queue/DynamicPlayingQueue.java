package com.poupa.vinylmusicplayer.misc.queue;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.discog.Discography;
import com.poupa.vinylmusicplayer.discog.tagging.MultiValuesTagUtil;
import com.poupa.vinylmusicplayer.misc.queue.AlbumShuffling.AlbumShufflingQueueLoader;
import com.poupa.vinylmusicplayer.model.Album;
import com.poupa.vinylmusicplayer.model.Song;


public class DynamicPlayingQueue extends StaticPlayingQueue {

    private DynamicQueueLoader queueLoader;

    public DynamicPlayingQueue() {
        super();

        queueLoader = new AlbumShufflingQueueLoader();
    }

    public DynamicPlayingQueue(ArrayList<IndexedSong> restoreQueue, ArrayList<IndexedSong> restoreOriginalQueue, int restoredPosition, int shuffleMode) {
        super(restoreQueue, restoreOriginalQueue, restoredPosition, shuffleMode);

        queueLoader = new AlbumShufflingQueueLoader();
    }

    private void loadNextQueue() {
        this.queue.clear();
        addAll(queueLoader.getNextQueue());
        this.songsIsStale = true;
    }

    public DynamicElement getDynamicElement() {
        return queueLoader.getDynamicElement();
    }

    public void setNextDynamicQueue(Bundle criteria) {
        queueLoader.setNextDynamicQueue(criteria);
    }

    // all method than modify queue should check if they need to call setNextQueue(criteria)
    public void add(Song song) {
        super.add(song);

        queueLoader.setNextDynamicQueue();
    }

    public int setCurrentPosition(int position) {
        if (position >= queue.size()) {
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
}
