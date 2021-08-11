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

        queueLoader = new AlbumShufflingQueueLoader(); // should not always be this one, setup a way to choose what loader I want

        queueLoader.setNextDynamicQueue(lastSong(), false);
    }

    public boolean restoreQueue(Context context, int restoredPosition) {
        return super.restoreQueue(context, restoredPosition) && queueLoader.restoreQueue(context, lastSong());
    }

    private void loadNextQueue() {
        this.queue.clear();
        addAll(queueLoader.getNextQueue());
        this.songsIsStale = true;
    }

    private Song lastSong() {
        if (queue.size() != 0)
            return queue.get(queue.size()-1);
        return Song.EMPTY_SONG;
    }

    public DynamicElement getDynamicElement(Context context) {
        return queueLoader.getDynamicElement(context);
    }

    public void setNextDynamicQueue(Bundle criteria) {
        queueLoader.setNextDynamicQueue(criteria, lastSong(), true);
    }

    public ArrayList<Song> getPlayingQueueSongOnly() {
        queueLoader.setNextDynamicQueue(lastSong(), false); // better way of updating this element (method is always called when queue is updated)
        return super.getPlayingQueueSongOnly();
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
