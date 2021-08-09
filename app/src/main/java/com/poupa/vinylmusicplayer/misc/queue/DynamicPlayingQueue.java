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
import com.poupa.vinylmusicplayer.model.Album;
import com.poupa.vinylmusicplayer.model.Song;


public class DynamicPlayingQueue extends StaticPlayingQueue {

    private Album nextAlbum; //test, will be replace by a interface for loading songs

    public DynamicPlayingQueue() {
        super();

        nextAlbum = new Album();
    }

    public DynamicPlayingQueue(ArrayList<IndexedSong> restoreQueue, ArrayList<IndexedSong> restoreOriginalQueue, int restoredPosition, int shuffleMode) {
        super(restoreQueue, restoreOriginalQueue, restoredPosition, shuffleMode);

        nextAlbum = new Album();
    }

    private void loadNextQueue() {
        this.queue.clear();
        addAll(nextAlbum.songs);
        this.songsIsStale = true;
    }

    private static final int NEXT_RANDOM_ALBUM_SONG_ID = -1;

    @NonNull
    private Song addNewRandomAlbum(long albumId, String albumName, long artistId, @NonNull List<String> artistNames) {
        return new Song(NEXT_RANDOM_ALBUM_SONG_ID, "Next Album: ", 0, -1, -1, "",
                -1, -1, albumId, albumName, artistId, artistNames);
    }

    public Song getPseudoSong() { //temporary
        return addNewRandomAlbum(this.nextAlbum.getId(), this.nextAlbum.getTitle(), this.nextAlbum.getArtistId(), MultiValuesTagUtil
                .split(this.nextAlbum.getArtistName()));
    }

    /*public void setNextQueue(Bundle criteria) {

    }*/

    // all method than modify queue should check if they need to call setNextQueue(criteria)
    public void add(Song song) {
        super.add(song);

        setNextQueue();
    }

    private void setNextQueue() {
        ArrayList<Album> albums;
        synchronized (Discography.getInstance()) {
            albums = new ArrayList<>(Discography.getInstance().getAllAlbums());
        }

        Random rand = new Random();
        this.nextAlbum = albums.get(rand.nextInt(albums.size()));
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

    public boolean isLastTrack() { // will this work, or will musicservice call staticplayingqueue ???
        return getCurrentPosition() == queue.size();
    }
}
