package com.poupa.vinylmusicplayer.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;

public class AutoDeleteTempFile implements AutoCloseable {
    @NonNull
    private final File file;

    protected AutoDeleteTempFile(@NonNull final File file) {
        this.file = file;
    }

    public static AutoDeleteTempFile create(@Nullable final String nameHint, @Nullable final String suffixHint) throws IOException {
        final String name = (nameHint != null) ? nameHint : "tmp-" + System.currentTimeMillis();
        final String suffix = (suffixHint != null) ? "." + suffixHint : "";

        File tempFile = File.createTempFile(name, suffix);
        tempFile.deleteOnExit();

        return new AutoDeleteTempFile(tempFile);
    }

    public void close() {
        file.delete();
    }

    public @NonNull File get() {
        return file;
    }
}
