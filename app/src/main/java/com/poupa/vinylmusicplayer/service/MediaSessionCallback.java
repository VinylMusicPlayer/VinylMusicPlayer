package com.poupa.vinylmusicplayer.service;

import static com.poupa.vinylmusicplayer.service.MusicService.CYCLE_REPEAT;
import static com.poupa.vinylmusicplayer.service.MusicService.TAG;
import static com.poupa.vinylmusicplayer.service.MusicService.TOGGLE_FAVORITE;
import static com.poupa.vinylmusicplayer.service.MusicService.TOGGLE_SHUFFLE;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.media.session.MediaSessionCompat;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.poupa.vinylmusicplayer.auto.AutoMediaIDHelper;
import com.poupa.vinylmusicplayer.discog.Discography;
import com.poupa.vinylmusicplayer.helper.ShuffleHelper;
import com.poupa.vinylmusicplayer.loader.AlbumLoader;
import com.poupa.vinylmusicplayer.loader.ArtistLoader;
import com.poupa.vinylmusicplayer.loader.LastAddedLoader;
import com.poupa.vinylmusicplayer.loader.SongLoader;
import com.poupa.vinylmusicplayer.loader.TopAndRecentlyPlayedTracksLoader;
import com.poupa.vinylmusicplayer.model.Album;
import com.poupa.vinylmusicplayer.model.Artist;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.provider.StaticPlaylist;
import com.poupa.vinylmusicplayer.util.MusicUtil;

import java.util.ArrayList;

public final class MediaSessionCallback extends MediaSessionCompat.Callback {

    private final Context context;
    private final MusicService musicService;

    MediaSessionCallback(MusicService musicService, Context context) {
        this.context = context;
        this.musicService = musicService;
    }

    @Override
    public void onPlay() {
        musicService.play();
    }

    @Override
    public void onPlayFromMediaId(String mediaId, Bundle extras) {
        super.onPlayFromMediaId(mediaId, extras);

        final String musicId = AutoMediaIDHelper.extractMusicID(mediaId);
        final long itemId = !TextUtils.isEmpty(musicId) ? Long.parseLong(musicId) : -1;
        final String category = AutoMediaIDHelper.extractCategory(mediaId);

        final ArrayList<Song> songs = new ArrayList<>();
        int startPosition = 0;

        if (category.equals(AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_SHUFFLE)) {
            songs.addAll(Discography.getInstance().getAllSongs(null));
            ShuffleHelper.makeShuffleList(songs, -1);
        } else {
            if (category.startsWith(AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_QUEUE)) {
                songs.addAll(musicService.getPlayingQueue());
            } else if (category.startsWith(AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_LAST_ADDED)) {
                songs.addAll(LastAddedLoader.getLastAddedSongs());
            } else if (category.startsWith(AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_HISTORY)) {
                songs.addAll(TopAndRecentlyPlayedTracksLoader.getRecentlyPlayedTracks(context));
            } else if (category.startsWith(AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_NOT_RECENTLY_PLAYED)) {
                songs.addAll(TopAndRecentlyPlayedTracksLoader.getNotRecentlyPlayedTracks(context));
            } else if (category.startsWith(AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_TOP_TRACKS)) {
                songs.addAll(TopAndRecentlyPlayedTracksLoader.getTopTracks(context));
            } else if (category.startsWith(AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_PLAYLIST)) {
                final String playlistIdStr = AutoMediaIDHelper.extractSubCategoryFromCategory(category);
                final long playlistId = !TextUtils.isEmpty(playlistIdStr) ? Long.parseLong(playlistIdStr) : -1;
                final StaticPlaylist playlist = StaticPlaylist.getPlaylist(playlistId);
                if (playlist != null) {
                    songs.addAll(playlist.asSongs());
                }
            } else if (category.startsWith(AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_ALBUM)) {
                final String albumIdStr = AutoMediaIDHelper.extractSubCategoryFromCategory(category);
                final long albumId = !TextUtils.isEmpty(albumIdStr) ? Long.parseLong(albumIdStr) : -1;
                Album album = AlbumLoader.getAlbum(albumId);
                songs.addAll(album.songs);
            }
            startPosition = Math.max(MusicUtil.indexOfSongInList(songs, itemId), 0);
        }

        musicService.openQueue(songs, startPosition, true);
        musicService.play();
    }

    /**
     * Inspired by <a href="https://developer.android.com/guide/topics/media-apps/interacting-with-assistant">...</a>
     */
    @Override
    public void onPlayFromSearch(String query, Bundle extras) {
        final ArrayList<Song> songs = new ArrayList<>();

        if (TextUtils.isEmpty(query)) {
            songs.addAll(SongLoader.getAllSongs());
        } else {
            // Build a queue based on songs that match "query" or "extras" param
            String mediaFocus = extras.getString(MediaStore.EXTRA_MEDIA_FOCUS);
            if (TextUtils.equals(mediaFocus,
                    MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE)) {
                String artistQuery = extras.getString(MediaStore.EXTRA_MEDIA_ARTIST);
                ArrayList<Artist> artists = ArtistLoader.getArtists(artistQuery);
                if (artists.size() > 0) {
                    Artist artist = artists.get(0);
                    songs.addAll(artist.getSongs());
                }
            } else if (TextUtils.equals(mediaFocus,
                    MediaStore.Audio.Albums.ENTRY_CONTENT_TYPE)) {
                String albumQuery = extras.getString(MediaStore.EXTRA_MEDIA_ALBUM);
                ArrayList<Album> albums = AlbumLoader.getAlbums(albumQuery);
                if (albums.size() > 0) {
                    Album album = albums.get(0);
                    songs.addAll(album.songs);
                }
            }
        }

        // Search by title
        if (songs.size() == 0) {
            songs.addAll(SongLoader.getSongs(query));
        }

        musicService.openQueue(songs, 0, true);

        musicService.play();
    }

    @Override
    public void onPause() {
        musicService.pause();
    }

    @Override
    public void onSkipToNext() {
        musicService.playNextSong(true);
    }

    @Override
    public void onSkipToPrevious() {
        musicService.back(true);
    }

    @Override
    public void onStop() {
        musicService.quit();
    }

    @Override
    public void onSeekTo(long pos) {
        musicService.seek((int) pos);
    }

    @Override
    public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
        return MediaButtonIntentReceiver.handleIntent(context, mediaButtonEvent);
    }

    @Override
    public void onCustomAction(@NonNull String action, Bundle extras) {
        switch (action) {
            case CYCLE_REPEAT:
                musicService.cycleRepeatMode();
                musicService.updateMediaSessionPlaybackState();
                break;

            case TOGGLE_SHUFFLE:
                musicService.toggleShuffle();
                musicService.updateMediaSessionPlaybackState();
                break;

            case TOGGLE_FAVORITE:
                MusicUtil.toggleFavorite(context, musicService.getCurrentSong());
                musicService.updateMediaSessionPlaybackState();
                break;

            default:
                Log.d(TAG, "Unsupported action: " + action);
                break;
        }
    }
}
