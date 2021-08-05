package com.poupa.vinylmusicplayer.misc.queue;


import com.poupa.vinylmusicplayer.model.Song;


public class IndexedSong { //IndexedSong

    public Song song;
    public int index;

    public IndexedSong(Song song, int index) {
        this.song = song;
        this.index = index;
    }

    @Override
    public String toString() {
        //return "{"+ index + ", " + song.toString() + "}";
        return "{"+ index + ", " + song.title + "}";
    }
}
