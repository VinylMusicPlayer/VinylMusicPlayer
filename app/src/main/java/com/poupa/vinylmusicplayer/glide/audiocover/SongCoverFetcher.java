package com.poupa.vinylmusicplayer.glide.audiocover;

import androidx.annotation.NonNull;

import com.bumptech.glide.Priority;
import com.poupa.vinylmusicplayer.App;
import com.poupa.vinylmusicplayer.util.AutoCloseAudioFile;
import com.poupa.vinylmusicplayer.util.OopsHandler;
import com.poupa.vinylmusicplayer.util.SAFUtil;

import org.jaudiotagger.tag.images.Artwork;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Karim Abou Zeid (kabouzeid)
 * @author SC (soncaokim)
 */
public class SongCoverFetcher extends AbsCoverFetcher {
    private final SongCover model;

    public SongCoverFetcher(SongCover model) {
        super();
        this.model = model;
    }

    @Override
    public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super InputStream> callback) {
        try (AutoCloseAudioFile audio = SAFUtil.loadReadOnlyAudioFile(App.getStaticContext(), model.song)) {
            if (audio == null) {
                callback.onLoadFailed(new IOException("Cannot load audio file"));
                return;
            }

            Artwork art = audio.get().getTag().getFirstArtwork();
            if (art != null) {
                byte[] imageData = art.getBinaryData();
                callback.onDataReady(new ByteArrayInputStream(imageData));
            } else {
                fallback(model.song.data, callback);
            }
        } catch (Exception e) {
            OopsHandler.collectStackTrace(e);
            callback.onLoadFailed(e);
        }
    }
}
