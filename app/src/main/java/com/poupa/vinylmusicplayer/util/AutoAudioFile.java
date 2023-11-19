package com.poupa.vinylmusicplayer.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jaudiotagger.audio.AudioFile;

// Utility class to auto-delete backing file used to create an volatile/temporary AudioFile object
public class AutoAudioFile implements AutoCloseable {
    @NonNull private final AudioFile audio;
    @Nullable
    private final AutoDeleteTempFile closable;

    private AutoAudioFile(@NonNull final AudioFile audio, @Nullable final AutoDeleteTempFile closable) {
        this.audio = audio;
        this.closable = closable;
    }

    public static AutoAudioFile create(@NonNull final AudioFile audio) {
        return new AutoAudioFile(audio, null);
    }

    public static AutoAudioFile createAutoDelete(@NonNull final AudioFile audio, @NonNull final AutoDeleteTempFile tempFile) {
        return new AutoAudioFile(audio, tempFile);
    }

    public void close() {
        if (closable != null) {
            closable.close();
        }
    }

    @NonNull public AudioFile get() {
        return audio;
    }
}
