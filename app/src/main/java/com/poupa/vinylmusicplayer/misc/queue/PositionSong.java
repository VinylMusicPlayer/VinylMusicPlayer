package com.poupa.vinylmusicplayer.misc.queue;


import com.poupa.vinylmusicplayer.discog.tagging.MultiValuesTagUtil;
import com.poupa.vinylmusicplayer.model.Song;


public class PositionSong {

    public Song song;
    public int position;

    public PositionSong(Song song, int position) {
        this.song = song;
        this.position = position;
    }

    @Override
    public String toString() {
        //return "{"+ position + ", " + song.toString() + "}";
        return "{"+ position + ", " + song.title + "}";
    }
}
