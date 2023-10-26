package com.poupa.vinylmusicplayer.glide.audiocover;

import androidx.annotation.NonNull;

import com.bumptech.glide.Priority;
import com.poupa.vinylmusicplayer.util.AutoDeleteAudioFile;
import com.poupa.vinylmusicplayer.util.SAFUtil;

import org.jaudiotagger.tag.images.Artwork;

import java.io.ByteArrayInputStream;
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
        try (AutoDeleteAudioFile audio = SAFUtil.loadAudioFile(model.file)) {
            Artwork art = audio.get().getTag().getFirstArtwork();
            if (art != null) {
                byte[] imageData = art.getBinaryData();
                callback.onDataReady(new ByteArrayInputStream(imageData));
            } else {
                InputStream data = fallback(model.file);
                if (data != null) {callback.onDataReady(data);}
                else {callback.onLoadFailed(new MissingResourceException("No artwork", "", ""));}
            }
        } catch (Exception e) {
            callback.onLoadFailed(e);
        }
    }
}
