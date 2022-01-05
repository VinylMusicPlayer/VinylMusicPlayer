package com.poupa.vinylmusicplayer.misc.queue;


import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import com.poupa.vinylmusicplayer.model.Song;


/** Provide all methods needed by {@link com.poupa.vinylmusicplayer.misc.queue.DynamicPlayingQueue} to get/set dynamic element or restore it */
public interface DynamicQueueLoader {

    DynamicQueueItemAdapter getAdapter();
    ArrayList<Song> getNextQueue();
    boolean isNextQueueEmpty();

    @NonNull
    DynamicElement getDynamicElement(Context context);

    /** Search for a new dynamic element
     * @param song the song will be used as criteria for the search
     * @param force set to true to ensure dynamic element will be updated
     * @return was dynamic element updated
     */
    boolean setNextDynamicQueue(Context context, Song song, boolean force);

    /** Search for a new dynamic element
     * @param criteria what parameters of the song need to be check
     * @param song the song will be used as criteria for the search
     * @param force set to true to ensure dynamic element will be updated
     * @return was dynamic element updated
     */
    boolean setNextDynamicQueue(Bundle criteria, Context context, Song song, boolean force);

    /** Restore loader
     * @param song last song used in setNextDynamicQueue
     */
    boolean restoreQueue(Context context, Song song);
}
