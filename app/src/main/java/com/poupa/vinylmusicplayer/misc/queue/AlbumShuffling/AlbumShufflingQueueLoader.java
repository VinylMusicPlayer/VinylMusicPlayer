package com.poupa.vinylmusicplayer.misc.queue.AlbumShuffling;


import java.util.ArrayList;
import java.util.Random;

import android.os.Bundle;

import com.poupa.vinylmusicplayer.discog.Discography;
import com.poupa.vinylmusicplayer.misc.queue.DynamicElement;
import com.poupa.vinylmusicplayer.misc.queue.DynamicQueueLoader;
import com.poupa.vinylmusicplayer.model.Album;
import com.poupa.vinylmusicplayer.model.Song;


public class AlbumShufflingQueueLoader implements DynamicQueueLoader {
    public static final String SEARCH_TYPE = "search_type";

    public static final int RANDOM_SEARCH = 1;
    public static final int ARTIST_SEARCH = 2;
    public static final int GENRE_SEARCH = 3;

    private Album nextAlbum;

    public AlbumShufflingQueueLoader() {
        nextAlbum = new Album();
    }

    public void setNextDynamicQueue() {
        ArrayList<Album> albums;
        synchronized (Discography.getInstance()) {
            albums = new ArrayList<>(Discography.getInstance().getAllAlbums());
        }

        Random rand = new Random();
        this.nextAlbum = albums.get(rand.nextInt(albums.size()));
    }

    public DynamicElement getDynamicElement() { // Should call R string
        return new DynamicElement("Next Album: ", this.nextAlbum.getTitle(), "-");
    }

    private DynamicElement getEmptyDynamicElement() {
        return new DynamicElement("Next Album: ", "", "-");
    }

    public void setNextDynamicQueue(Bundle criteria) {
        setNextDynamicQueue(); //WIP
    }

    public ArrayList<Song> getNextQueue() {
        return nextAlbum.songs;
    }
}
