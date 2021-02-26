package com.poupa.vinylmusicplayer.auto;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.media.MediaBrowserCompat;

import androidx.annotation.NonNull;

import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.loader.AlbumLoader;
import com.poupa.vinylmusicplayer.loader.ArtistLoader;
import com.poupa.vinylmusicplayer.loader.LastAddedLoader;
import com.poupa.vinylmusicplayer.loader.PlaylistLoader;
import com.poupa.vinylmusicplayer.loader.PlaylistSongLoader;
import com.poupa.vinylmusicplayer.loader.TopAndRecentlyPlayedTracksLoader;
import com.poupa.vinylmusicplayer.model.Album;
import com.poupa.vinylmusicplayer.model.Artist;
import com.poupa.vinylmusicplayer.model.CategoryInfo;
import com.poupa.vinylmusicplayer.model.Playlist;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.service.MusicService;
import com.poupa.vinylmusicplayer.util.MusicUtil;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Beesham Sarendranauth (Beesham)
 * @author SC (soncaokim)
 */

public class AutoMusicProvider {
    private final WeakReference<MusicService> mMusicService;
    private final Context mContext;

    public AutoMusicProvider(MusicService musicService) {
        mContext = musicService;
        mMusicService = new WeakReference<>(musicService);
    }

    @NonNull
    public List<MediaBrowserCompat.MediaItem> getChildren(@NonNull String path, Resources resources) {
        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();

        switch (path) {
            case AutoMediaIDHelper.MEDIA_ID_ROOT:
                // Library sections - Follow the same order as the full app
                ArrayList<CategoryInfo> categories = PreferenceUtil.getInstance().getLibraryCategoryInfos();
                for (CategoryInfo categoryInfo : categories) {
                    if (categoryInfo.visible) {
                        switch (categoryInfo.category) {
                            case ALBUMS:
                                boolean albumGrid = PreferenceUtil.getInstance().getAlbumGridSize(mContext) > 1;
                                mediaItems.add(new AutoMediaItemBuilder(mContext, AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_ALBUM)
                                        .title(resources.getString(R.string.albums_label))
                                        .icon(R.drawable.ic_album_white_24dp)
                                        .asBrowsable()
                                        .gridLayout(albumGrid)
                                        .build()
                                );
                                break;
                            case ARTISTS:
                                boolean artistGrid = PreferenceUtil.getInstance().getArtistGridSize(mContext) > 1;
                                mediaItems.add(new AutoMediaItemBuilder(mContext, AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_ARTIST)
                                        .title(resources.getString(R.string.artists_label))
                                        .icon(R.drawable.ic_people_white_24dp)
                                        .asBrowsable()
                                        .gridLayout(artistGrid)
                                        .build()
                                );
                                break;
                            case PLAYLISTS:
                                mediaItems.add(new AutoMediaItemBuilder(mContext, AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_PLAYLIST)
                                        .title(resources.getString(R.string.playlists_label))
                                        .icon(R.drawable.ic_queue_music_white_24dp)
                                        .asBrowsable()
                                        .build()
                                );
                                break;
                            case SONGS:
                                mediaItems.add(new AutoMediaItemBuilder(mContext, AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_SHUFFLE)
                                        .title(resources.getString(R.string.action_shuffle_all))
                                        .icon(R.drawable.ic_shuffle_white_24dp)
                                        .asPlayable()
                                        .build()
                                );
                                break;
                        }
                    }
                }

                // Others
                mediaItems.add(new AutoMediaItemBuilder(mContext, AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_QUEUE)
                        .title(resources.getString(R.string.queue_label))
                        .icon(R.drawable.ic_playlist_play_white_24dp)
                        .asBrowsable()
                        .build()
                );
                break;

            case AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_PLAYLIST:
                // Smart playlists
                mediaItems.add(new AutoMediaItemBuilder(mContext, AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_LAST_ADDED)
                        .title(resources.getString(R.string.last_added))
                        .icon(R.drawable.ic_library_add_white_24dp)
                        .asBrowsable()
                        .build()
                );
                mediaItems.add(new AutoMediaItemBuilder(mContext, AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_HISTORY)
                        .title(resources.getString(R.string.history_label))
                        .icon(R.drawable.ic_access_time_white_24dp)
                        .asBrowsable()
                        .build()
                );
                mediaItems.add(new AutoMediaItemBuilder(mContext, AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_NOT_RECENTLY_PLAYED)
                        .title(resources.getString(R.string.not_recently_played))
                        .icon(R.drawable.ic_watch_later_white_24dp)
                        .asBrowsable()
                        .build()
                );
                mediaItems.add(new AutoMediaItemBuilder(mContext, AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_TOP_TRACKS)
                        .title(resources.getString(R.string.top_tracks_label))
                        .icon(R.drawable.ic_trending_up_white_24dp)
                        .asBrowsable()
                        .build()
                );

                // Static playlists
                for (Playlist entry : PlaylistLoader.getAllPlaylists(mContext)) {
                    // Here we are appending playlist ID to the MEDIA_ID_MUSICS_BY_PLAYLIST category.
                    // Upon browsing event, this will be handled as per the case of other playlists
                    mediaItems.add(new AutoMediaItemBuilder(mContext, AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_PLAYLIST, entry.id)
                            .title(entry.name)
                            .icon(MusicUtil.isFavoritePlaylist(mContext, entry)
                                    ? R.drawable.ic_favorite_white_24dp
                                    : R.drawable.ic_queue_music_white_24dp)
                            .asBrowsable()
                            .build()
                    );
                }
                break;

            case AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_ALBUM:
                for (Album entry : AlbumLoader.getAllAlbums()) {
                    mediaItems.add(new AutoMediaItemBuilder(mContext, path, entry.getId())
                            .title(entry.getTitle())
                            .subTitle(MusicUtil.getAlbumInfoString(mContext, entry))
                            // TODO Supply the cover image
                            .asPlayable()
                            .build()
                    );
                }
                break;

            case AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_ARTIST:
                for (Artist entry : ArtistLoader.getAllArtists()) {
                    mediaItems.add(new AutoMediaItemBuilder(mContext, path, entry.getId())
                            .title(entry.getName())
                            .subTitle(MusicUtil.getArtistInfoString(mContext, entry))
                            // TODO Supply the cover image
                            .asPlayable()
                            .build()
                    );
                }
                break;

            case AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_QUEUE:
                final MusicService service = mMusicService.get();
                if (service != null) {
                    // Only show the queue starting from the currently played song
                    final List<Song> songs = truncatedList(service.getPlayingQueue(), service.getPosition());
                    for (Song s : songs) {
                        mediaItems.add(new AutoMediaItemBuilder(mContext, path, s.id)
                                .title(s.title)
                                .subTitle(MusicUtil.getSongInfoString(s))
                                .asPlayable()
                                .build()
                        );
                    }
                }
                break;

            default: // We get to the case of (smart/dumb) playlists here
                mediaItems.addAll(getPlayListChildren(path));
                break;
        }

        return mediaItems;
    }

    @NonNull
    private List<MediaBrowserCompat.MediaItem> getPlayListChildren(@NonNull String path) {
        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();

        List<Song> songs = null;
        switch (path) {
            case AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_LAST_ADDED:
                songs = LastAddedLoader.getLastAddedSongs();
                break;
            case AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_HISTORY:
                songs = TopAndRecentlyPlayedTracksLoader.getRecentlyPlayedTracks(mContext);
                break;
            case AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_NOT_RECENTLY_PLAYED:
                songs = TopAndRecentlyPlayedTracksLoader.getNotRecentlyPlayedTracks(mContext);
                break;
            case AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_TOP_TRACKS:
                songs = TopAndRecentlyPlayedTracksLoader.getTopTracks(mContext);
                break;
            default:
                final String pathPrefix = AutoMediaIDHelper.extractCategory(path);
                if (pathPrefix.equals(AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_PLAYLIST)) {
                    try {
                        long playListId = Long.parseLong(AutoMediaIDHelper.extractMusicID(path));
                        songs = PlaylistSongLoader.getPlaylistSongList(mContext, playListId);
                    }
                    catch (NumberFormatException ignored) {}
                }
                break;
        }
        if (songs != null) {
            final List<Song> limitedSongs = truncatedList(songs, 0);
            final String pathPrefix = AutoMediaIDHelper.extractCategory(path);
            for (Song s : limitedSongs) {
                mediaItems.add(new AutoMediaItemBuilder(mContext, pathPrefix, s.id)
                        .title(s.title)
                        .subTitle(MusicUtil.getSongInfoString(s))
                        .asPlayable()
                        .build()
                );
            }
        }

        return mediaItems;
    }

    @NonNull
    private List<Song> truncatedList(@NonNull List<Song> songs, int startPosition) {
        // As per https://developer.android.com/training/cars/media
        // Android Auto and Android Automotive OS have strict limits on how many media items they can display in each level of the menu
        final int LISTING_SIZE_LIMIT = 100;

        final int fromPosition = Math.max(0, startPosition);
        final int toPosition = Math.min(songs.size(), fromPosition + LISTING_SIZE_LIMIT);

        return songs.subList(fromPosition, toPosition);
    }
}
