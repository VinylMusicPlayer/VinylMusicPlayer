package com.poupa.vinylmusicplayer.service;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.support.v4.media.MediaBrowserCompat;
import android.text.TextUtils;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.loader.AlbumLoader;
import com.poupa.vinylmusicplayer.loader.ArtistLoader;
import com.poupa.vinylmusicplayer.loader.LastAddedLoader;
import com.poupa.vinylmusicplayer.loader.TopAndRecentlyPlayedTracksLoader;
import com.poupa.vinylmusicplayer.model.Album;
import com.poupa.vinylmusicplayer.model.Artist;
import com.poupa.vinylmusicplayer.model.CategoryInfo;
import com.poupa.vinylmusicplayer.model.Playlist;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.model.smartplaylist.HistoryPlaylist;
import com.poupa.vinylmusicplayer.model.smartplaylist.LastAddedPlaylist;
import com.poupa.vinylmusicplayer.model.smartplaylist.MyTopTracksPlaylist;
import com.poupa.vinylmusicplayer.model.smartplaylist.NotRecentlyPlayedPlaylist;
import com.poupa.vinylmusicplayer.model.smartplaylist.ShuffleAllPlaylist;
import com.poupa.vinylmusicplayer.provider.StaticPlaylist;
import com.poupa.vinylmusicplayer.util.MusicUtil;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * @author Beesham Sarendranauth (Beesham)
 * @author SC (soncaokim)
 */

public class BrowsableMusicProvider {
    private final WeakReference<MusicService> mMusicService;
    private final Context mContext;

    BrowsableMusicProvider(MusicService musicService) {
        mContext = musicService;
        mMusicService = new WeakReference<>(musicService);
    }

    @NonNull
    List<MediaBrowserCompat.MediaItem> getChildren(@NonNull String path, @NonNull Resources resources) {
        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();

        switch (path) {
            case BrowsableMediaIDHelper.MEDIA_ID_ROOT:
                mediaItems.addAll(getRootChildren(resources));
                break;

            case BrowsableMediaIDHelper.MEDIA_ID_MUSICS_BY_PLAYLIST:
                mediaItems.addAll(getAllPlaylistsChildren(resources));
                break;

            case BrowsableMediaIDHelper.MEDIA_ID_MUSICS_BY_ALBUM:
                buildMediaItemsFromList(
                        mediaItems,
                        AlbumLoader.getAllAlbums(),
                        0,
                        new String[]{path},
                        Album::getId,
                        Album::getTitle,
                        (Album a) -> MusicUtil.getAlbumInfoString(mContext, a),
                        (Album a) -> MusicUtil.getMediaStoreAlbumCover(a.safeGetFirstSong()),
                        R.drawable.ic_album_white_24dp,
                        false,
                        resources);
                break;

            case BrowsableMediaIDHelper.MEDIA_ID_MUSICS_BY_ARTIST:
                buildMediaItemsFromList(
                        mediaItems,
                        ArtistLoader.getAllArtists(),
                        0,
                        new String[]{path},
                        Artist::getId,
                        Artist::getName,
                        (Artist a) -> MusicUtil.getArtistInfoString(mContext, a),
                        (Artist a) -> null, // TODO Provide artist cover image - this requires exposing the custom images via a ContentProvider
                        R.drawable.ic_person_white_24dp,
                        false,
                        resources);
                break;

            case BrowsableMediaIDHelper.MEDIA_ID_MUSICS_BY_QUEUE:
                final MusicService service = mMusicService.get();
                if (service != null) {
                    buildMediaItemsFromList(
                            mediaItems,
                            service.getPlayingQueue(),
                            service.getPosition(), // Only show the queue starting from the currently played song
                            new String[]{path},
                            (Song s) -> s.id,
                            (Song s) -> s.title,
                            MusicUtil::getSongInfoString,
                            MusicUtil::getMediaStoreAlbumCover,
                            R.drawable.ic_music_note_white_24dp,
                            true,
                            resources);
                }
                break;

            default: // We get to the case of (smart/dumb) playlists and browse by album/artist here
                final String pathPrefix = BrowsableMediaIDHelper.extractCategory(path);
                if (pathPrefix.equals(BrowsableMediaIDHelper.MEDIA_ID_MUSICS_BY_ARTIST)) {
                    final String artistIdStr = BrowsableMediaIDHelper.extractMusicID(path);
                    final long artistId = !TextUtils.isEmpty(artistIdStr) ? Long.parseLong(artistIdStr) : -1;
                    mediaItems.addAll(getArtistChildren(resources, artistId));
                } else if (pathPrefix.equals(BrowsableMediaIDHelper.MEDIA_ID_MUSICS_BY_ALBUM)) {
                    final String albumIdStr = BrowsableMediaIDHelper.extractMusicID(path);
                    final long albumId = !TextUtils.isEmpty(albumIdStr) ? Long.parseLong(albumIdStr) : -1;
                    mediaItems.addAll(getAlbumChildren(resources, albumId));
                } else {
                    mediaItems.addAll(getSpecificPlaylistChildren(resources, path));
                }
                break;
        }

        return mediaItems;
    }

    @NonNull
    List<MediaBrowserCompat.MediaItem> getRootChildren(@NonNull Resources resources) {
        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();

        // Library sections - Follow the same order as the full app
        ArrayList<CategoryInfo> categories = PreferenceUtil.getInstance().getLibraryCategoryInfos();
        for (CategoryInfo categoryInfo : categories) {
            if (categoryInfo.visible) {
                switch (categoryInfo.category) {
                    case ALBUMS:
                        boolean albumGrid = PreferenceUtil.getInstance().getAlbumGridSize(mContext) > 1;
                        mediaItems.add(BrowsableMediaItem.with(mContext)
                                .path(BrowsableMediaIDHelper.MEDIA_ID_MUSICS_BY_ALBUM)
                                .title(resources.getString(R.string.albums))
                                .icon(R.drawable.ic_album_white_24dp)
                                .asBrowsable()
                                .gridLayout(albumGrid)
                                .build()
                        );
                        break;
                    case ARTISTS:
                        mediaItems.add(BrowsableMediaItem.with(mContext)
                                .path(BrowsableMediaIDHelper.MEDIA_ID_MUSICS_BY_ARTIST)
                                .title(resources.getString(R.string.artists))
                                .icon(R.drawable.ic_people_white_24dp)
                                .asBrowsable()
                                .build()
                        );
                        break;
                    case PLAYLISTS:
                        mediaItems.add(BrowsableMediaItem.with(mContext)
                                .path(BrowsableMediaIDHelper.MEDIA_ID_MUSICS_BY_PLAYLIST)
                                .title(resources.getString(R.string.playlists_label))
                                .icon(R.drawable.ic_queue_music_white_24dp)
                                .asBrowsable()
                                .build()
                        );
                        break;
                    case SONGS:
                        mediaItems.add(BrowsableMediaItem.with(mContext)
                                .path(BrowsableMediaIDHelper.MEDIA_ID_MUSICS_BY_SHUFFLE)
                                .title(resources.getString(R.string.action_shuffle_all))
                                .subTitle(new ShuffleAllPlaylist(mContext).getInfoString(mContext))
                                .icon(R.drawable.ic_shuffle_white_24dp)
                                .asPlayable()
                                .build()
                        );
                        break;
                }
            }
        }

        // Queue
        mediaItems.add(getQueueChild(resources));

        return mediaItems;
    }

    @NonNull
    private MediaBrowserCompat.MediaItem getQueueChild(@NonNull Resources resources) {
        return BrowsableMediaItem.with(mContext)
                .path(BrowsableMediaIDHelper.MEDIA_ID_MUSICS_BY_QUEUE)
                .title(resources.getString(R.string.queue_label))
                .subTitle(mMusicService.get() != null ? mMusicService.get().getQueueInfoString() : "")
                .icon(R.drawable.ic_playlist_play_white_24dp)
                .asBrowsable()
                .build();
    }

    @NonNull
    private List<MediaBrowserCompat.MediaItem> getAllPlaylistsChildren(@NonNull Resources resources) {
        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();

        // Smart playlists
        PreferenceUtil prefs = PreferenceUtil.getInstance();
        if (prefs.getLastAddedCutoffTimeSecs() > 0) {
            mediaItems.add(BrowsableMediaItem.with(mContext)
                    .path(BrowsableMediaIDHelper.MEDIA_ID_MUSICS_BY_LAST_ADDED)
                    .title(resources.getString(R.string.last_added))
                    .subTitle(new LastAddedPlaylist(mContext).getInfoString(mContext))
                    .icon(R.drawable.ic_library_add_white_24dp)
                    .asBrowsable()
                    .build()
            );
        }
        if (prefs.getRecentlyPlayedCutoffTimeMillis() > 0) {
            mediaItems.add(BrowsableMediaItem.with(mContext)
                    .path(BrowsableMediaIDHelper.MEDIA_ID_MUSICS_BY_HISTORY)
                    .title(resources.getString(R.string.history_label))
                    .subTitle(new HistoryPlaylist(mContext).getInfoString(mContext))
                    .icon(R.drawable.ic_access_time_white_24dp)
                    .asBrowsable()
                    .build()
            );
        }
        if (prefs.getNotRecentlyPlayedCutoffTimeMillis() > 0) {
            mediaItems.add(BrowsableMediaItem.with(mContext)
                    .path(BrowsableMediaIDHelper.MEDIA_ID_MUSICS_BY_NOT_RECENTLY_PLAYED)
                    .title(resources.getString(R.string.not_recently_played))
                    .subTitle(new NotRecentlyPlayedPlaylist(mContext).getInfoString(mContext))
                    .icon(R.drawable.ic_watch_later_white_24dp)
                    .asBrowsable()
                    .build()
            );
        }
        if (prefs.maintainTopTrackPlaylist()) {
            mediaItems.add(BrowsableMediaItem.with(mContext)
                    .path(BrowsableMediaIDHelper.MEDIA_ID_MUSICS_BY_TOP_TRACKS)
                    .title(resources.getString(R.string.top_tracks_label))
                    .subTitle(new MyTopTracksPlaylist(mContext).getInfoString(mContext))
                    .icon(R.drawable.ic_trending_up_white_24dp)
                    .asBrowsable()
                    .build()
            );
        }

        // Static playlists
        for (StaticPlaylist staticPlaylist : StaticPlaylist.getAllPlaylists()) {
            Playlist entry = staticPlaylist.asPlaylist();
            // Here we are appending playlist ID to the MEDIA_ID_MUSICS_BY_PLAYLIST category.
            // Upon browsing event, this will be handled as per the case of other playlists
            mediaItems.add(BrowsableMediaItem.with(mContext)
                    .path(BrowsableMediaIDHelper.MEDIA_ID_MUSICS_BY_PLAYLIST, entry.id)
                    .title(entry.name)
                    .subTitle(entry.getInfoString(mContext))
                    .icon(MusicUtil.isFavoritePlaylist(mContext, entry)
                            ? R.drawable.ic_favorite_white_24dp
                            : R.drawable.ic_queue_music_white_24dp)
                    .asBrowsable()
                    .build()
            );
        }

        return mediaItems;
    }

    @NonNull
    private List<MediaBrowserCompat.MediaItem> getArtistChildren(@NonNull Resources resources, long artistId) {
        Artist artist = ArtistLoader.getArtist(artistId);

        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();
        buildMediaItemsFromList(
                mediaItems,
                artist.albums,
                0,
                new String[]{BrowsableMediaIDHelper.MEDIA_ID_MUSICS_BY_ALBUM},
                Album::getId,
                Album::getTitle,
                (Album a) -> MusicUtil.getAlbumInfoString(mContext, a),
                (Album a) -> MusicUtil.getMediaStoreAlbumCover(a.safeGetFirstSong()),
                R.drawable.ic_album_white_24dp,
                false,
                resources);
        return mediaItems;
    }

    @NonNull
    private List<MediaBrowserCompat.MediaItem> getAlbumChildren(@NonNull Resources resources, long albumId) {
        Album album = AlbumLoader.getAlbum(albumId);

        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();
        buildMediaItemsFromList(
                mediaItems,
                album.songs,
                0,
                new String[]{BrowsableMediaIDHelper.MEDIA_ID_MUSICS_BY_ALBUM, String.valueOf(albumId)},
                (Song s) -> s.id,
                (Song s) -> s.title,
                MusicUtil::getSongInfoString,
                MusicUtil::getMediaStoreAlbumCover,
                R.drawable.ic_music_note_white_24dp,
                true,
                resources);
        return mediaItems;
    }

    @NonNull
    private List<MediaBrowserCompat.MediaItem> getSpecificPlaylistChildren(@NonNull Resources resources, @NonNull String path) {
        List<? extends Song> songs = null;
        String[] pathParts = null;
        switch (path) {
            case BrowsableMediaIDHelper.MEDIA_ID_MUSICS_BY_LAST_ADDED:
                songs = LastAddedLoader.getLastAddedSongs();
                pathParts = new String[]{path};
                break;
            case BrowsableMediaIDHelper.MEDIA_ID_MUSICS_BY_HISTORY:
                songs = TopAndRecentlyPlayedTracksLoader.getRecentlyPlayedTracks(mContext);
                pathParts = new String[]{path};
                break;
            case BrowsableMediaIDHelper.MEDIA_ID_MUSICS_BY_NOT_RECENTLY_PLAYED:
                songs = TopAndRecentlyPlayedTracksLoader.getNotRecentlyPlayedTracks(mContext);
                pathParts = new String[]{path};
                break;
            case BrowsableMediaIDHelper.MEDIA_ID_MUSICS_BY_TOP_TRACKS:
                songs = TopAndRecentlyPlayedTracksLoader.getTopTracks(mContext);
                pathParts = new String[]{path};
                break;
            default:
                final String pathPrefix = BrowsableMediaIDHelper.extractCategory(path);
                if (pathPrefix.equals(BrowsableMediaIDHelper.MEDIA_ID_MUSICS_BY_PLAYLIST)) {
                    final String playlistIdStr = BrowsableMediaIDHelper.extractMusicID(path);
                    final long playListId = !TextUtils.isEmpty(playlistIdStr) ? Long.parseLong(playlistIdStr) : -1;
                    final StaticPlaylist playlist = StaticPlaylist.getPlaylist(playListId);
                    if (playlist != null) {
                        songs = playlist.asSongs();
                    }
                    pathParts = new String[]{pathPrefix, playlistIdStr};
                }
                break;
        }

        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();
        buildMediaItemsFromList(
                mediaItems,
                songs,
                0,
                pathParts,
                (Song s) -> s.id,
                (Song s) -> s.title,
                MusicUtil::getSongInfoString,
                MusicUtil::getMediaStoreAlbumCover,
                R.drawable.ic_music_note_white_24dp,
                true,
                resources);
        return mediaItems;
    }

    private <T> void buildMediaItemsFromList(
            @NonNull List<MediaBrowserCompat.MediaItem> destination,
            @Nullable List<? extends T> source,
            int startPosition,
            String[] pathParts,
            Function<T, Long> pathIdExtractor,
            Function<T, String> titleExtractor,
            Function<T, String> subTitleExtractor,
            Function<T, Bitmap> coverImageExtractor,
            @DrawableRes int fallbackCoverImage,
            boolean isPlayable,
            @NonNull Resources resources
    ) {
        if (source == null) {return;}

        final List<? extends T> truncatedSource = truncatedList(source, startPosition);
        for (T item : truncatedSource) {
            BrowsableMediaItem.Builder builder = BrowsableMediaItem.with(mContext)
                    .path(pathParts, pathIdExtractor.apply(item))
                    .title(titleExtractor.apply(item))
                    .subTitle(subTitleExtractor.apply(item));
            if (isPlayable) {
                builder = builder.asPlayable();
            } else {
                builder = builder.asBrowsable();
            }
            final Bitmap icon = coverImageExtractor.apply(item);
            if (icon != null) {
                builder = builder.icon(icon);
            } else {
                builder = builder.icon(fallbackCoverImage);
            }
            destination.add(builder.build());
        }
        if (source.size() > truncatedSource.size()) {
            destination.add(truncatedListIndicator(resources, pathParts));
        }
    }

    @NonNull
    private static <T> List<? extends T> truncatedList(@NonNull List<T> songs, int startPosition) {
        // As per https://developer.android.com/training/cars/media
        // Android Auto and Android Automotive OS have strict limits on how many media items they can display in each level of the menu
        final int LISTING_SIZE_LIMIT = 50;

        final int fromPosition = Math.max(0, startPosition);
        final int toPosition = Math.min(songs.size(), fromPosition + LISTING_SIZE_LIMIT);

        return songs.subList(fromPosition, toPosition);
    }

    @NonNull
    private MediaBrowserCompat.MediaItem truncatedListIndicator(@NonNull Resources resources, @NonNull final String[] pathParts) {
        return BrowsableMediaItem.with(mContext)
                .path(pathParts, Song.EMPTY_SONG.id)
                .title(resources.getString(R.string.auto_limited_listing_title))
                .subTitle(resources.getString(R.string.auto_limited_listing_subtitle))
                .build();
    }
}
