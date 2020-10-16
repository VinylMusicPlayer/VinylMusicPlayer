package com.poupa.vinylmusicplayer.adapter.song;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.poupa.vinylmusicplayer.interfaces.CabHolder;
import com.poupa.vinylmusicplayer.model.Artist;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.util.MusicUtil;

import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class AlbumSongAdapter extends SongAdapter {

    public AlbumSongAdapter(AppCompatActivity activity, ArrayList<Song> dataSet, @LayoutRes int itemLayoutRes, boolean usePalette, @Nullable CabHolder cabHolder) {
        super(activity, dataSet, itemLayoutRes, usePalette, cabHolder);
        this.showAlbumImage = false; // We don't want to load it in this adapter
    }

    @Override
    public void onBindViewHolder(@NonNull SongAdapter.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        final Song song = dataSet.get(position);

        if (holder.imageText != null) {
            final int trackNumber = MusicUtil.getFixedTrackNumber(song.trackNumber);
            final String trackNumberString = trackNumber > 0 ? String.valueOf(trackNumber) : "-";
            holder.imageText.setText(trackNumberString);
        }
    }

    @Override
    protected String getSongText(Song song) {
        final String artist = MusicUtil.isArtistNameUnknown(song.artistName) ? Artist.UNKNOWN_ARTIST_DISPLAY_NAME : song.artistName;
        return MusicUtil.buildInfoString(
                MusicUtil.getReadableDurationString(song.duration),
                artist);
    }
}
