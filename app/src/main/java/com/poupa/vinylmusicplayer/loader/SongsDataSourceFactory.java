package com.poupa.vinylmusicplayer.loader;

import android.arch.paging.DataSource;
import android.support.v4.media.MediaBrowserCompat;

import com.poupa.vinylmusicplayer.model.Song;

public class SongsDataSourceFactory extends DataSource.Factory<Integer, Song> {

    private final MediaBrowserCompat mediaBrowser;

    SongsDataSourceFactory(MediaBrowserCompat mediaBrowser) {
        this.mediaBrowser = mediaBrowser;
    }

    @Override
    public DataSource<Integer, Song> create() {
        return new SongsDataSource(mediaBrowser);
    }
}