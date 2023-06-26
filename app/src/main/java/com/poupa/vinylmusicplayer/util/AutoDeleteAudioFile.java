package com.poupa.vinylmusicplayer.util;

import androidx.annotation.NonNull;

import org.jaudiotagger.audio.AudioFile;

// Utility class to auto-delete backing file used to create an volatile/temporary AudioFile object
public class AutoDeleteAudioFile implements AutoCloseable {
    @NonNull private final AudioFile audio;

    public AutoDeleteAudioFile(@NonNull AudioFile audio) {
        this.audio = audio;
    }

    public void close() throws Exception {
        audio.getFile().delete();
    }

    @NonNull public AudioFile get() {
        return audio;
    }
}
