package com.poupa.vinylmusicplayer.auto;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaSessionCompat;

import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.service.MusicService;
import com.poupa.vinylmusicplayer.util.PackageValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beesham Sarendranauth (Beesham)
 */
public class AutoMusicBrowserService extends MediaBrowserServiceCompat implements ServiceConnection {

    private CursorBasedMediaProvider cursorBasedMediaProvider;
    private AutoMusicProvider mMusicProvider;
    private PackageValidator mPackageValidator;
    private MediaSessionCompat mMediaSession;
    private MusicService mMusicService;

    //private MediaSessionCompat mediaSession;


    public AutoMusicBrowserService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //mediaSession = new MediaSessionCompat(getBaseContext(), null);
        //setSessionToken(mediaSession.getSessionToken());
        cursorBasedMediaProvider = new CursorBasedMediaProvider(this);
        mMusicProvider = new AutoMusicProvider(this);
        mPackageValidator = new PackageValidator(this);

        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(this);
    }

    private void createMediaSession() {
        setSessionToken(mMediaSession.getSessionToken());
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        // Check origin to ensure we're not allowing any arbitrary app to browse app contents
        if (!mPackageValidator.isCallerAllowed(this, clientPackageName, clientUid)) {
            // Request from an untrusted package: return an empty browser root
            return new MediaBrowserServiceCompat.BrowserRoot(AutoMediaIDHelper.MEDIA_ID_EMPTY_ROOT, null);
        }

        return new BrowserRoot(AutoMediaIDHelper.MEDIA_ID_ROOT, null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        result.detach();
        return;
    }

    @Override
    public void onLoadChildren(@NonNull final String parentId, @NonNull final Result<List<MediaBrowserCompat.MediaItem>> result, @NonNull Bundle options) {
        /*if (AutoMediaIDHelper.MEDIA_ID_EMPTY_ROOT.equals(parentId)) {
            result.sendResult(new ArrayList<>());
        } else if (mMusicProvider.isInitialized()) {
            result.sendResult(mMusicProvider.getChildren(parentId, getResources()));
        } else {
            result.detach();
            mMusicProvider.retrieveMediaAsync(success -> result.sendResult(mMusicProvider.getChildren(parentId, getResources())));
        }*/

        result.detach();
        if (!(options.containsKey(MediaBrowserCompat.EXTRA_PAGE) && options.containsKey(MediaBrowserCompat.EXTRA_PAGE_SIZE)))
            return;

        /*if (mMusicProvider == null)
            mediaProvider = mediaProviderFactory.provide();*/

        int page = options.getInt(MediaBrowserCompat.EXTRA_PAGE);
        int pageSize = options.getInt(MediaBrowserCompat.EXTRA_PAGE_SIZE);
        List<Song> songs = getSongsPage(page, pageSize);

        List<MediaBrowserCompat.MediaItem> mediaItems = mapToMediaItems(songs);
        result.sendResult(mediaItems);
    }

    private List<Song> getSongsPage(int page, int pageSize) {
        int startPosition = page * pageSize;
        if (startPosition + pageSize <= cursorBasedMediaProvider.getMediaSize())
            return cursorBasedMediaProvider.getSongsAtRange(startPosition, startPosition + pageSize);
        else
            return cursorBasedMediaProvider.getSongsAtRange(startPosition, cursorBasedMediaProvider.getMediaSize());
    }

    private List<MediaBrowserCompat.MediaItem> mapToMediaItems(List<Song> songs) {
        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();
        for (Song song : songs) {
            Bundle songBundle = new Bundle();
            songBundle.putInt("id", song.id);
            songBundle.putString("title", song.title);
            songBundle.putInt("trackNumber", song.trackNumber);
            songBundle.putInt("year", song.year);
            songBundle.putLong("duration", song.duration);
            songBundle.putString("data", song.data);
            songBundle.putLong("dateAdded", song.dateAdded);
            songBundle.putLong("dateModified", song.dateModified);
            songBundle.putInt("albumId", song.albumId);
            songBundle.putString("albumName", song.albumName);
            songBundle.putInt("artistId", song.artistId);
            songBundle.putString("artistName", song.artistName);

            MediaDescriptionCompat mediaDescription = new MediaDescriptionCompat.Builder()
                    .setTitle(song.title)
                    .setMediaId(song.data)
                    .setExtras(songBundle)
                    .build();
            MediaBrowserCompat.MediaItem mediaItem = new MediaBrowserCompat.MediaItem(mediaDescription, 0);
            mediaItems.add(mediaItem);
        }

        return mediaItems;
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder service) {
        MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
        mMusicService = binder.getService();
        mMediaSession = mMusicService.getMediaSession();
        createMediaSession();
        mMusicProvider.setMusicService(mMusicService);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        mMusicService = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (SERVICE_INTERFACE.equals(intent.getAction())) {
            return super.onBind(intent);
        }
        else {
            return null;
        }
    }
}
