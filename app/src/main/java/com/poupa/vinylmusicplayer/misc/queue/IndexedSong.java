package com.poupa.vinylmusicplayer.misc.queue;


import com.poupa.vinylmusicplayer.model.Song;


public class IndexedSong {

    public static final int INVALID_INDEX = -1;
    public static final IndexedSong EMPTY_INDEXED_SONG = new IndexedSong(Song.EMPTY_SONG, INVALID_INDEX);

    public Song song;
    public int index;

    public IndexedSong(Song song, int index) {
        this.song = song;
        this.index = index;
    }

    @Override
    public String toString() {
        return "{"+ index + ", " + song.title + "}";
    }
}
