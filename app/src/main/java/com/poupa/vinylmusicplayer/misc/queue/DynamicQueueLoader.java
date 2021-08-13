package com.poupa.vinylmusicplayer.misc.queue;


import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import com.poupa.vinylmusicplayer.model.Song;


public interface DynamicQueueLoader {
    @NonNull
    DynamicElement getDynamicElement(Context context);

    void setNextDynamicQueue(Context context, Song song, boolean force);
    void setNextDynamicQueue(Bundle criteria, Context context, Song song, boolean force);

    ArrayList<Song> getNextQueue();
    boolean isNextQueueEmpty();

    boolean restoreQueue(Context context, Song song);
}
