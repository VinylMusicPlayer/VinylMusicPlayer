package com.poupa.vinylmusicplayer.glide.audiocover;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Priority;
import com.poupa.vinylmusicplayer.App;
import com.poupa.vinylmusicplayer.util.AutoCloseAudioFile;
import com.poupa.vinylmusicplayer.util.OopsHandler;
import com.poupa.vinylmusicplayer.util.SAFUtil;

import org.jaudiotagger.tag.Tag;
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
        final InputStream data = loadData();
        if (data == null) {
            callback.onLoadFailed(new IOException("Cannot load artwork"));
        } else {
            callback.onDataReady(data);
        }
    }

    @Nullable
    public InputStream loadData() {
        try (final AutoCloseAudioFile audio = SAFUtil.loadReadOnlyAudioFile(App.getStaticContext(), model.song)) {
            if (audio == null) {return null;}

            final Tag tags = audio.get().getTag();
            if (tags != null) {
                final Artwork art = tags.getFirstArtwork();
                if (art != null) {
                    final byte[] imageData = art.getBinaryData();
                    return new ByteArrayInputStream(imageData);
                }
            }
            return fallback(model.song.data);
        } catch (final Exception e) {
            OopsHandler.collectStackTrace(e);
            return null;
        }
    }
}
