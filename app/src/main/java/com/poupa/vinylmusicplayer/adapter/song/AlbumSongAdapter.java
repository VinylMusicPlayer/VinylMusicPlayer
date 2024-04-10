package com.poupa.vinylmusicplayer.adapter.song;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.discog.tagging.MultiValuesTagUtil;
import com.poupa.vinylmusicplayer.interfaces.CabHolder;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.util.MusicUtil;

import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class AlbumSongAdapter extends SongAdapter {

    public AlbumSongAdapter(AppCompatActivity activity, ArrayList<Song> dataSet, boolean usePalette, @Nullable CabHolder cabHolder) {
        super(activity, dataSet, R.layout.item_list, usePalette, cabHolder);
        this.showAlbumImage = false; // We don't want to load it in this adapter
    }

    @Override
    public void onBindViewHolder(@NonNull SongAdapter.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        final Song song = dataSet.get(position);

        if (holder.imageText != null) {
            final String trackNumberString = MusicUtil.getTrackNumberInfoString(song);
            holder.imageText.setText(trackNumberString);
        }
    }

    @Override
    @NonNull
    protected String getSongText(@NonNull final Song song) {
        return MusicUtil.buildInfoString(
                MusicUtil.getReadableDurationString(song.duration),
                MultiValuesTagUtil.infoStringAsArtists(song.artistNames));
    }
}
