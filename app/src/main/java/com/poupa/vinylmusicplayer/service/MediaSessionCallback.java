package com.poupa.vinylmusicplayer.service;

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
import com.poupa.vinylmusicplayer.loader.PlaylistLoader;
import com.poupa.vinylmusicplayer.loader.SongLoader;
import com.poupa.vinylmusicplayer.loader.TopAndRecentlyPlayedTracksLoader;
import com.poupa.vinylmusicplayer.model.Album;
import com.poupa.vinylmusicplayer.model.Artist;
import com.poupa.vinylmusicplayer.model.Playlist;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.util.MusicUtil;

import java.util.ArrayList;
import java.util.List;

import static com.poupa.vinylmusicplayer.service.MusicService.CYCLE_REPEAT;
import static com.poupa.vinylmusicplayer.service.MusicService.TAG;
import static com.poupa.vinylmusicplayer.service.MusicService.TOGGLE_FAVORITE;
import static com.poupa.vinylmusicplayer.service.MusicService.TOGGLE_SHUFFLE;

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
        final long itemId = musicId != null ? Long.parseLong(musicId) : -1;
        final ArrayList<Song> songs = new ArrayList<>();

        final String category = AutoMediaIDHelper.extractCategory(mediaId);
        switch (category) {
            case AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_ALBUM:
                Album album = AlbumLoader.getAlbum(itemId);
                songs.addAll(album.songs);
                musicService.openQueue(songs, 0, true);
                break;

            case AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_ARTIST:
                Artist artist = ArtistLoader.getArtist(itemId);
                songs.addAll(artist.getSongs());
                musicService.openQueue(songs, 0, true);
                break;

            case AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_PLAYLIST:
                Playlist playlist = PlaylistLoader.getPlaylist(context, itemId);
                songs.addAll(playlist.getSongs(context));
                musicService.openQueue(songs, 0, true);
                break;

            case AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_LAST_ADDED:
            case AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_HISTORY:
            case AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_NOT_RECENTLY_PLAYED:
            case AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_TOP_TRACKS:
            case AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_QUEUE:
                List<Song> tracks;
                if (category.equals(AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_LAST_ADDED)) {
                    tracks = LastAddedLoader.getLastAddedSongs();
                } else if (category.equals(AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_HISTORY)) {
                    tracks = TopAndRecentlyPlayedTracksLoader.getRecentlyPlayedTracks(context);
                } else if (category.equals(AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_NOT_RECENTLY_PLAYED)) {
                    tracks = TopAndRecentlyPlayedTracksLoader.getNotRecentlyPlayedTracks(context);
                } else if (category.equals(AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_TOP_TRACKS)) {
                    tracks = TopAndRecentlyPlayedTracksLoader.getTopTracks(context);
                } else {
                    tracks = musicService.getPlayingQueue();
                }
                songs.addAll(tracks);
                int songIndex = MusicUtil.indexOfSongInList(tracks, itemId);
                if (songIndex == -1) {
                    songIndex = 0;
                }
                musicService.openQueue(songs, songIndex, true);
                break;

            case AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_SHUFFLE:
                ArrayList<Song> allSongs = Discography.getInstance().getAllSongs();
                ShuffleHelper.makeShuffleList(allSongs, -1);
                musicService.openQueue(allSongs, 0, true);
                break;

            default:
                break;
        }

        musicService.play();
    }

    /**
     * Inspired by https://developer.android.com/guide/topics/media-apps/interacting-with-assistant
     * @param query
     * @param extras
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
