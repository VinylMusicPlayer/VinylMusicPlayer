package com.poupa.vinylmusicplayer.glide.audiocover;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.poupa.vinylmusicplayer.model.Song;

/**
 * @author Karim Abou Zeid (kabouzeid)
 * @author SC (soncaokim)
 */
public class SongCover {
    public final Song song;

    public SongCover(@NonNull final Song song) {
        this.song = song;
    }

    @Override
    public int hashCode() {
        return song.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object object) {
        if (object instanceof SongCover) {
            SongCover other = (SongCover) object;
            return song.equals(other.song);
        }
        return false;
    }
}
