package com.poupa.vinylmusicplayer.auto;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
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
import java.util.List;
import java.util.function.Supplier;

/**
 * Created by Beesham Sarendranauth (Beesham)
 */
public class AutoMusicProvider {
    private static final String BASE_URI = "androidauto://vinyl";
    private static final int PATH_SEGMENT_ID = 0;
    private static final int PATH_SEGMENT_TITLE = 1;
    private static final int PATH_SEGMENT_ARTIST = 2;

    private final WeakReference<MusicService> mMusicService;

    private final Context mContext;

    // This is a hack - for some reason the listing of big list takes forever
    // for Queue and Playlists, but not for Albums and Artist
    // so instead we deliver only a portion of it to Auto
    // TODO Drop this hack, restore the async full load of different lists
    private final int LISTING_SIZE_LIMIT = 1000;

    public AutoMusicProvider(MusicService musicService) {
        mContext = musicService;
        mMusicService = new WeakReference<>(musicService);
    }

    private synchronized List<Uri> buildListByLoader(Supplier<List<Song>> loader) {
        List<Uri> result = new ArrayList<>();

        final List<Song> allSongs = loader.get();
        final int fromPosition = 0;
        final int toPosition = Math.min(allSongs.size() - 1, fromPosition + LISTING_SIZE_LIMIT);
        final List<Song> songs = allSongs.subList(fromPosition, toPosition);

        for (Song s : songs) {
            Uri.Builder songData = Uri.parse(BASE_URI).buildUpon();
            songData.appendPath(String.valueOf(s.id))
                    .appendPath(s.title)
                    .appendPath(MultiValuesTagUtil.infoString(s.artistNames))
                    .appendPath(String.valueOf(s.albumId));
            result.add(songData.build());
        }

        return result;
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
                for (Playlist entry : PlaylistLoader.getAllPlaylists(mContext)) {
                    Uri uri = Uri.parse(BASE_URI).buildUpon()
                            .appendPath(String.valueOf(entry.id))
                            .appendPath(entry.name)
                            .build();
                    mediaItems.add(createPlayableMediaItem(mediaId, uri, uri.getPathSegments().get(PATH_SEGMENT_TITLE), null));
                }
                break;

            case AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_ALBUM:
                for (Album entry : AlbumLoader.getAllAlbums()) {
                    Uri uri = Uri.parse(BASE_URI).buildUpon()
                            .appendPath(String.valueOf(entry.getId()))
                            .appendPath(entry.getTitle())
                            .appendPath(entry.getArtistName())
                            .build();
                    final List<String> segments = uri.getPathSegments();
                    mediaItems.add(createPlayableMediaItem(mediaId, uri, segments.get(PATH_SEGMENT_TITLE), segments.get(PATH_SEGMENT_ARTIST)));
                }
                break;

            case AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_ARTIST:
                for (Artist entry : ArtistLoader.getAllArtists()) {
                    Uri uri = Uri.parse(BASE_URI).buildUpon()
                            .appendPath(String.valueOf(entry.getId()))
                            .appendPath(entry.getName())
                            .appendPath(entry.getName())
                            .build();
                    mediaItems.add(createPlayableMediaItem(mediaId, uri, uri.getPathSegments().get(PATH_SEGMENT_ARTIST), null));
                }
                break;

            case AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_QUEUE:
                final MusicService service = mMusicService.get();
                if (service != null) {
                    // Only show the queue starting from the currently played song
                    final List<Song> queue = service.getPlayingQueue();
                    final int fromPosition = Math.max(0, service.getPosition());
                    final int toPosition = Math.min(queue.size() - 1, fromPosition + LISTING_SIZE_LIMIT);
                    final List<Song> songs = queue.subList(fromPosition, toPosition);

                    for (Song s : songs) {
                        Uri uri = Uri.parse(BASE_URI).buildUpon()
                                .appendPath(String.valueOf(s.id))
                                .appendPath(s.title)
                                .appendPath(MultiValuesTagUtil.infoString(s.artistNames))
                                .appendPath(String.valueOf(s.albumId))
                                .build();
                        final List<String> segments = uri.getPathSegments();
                        mediaItems.add(createPlayableMediaItem(mediaId, uri, segments.get(PATH_SEGMENT_TITLE), segments.get(PATH_SEGMENT_ARTIST)));
                    }
                }
                break;

            default:
                Iterable<Uri> listing = null;
                switch (mediaId) {
                    case AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_LAST_ADDED:
                        listing = buildListByLoader(
                                LastAddedLoader::getLastAddedSongs
                        );
                        break;
                    case AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_HISTORY:
                        listing = buildListByLoader(
                                () -> TopAndRecentlyPlayedTracksLoader.getRecentlyPlayedTracks(mContext)
                        );
                        break;
                    case AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_NOT_RECENTLY_PLAYED:
                        listing = buildListByLoader(
                                () -> TopAndRecentlyPlayedTracksLoader.getNotRecentlyPlayedTracks(mContext)
                        );
                        break;
                    case AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_TOP_TRACKS:
                        listing = buildListByLoader(
                                () -> TopAndRecentlyPlayedTracksLoader.getTopTracks(mContext)
                        );
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
}
