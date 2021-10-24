package com.poupa.vinylmusicplayer.misc.queue;


import com.poupa.vinylmusicplayer.model.Song;


public class IndexedSong extends Song {

    public static final int INVALID_INDEX = -1;
    public static final IndexedSong EMPTY_INDEXED_SONG = new IndexedSong(Song.EMPTY_SONG, INVALID_INDEX, INVALID_INDEX);

    public int index;
    private long uniqueId;

    public IndexedSong(Song song, int index, long uniqueId) {
        super(song);
        this.index = index;

        this.uniqueId = uniqueId;
    }

    public boolean isQuickEqual(Song song) {
        boolean ret = super.isQuickEqual(song);

        if (getClass() == song.getClass()) {
            IndexedSong indexedSong = (IndexedSong) song;
            ret &= (indexedSong.index == INVALID_INDEX) || (this.index == indexedSong.index);
        }

        return ret;
    }

    @Override
    public String toString() {
        return "{"+ index + ", " + title + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        return (index == ((IndexedSong)o).index);
    }

    public void setUniqueId(long uniqueId) {
        this.uniqueId = uniqueId;
    }

    public long getUniqueId() {
        return uniqueId;
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + index;
    }
}
