package com.poupa.vinylmusicplayer.glide.audiocover;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;

/**
 * @author SC (soncaokim)
 */
public class FileCover {
    public final File file;

    public FileCover(@NonNull final File file) {
        this.file = file;
    }

    @Override
    public int hashCode() {
        return file.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object object) {
        if (object instanceof FileCover) {
            FileCover other = (FileCover) object;
            return file.equals(other.file);
        }
        return false;
    }
}
