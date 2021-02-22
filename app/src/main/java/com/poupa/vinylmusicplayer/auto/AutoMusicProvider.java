package com.poupa.vinylmusicplayer.auto;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;

import androidx.annotation.Nullable;

import com.kabouzeid.appthemehelper.ThemeStore;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.discog.tagging.MultiValuesTagUtil;
import com.poupa.vinylmusicplayer.loader.AlbumLoader;
import com.poupa.vinylmusicplayer.loader.ArtistLoader;
import com.poupa.vinylmusicplayer.loader.LastAddedLoader;
import com.poupa.vinylmusicplayer.loader.PlaylistLoader;
import com.poupa.vinylmusicplayer.loader.TopAndRecentlyPlayedTracksLoader;
import com.poupa.vinylmusicplayer.model.Album;
import com.poupa.vinylmusicplayer.model.Artist;
import com.poupa.vinylmusicplayer.model.Playlist;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.service.MusicService;
import com.poupa.vinylmusicplayer.util.ImageUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Supplier;

/**
 * Created by Beesham Sarendranauth (Beesham)
 */
public class AutoMusicProvider {
    private static final String BASE_URI = "androidauto://vinyl";
    private static final int PATH_SEGMENT_ID = 0;
    private static final int PATH_SEGMENT_TITLE = 1;
    private static final int PATH_SEGMENT_ARTIST = 2;
    private static final int PATH_SEGMENT_ALBUM_ID = 3;

    private final WeakReference<MusicService> mMusicService;

    // Categorized caches for music data
    private ConcurrentMap<Integer, Uri> mMusicListByPlaylist;
    private ConcurrentMap<Integer, Uri> mMusicListByAlbum;
    private ConcurrentMap<Integer, Uri> mMusicListByArtist;

    private final Context mContext;
    private volatile State mCurrentState = State.NON_INITIALIZED;

    // This is a hack - for some reason the listing of big list takes forever
    // for Queue and Playlists, but not for Albums and Artist
    // so instead we deliver only a portion of it to Auto
    private final int LISTING_SIZE_LIMIT = 1000;

    public AutoMusicProvider(MusicService musicService) {
        mContext = musicService;
        mMusicService = new WeakReference<>(musicService);

        mMusicListByPlaylist = new ConcurrentSkipListMap<>();
        mMusicListByAlbum = new ConcurrentSkipListMap<>();
        mMusicListByArtist = new ConcurrentSkipListMap<>();
    }

    private Iterable<Uri> getPlaylists() {
        if (mCurrentState != State.INITIALIZED) {
            return Collections.emptyList();
        }
        return mMusicListByPlaylist.values();
    }

    private Iterable<Uri> getAlbums() {
        if (mCurrentState != State.INITIALIZED) {
            return Collections.emptyList();
        }
        return mMusicListByAlbum.values();
    }

    private Iterable<Uri> getArtists() {
        if (mCurrentState != State.INITIALIZED) {
            return Collections.emptyList();
        }
        return mMusicListByArtist.values();
    }

    private Iterable<Uri> getQueue() {
        if (mCurrentState != State.INITIALIZED) {
            return Collections.emptyList();
        }

        ConcurrentMap<Integer, Uri> queueList = new ConcurrentSkipListMap<>();

        final MusicService service = mMusicService.get();
        if (service != null) {
            // Only show the queue starting from the currently played song
            final List<Song> queue = service.getPlayingQueue();
            final int fromPosition = Math.max(0, service.getPosition());
            final int toPosition = Math.min(queue.size() - 1, fromPosition + LISTING_SIZE_LIMIT);
            final List<Song> songs = queue.subList(fromPosition, toPosition);

            for (int i = 0; i < songs.size(); i++) {
                final Song s = songs.get(i);
                Uri.Builder songData = Uri.parse(BASE_URI).buildUpon();
                songData.appendPath(String.valueOf(s.id))
                        .appendPath(s.title)
                        .appendPath(MultiValuesTagUtil.infoString(s.artistNames))
                        .appendPath(String.valueOf(s.albumId));
                queueList.putIfAbsent(i, songData.build());
            }
        }

        return queueList.values();
    }

    public boolean isInitialized() {
        return mCurrentState == State.INITIALIZED;
    }

    /**
     * Get the list of music tracks from a server and caches the track information
     * for future reference, keying tracks by musicId
     */
    public void retrieveMediaAsync(final Callback callback) {
        if (mCurrentState == State.INITIALIZED) {
            if (callback != null) {
                // Nothing to do, execute callback immediately
                callback.onMusicCatalogReady(true);
            }
            return;
        }

        // Asynchronously load the music catalog in a separate thread
        new AsyncTask<Void, Void, State>() {
            @Override
            protected State doInBackground(Void... params) {
                retrieveMedia();
                return mCurrentState;
            }

            @Override
            protected void onPostExecute(State current) {
                if (callback != null) {
                    callback.onMusicCatalogReady(current == State.INITIALIZED);
                }
            }
        }.execute();
    }

    private synchronized ConcurrentMap<Integer, Uri> buildListByLoader(Supplier<List<Song>> loader) {
        ConcurrentMap<Integer, Uri> result = new ConcurrentHashMap<>();
        if (mCurrentState != State.INITIALIZED) {
            return result;
        }

        final List<Song> allSongs = loader.get();
        final int fromPosition = 0;
        final int toPosition = Math.min(allSongs.size() - 1, fromPosition + LISTING_SIZE_LIMIT);
        final List<Song> songs = allSongs.subList(fromPosition, toPosition);

        for (int i = 0; i < songs.size(); i++) {
            final Song s = songs.get(i);
            Uri.Builder songData = Uri.parse(BASE_URI).buildUpon();
            songData.appendPath(String.valueOf(s.id))
                    .appendPath(s.title)
                    .appendPath(MultiValuesTagUtil.infoString(s.artistNames))
                    .appendPath(String.valueOf(s.albumId));
            result.putIfAbsent(i, songData.build());
        }

        return result;
    }

    private synchronized void buildListsByPlaylist() {
        ConcurrentMap<Integer, Uri> newMusicListByPlaylist = new ConcurrentSkipListMap<>();

        final List<Playlist> playlists = PlaylistLoader.getAllPlaylists(mContext);
        for (int i = 0; i < playlists.size(); i++) {
            final Playlist p = playlists.get(i);
            Uri.Builder playlistData = Uri.parse(BASE_URI).buildUpon();
            playlistData.appendPath(String.valueOf(p.id))
                    .appendPath(p.name);
            newMusicListByPlaylist.putIfAbsent(i, playlistData.build());
        }

        mMusicListByPlaylist = newMusicListByPlaylist;
    }

    private synchronized void buildListsByAlbum() {
        ConcurrentMap<Integer, Uri> newMusicListByAlbum = new ConcurrentSkipListMap<>();

        final List<Album> albums = AlbumLoader.getAllAlbums();
        for (int i = 0; i < albums.size(); i++) {
            final Album a = albums.get(i);
            Uri.Builder albumData = Uri.parse(BASE_URI).buildUpon();
            albumData.appendPath(String.valueOf(a.getId()))
                    .appendPath(a.getTitle())
                    .appendPath(a.getArtistName())
                    .appendPath(String.valueOf(a.getId()));
            newMusicListByAlbum.putIfAbsent(i, albumData.build());
        }

        mMusicListByAlbum = newMusicListByAlbum;
    }

    private synchronized void buildListsByArtist() {
        ConcurrentMap<Integer, Uri> newMusicListByArtist = new ConcurrentSkipListMap<>();

        final List<Artist> artists = ArtistLoader.getAllArtists();
        for (int i = 0; i < artists.size(); i++) {
            final Artist a = artists.get(i);
            Uri.Builder artistData = Uri.parse(BASE_URI).buildUpon();
            artistData.appendPath(String.valueOf(a.getId()))
                    .appendPath(a.getName())
                    .appendPath(a.getName());
            newMusicListByArtist.putIfAbsent(i, artistData.build());
        }

        mMusicListByArtist = newMusicListByArtist;
    }

    private synchronized void retrieveMedia() {
        try {
            if (mCurrentState == State.NON_INITIALIZED) {
                mCurrentState = State.INITIALIZING;

                // Note: The smart playlists and the queue are not prebuilt and cached here,
                // since their content may change frequently

                buildListsByPlaylist();
                buildListsByAlbum();
                buildListsByArtist();

                mCurrentState = State.INITIALIZED;
            }
        } finally {
            if (mCurrentState != State.INITIALIZED) {
                // Something bad happened, so we reset state to NON_INITIALIZED to allow
                // retries (eg if the network connection is temporary unavailable)
                mCurrentState = State.NON_INITIALIZED;
            }
        }
    }

    public List<MediaBrowserCompat.MediaItem> getChildren(String mediaId, Resources resources) {
        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();

        if (!AutoMediaIDHelper.isBrowseable(mediaId)) {
            return mediaItems;
        }

        switch (mediaId) {
            case AutoMediaIDHelper.MEDIA_ID_ROOT:
                mediaItems.add(createBrowsableMediaItem(AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_LAST_ADDED, resources.getString(R.string.last_added), R.drawable.ic_library_add_white_24dp));
                mediaItems.add(createBrowsableMediaItem(AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_HISTORY, resources.getString(R.string.history_label), R.drawable.ic_access_time_white_24dp));
                mediaItems.add(createBrowsableMediaItem(AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_NOT_RECENTLY_PLAYED, resources.getString(R.string.not_recently_played), R.drawable.ic_watch_later_white_24dp));
                mediaItems.add(createBrowsableMediaItem(AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_TOP_TRACKS, resources.getString(R.string.top_tracks_label), R.drawable.ic_trending_up_white_24dp));

                mediaItems.add(createBrowsableMediaItem(AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_PLAYLIST, resources.getString(R.string.playlists_label), R.drawable.ic_queue_music_white_24dp));
                mediaItems.add(createBrowsableMediaItem(AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_ALBUM, resources.getString(R.string.albums_label), R.drawable.ic_album_white_24dp));
                mediaItems.add(createBrowsableMediaItem(AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_ARTIST, resources.getString(R.string.artists_label), R.drawable.ic_people_white_24dp));

                mediaItems.add(createPlayableMediaItem(AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_SHUFFLE, resources.getString(R.string.action_shuffle_all), R.drawable.ic_shuffle_white_24dp));
                mediaItems.add(createBrowsableMediaItem(AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_QUEUE, resources.getString(R.string.queue_label), R.drawable.ic_playlist_play_white_24dp));
                break;

            case AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_PLAYLIST:
                for (final Uri uri : getPlaylists()) {
                    mediaItems.add(createPlayableMediaItem(mediaId, uri, uri.getPathSegments().get(PATH_SEGMENT_TITLE), null));
                }
                break;

            case AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_ALBUM:
                for (final Uri uri : getAlbums()) {
                    final List<String> segments = uri.getPathSegments();
                    mediaItems.add(createPlayableMediaItem(mediaId, uri, segments.get(PATH_SEGMENT_TITLE), segments.get(PATH_SEGMENT_ARTIST)));
                }
                break;

            case AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_ARTIST:
                for (final Uri uri : getArtists()) {
                    mediaItems.add(createPlayableMediaItem(mediaId, uri, uri.getPathSegments().get(PATH_SEGMENT_ARTIST), null));
                }
                break;

            default:
                Iterable<Uri> listing = null;
                switch (mediaId) {
                    case AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_LAST_ADDED:
                        listing = buildListByLoader(
                                LastAddedLoader::getLastAddedSongs
                        ).values();
                        break;
                    case AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_HISTORY:
                        listing = buildListByLoader(
                                () -> TopAndRecentlyPlayedTracksLoader.getRecentlyPlayedTracks(mContext)
                        ).values();
                        break;
                    case AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_NOT_RECENTLY_PLAYED:
                        listing = buildListByLoader(
                                () -> TopAndRecentlyPlayedTracksLoader.getNotRecentlyPlayedTracks(mContext)
                        ).values();
                        break;
                    case AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_TOP_TRACKS:
                        listing = buildListByLoader(
                                () -> TopAndRecentlyPlayedTracksLoader.getTopTracks(mContext)
                        ).values();
                        break;
                    case AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_QUEUE:
                        listing = getQueue();
                        break;
                }
                if (listing != null) {
                    for (final Uri uri : listing) {
                        final List<String> segments = uri.getPathSegments();
                        mediaItems.add(createPlayableMediaItem(mediaId, uri, segments.get(PATH_SEGMENT_TITLE), segments.get(PATH_SEGMENT_ARTIST)));
                    }
                }
                break;
        }

        return mediaItems;
    }

    private MediaBrowserCompat.MediaItem createBrowsableMediaItem(String mediaId, String title, int iconDrawableId) {
        MediaDescriptionCompat.Builder builder = new MediaDescriptionCompat.Builder();
        builder.setMediaId(mediaId)
                .setTitle(title)
                .setIconBitmap(ImageUtil.createBitmap(ImageUtil.getTintedVectorDrawable(mContext, iconDrawableId, ThemeStore.textColorSecondary(mContext))));

        return new MediaBrowserCompat.MediaItem(builder.build(),
                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE);
    }

    private MediaBrowserCompat.MediaItem createPlayableMediaItem(String mediaId, Uri musicSelection,
                                                                 String title, @Nullable String subtitle) {
        MediaDescriptionCompat.Builder builder = new MediaDescriptionCompat.Builder();
        builder.setMediaId(AutoMediaIDHelper.createMediaID(musicSelection.getPathSegments().get(PATH_SEGMENT_ID), mediaId))
                .setTitle(title);

        if (subtitle != null) {
            builder.setSubtitle(subtitle);
        }

        return new MediaBrowserCompat.MediaItem(builder.build(),
                MediaBrowserCompat.MediaItem.FLAG_PLAYABLE);
    }

    private MediaBrowserCompat.MediaItem createPlayableMediaItem(String mediaId, String title, int iconDrawableId) {
        MediaDescriptionCompat.Builder builder = new MediaDescriptionCompat.Builder()
                .setMediaId(mediaId)
                .setTitle(title)
                .setIconBitmap(ImageUtil.createBitmap(ImageUtil.getTintedVectorDrawable(mContext, iconDrawableId, ThemeStore.textColorSecondary(mContext))));

        return new MediaBrowserCompat.MediaItem(builder.build(),
                MediaBrowserCompat.MediaItem.FLAG_PLAYABLE);
    }

    private enum State {
        NON_INITIALIZED, INITIALIZING, INITIALIZED
    }

    public interface Callback {
        void onMusicCatalogReady(boolean success);
    }
}
