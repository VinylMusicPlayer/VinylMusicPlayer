package com.poupa.vinylmusicplayer.glide.audiocover;

import androidx.annotation.NonNull;

import com.bumptech.glide.Priority;
import com.poupa.vinylmusicplayer.util.OopsHandler;
import com.poupa.vinylmusicplayer.util.SAFUtil;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.tag.images.Artwork;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.MissingResourceException;

/**
 * @author SC (soncaokim)
 */
public class FileCoverFetcher extends AbsCoverFetcher {
    private final FileCover model;

    public FileCoverFetcher(FileCover model) {
        super();
        this.model = model;
    }

    @Override
    public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super InputStream> callback) {
        try {
            final AudioFile audio = SAFUtil.loadAudioFile(model.file);
            if (audio == null) {
                callback.onLoadFailed(new IOException("Cannot load audio file"));
                return;
            }

            final Artwork art = audio.getTag().getFirstArtwork();
            if (art != null) {
                byte[] imageData = art.getBinaryData();
                callback.onDataReady(new ByteArrayInputStream(imageData));
            } else {
                InputStream data = fallback(model.file);
                if (data != null) {callback.onDataReady(data);}
                else {callback.onLoadFailed(new MissingResourceException("No artwork", "", ""));}
            }
        } catch (Exception e) {
            OopsHandler.collectStackTrace(e);
            callback.onLoadFailed(e);
        }
    }
}
