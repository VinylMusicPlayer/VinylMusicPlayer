package com.poupa.vinylmusicplayer.util;

import androidx.annotation.NonNull;

import org.jaudiotagger.audio.AudioFile;

// Utility class to auto-delete backing file used to create an volatile/temporary AudioFile object
public class AutoDeleteAudioFile implements AutoCloseable {
    @NonNull private final AudioFile audio;
    @NonNull private final AutoCloseable closable;

    public AutoDeleteAudioFile(@NonNull final AudioFile audio) {
        this.audio = audio;
        this.closable = new AutoDeleteTempFile(audio.getFile());
    }

    public void close() throws Exception {
        closable.close();
    }

    @NonNull public AudioFile get() {
        return audio;
    }
}
