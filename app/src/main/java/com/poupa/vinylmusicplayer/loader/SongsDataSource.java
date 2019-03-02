package com.poupa.vinylmusicplayer.loader;

import android.arch.paging.PositionalDataSource;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;

import com.poupa.vinylmusicplayer.auto.AutoMediaIDHelper;
import com.poupa.vinylmusicplayer.model.Song;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SongsDataSource extends PositionalDataSource<Song> {

    private final MediaBrowserCompat mediaBrowser;
    private String rootId;
    private Set<Integer> loadedPages = new HashSet<>();

    SongsDataSource(MediaBrowserCompat mediaBrowser) {
        this.mediaBrowser = mediaBrowser;
    }

    @Override
    public void loadInitial(@NonNull final LoadInitialParams params, @NonNull final LoadInitialCallback<Song> callback) {
        String parentId = getParentId(params.requestedStartPosition);
        Bundle extra = getInitialPageBundle(params);
        mediaBrowser.subscribe(parentId, extra, new MediaBrowserCompat.SubscriptionCallback() {
            @Override
            public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children, @NonNull Bundle options) {
                loadedPages.add(0);
                List<Song> songs = MapToSongs(children);
                callback.onResult(songs, params.requestedStartPosition);
            }
        });
    }

    @Override
    public void loadRange(@NonNull final LoadRangeParams params, @NonNull final LoadRangeCallback<Song> callback) {
        final int pageIndex = getPageIndex(params);
        if (loadedPages.contains(pageIndex)) {
            callback.onResult(new ArrayList<>());
            return;
        }

        String parentId = getParentId(params.startPosition);
        Bundle extra = getRangeBundle(params);
        mediaBrowser.subscribe(parentId, extra, new MediaBrowserCompat.SubscriptionCallback() {
            @Override
            public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children, @NonNull Bundle options) {
                loadedPages.add(pageIndex);
                List<Song> songs = MapToSongs(children);
                callback.onResult(songs);
            }
        });
    }

    private int getPageIndex(LoadRangeParams params) {
        return params.startPosition / params.loadSize;
    }

    private Bundle getRangeBundle(LoadRangeParams params) {
        Bundle extra = new Bundle();
        extra.putInt(MediaBrowserCompat.EXTRA_PAGE, getPageIndex(params));
        extra.putInt(MediaBrowserCompat.EXTRA_PAGE_SIZE, params.loadSize);
        return extra;
    }

    @NonNull
    private Bundle getInitialPageBundle(@NonNull LoadInitialParams params) {
        Bundle extra = new Bundle();
        extra.putInt(MediaBrowserCompat.EXTRA_PAGE, 0);
        extra.putInt(MediaBrowserCompat.EXTRA_PAGE_SIZE, params.pageSize);
        return extra;
    }

    private List<Song> MapToSongs(List<MediaBrowserCompat.MediaItem> children) {
        List<Song> songs = new ArrayList<>();
        for (MediaBrowserCompat.MediaItem mediaItem : children) {
            Bundle songBundle = mediaItem.getDescription().getExtras();
            Song song = new Song((int) songBundle.get("id"), (String) songBundle.get("title"), (int) songBundle.get("trackNumber"),
                    (int) songBundle.get("year"), (long) songBundle.get("duration"), (String) songBundle.get("data"),
                    (long) songBundle.get("dateAdded"), (long) songBundle.get("dateModified"), (int) songBundle.get("albumId"),
                    (String) songBundle.get("albumName"), (int) songBundle.get("artistId"), (String) songBundle.get("artistName"));
            songs.add(song);
        }

        return songs;
    }

    private String getParentId(int requestedStartPosition) {
        if (rootId == null)
            //rootId = mediaBrowser.getRoot();
            rootId = new MediaBrowserServiceCompat.BrowserRoot(AutoMediaIDHelper.MEDIA_ID_ROOT, null).getRootId();

        return rootId + requestedStartPosition;
    }
}