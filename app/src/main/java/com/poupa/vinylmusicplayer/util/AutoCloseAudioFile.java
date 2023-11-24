package com.poupa.vinylmusicplayer.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jaudiotagger.audio.AudioFile;

// Utility class to auto-delete backing file used to create an volatile/temporary AudioFile object
public class AutoCloseAudioFile implements AutoCloseable {
    @NonNull private final AudioFile audio;
    @Nullable
    private final AutoDeleteTempFile closable;

    private AutoCloseAudioFile(@NonNull final AudioFile audio, @Nullable final AutoDeleteTempFile closable) {
        this.audio = audio;
        this.closable = closable;
    }

    public static AutoCloseAudioFile create(@NonNull final AudioFile audio) {
        return new AutoCloseAudioFile(audio, null);
    }

    public static AutoCloseAudioFile createAutoDelete(@NonNull final AudioFile audio, @NonNull final AutoDeleteTempFile tempFile) {
        return new AutoCloseAudioFile(audio, tempFile);
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
