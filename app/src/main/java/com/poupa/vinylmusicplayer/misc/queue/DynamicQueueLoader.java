package com.poupa.vinylmusicplayer.misc.queue;


import java.util.ArrayList;

import android.os.Bundle;

import com.poupa.vinylmusicplayer.model.Song;


public interface DynamicQueueLoader {
    DynamicElement getDynamicElement();
    void setNextDynamicQueue();
    void setNextDynamicQueue(Bundle criteria);
    ArrayList<Song> getNextQueue();
}
