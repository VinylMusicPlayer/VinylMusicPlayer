package com.poupa.vinylmusicplayer.glide.audiocover;

import androidx.annotation.Nullable;

import com.poupa.vinylmusicplayer.glide.artistimage.ArtistImage;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class AudioFileCover {
    public final String filePath;

    public AudioFileCover(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public int hashCode() {
        return filePath.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object object) {
        if (object instanceof AudioFileCover) {
            AudioFileCover other = (AudioFileCover) object;
            return filePath.equals(other.filePath);
        }
        return false;
    }
}
