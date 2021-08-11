package com.poupa.vinylmusicplayer.misc.queue;


import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import com.poupa.vinylmusicplayer.model.Song;


public interface DynamicQueueLoader {
    DynamicElement getDynamicElement(Context context);
    void setNextDynamicQueue(Song song, boolean force);
    void setNextDynamicQueue(Bundle criteria, Song song, boolean force);
    ArrayList<Song> getNextQueue();

    boolean restoreQueue(Context context, Song song);
}
