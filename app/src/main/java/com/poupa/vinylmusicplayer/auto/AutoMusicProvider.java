package com.poupa.vinylmusicplayer.auto;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
import com.poupa.vinylmusicplayer.util.ImageUtil;
import com.poupa.vinylmusicplayer.util.MusicUtil;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beesham Sarendranauth (Beesham)
 */
public class AutoMusicProvider {
    private final WeakReference<MusicService> mMusicService;
    private final Context mContext;

    private final int BROWSABLE = MediaBrowserCompat.MediaItem.FLAG_BROWSABLE;
    private final int PLAYABLE = MediaBrowserCompat.MediaItem.FLAG_PLAYABLE;

    public AutoMusicProvider(MusicService musicService) {
        mContext = musicService;
        mMusicService = new WeakReference<>(musicService);
    }

    public List<MediaBrowserCompat.MediaItem> getChildren(String mediaId, Resources resources) {
        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();

        switch (mediaId) {
            case AutoMediaIDHelper.MEDIA_ID_ROOT:
                // Follow the same library order as the full app
                ArrayList<CategoryInfo> categories = PreferenceUtil.getInstance().getLibraryCategoryInfos();
                for (CategoryInfo categoryInfo : categories) {
                    if (categoryInfo.visible) {
                        switch (categoryInfo.category) {
                            case ALBUMS:
                                mediaItems.add(createMediaItem(
                                        AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_ALBUM,
                                        resources.getString(R.string.albums_label),
                                        R.drawable.ic_album_white_24dp,
                                        BROWSABLE
                                ));
                                break;
                            case ARTISTS:
                                mediaItems.add(createMediaItem(
                                        AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_ARTIST,
                                        resources.getString(R.string.artists_label),
                                        R.drawable.ic_people_white_24dp,
                                        BROWSABLE
                                ));
                                break;
                            case PLAYLISTS:
                                mediaItems.add(createMediaItem(
                                        AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_PLAYLIST,
                                        resources.getString(R.string.playlists_label),
                                        R.drawable.ic_queue_music_white_24dp,
                                        BROWSABLE
                                ));
                                break;
                            case SONGS:
                                mediaItems.add(createMediaItem(
                                        AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_SHUFFLE,
                                        resources.getString(R.string.action_shuffle_all),
                                        R.drawable.ic_shuffle_white_24dp,
                                        PLAYABLE
                                ));
                                break;
                        }
                    }
                }

                mediaItems.add(createMediaItem(
                        AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_QUEUE,
                        resources.getString(R.string.queue_label),
                        R.drawable.ic_playlist_play_white_24dp,
                        BROWSABLE
                ));
                break;

            case AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_PLAYLIST:
                // TODO Provide hints to show these as list
                // Smart playlists
                mediaItems.add(createMediaItem(
                        AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_LAST_ADDED,
                        resources.getString(R.string.last_added),
                        R.drawable.ic_library_add_white_24dp,
                        BROWSABLE
                ));
                mediaItems.add(createMediaItem(
                        AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_HISTORY,
                        resources.getString(R.string.history_label),
                        R.drawable.ic_access_time_white_24dp,
                        BROWSABLE
                ));
                mediaItems.add(createMediaItem(
                        AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_NOT_RECENTLY_PLAYED,
                        resources.getString(R.string.not_recently_played),
                        R.drawable.ic_watch_later_white_24dp,
                        BROWSABLE
                ));
                mediaItems.add(createMediaItem(
                        AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_TOP_TRACKS,
                        resources.getString(R.string.top_tracks_label),
                        R.drawable.ic_trending_up_white_24dp,
                        BROWSABLE
                ));

                // Static playlists
                for (Playlist entry : PlaylistLoader.getAllPlaylists(mContext)) {
                    mediaItems.add(createMediaItem(
                            // Here we are appending playlist ID to the MEDIA_ID_MUSICS_BY_PLAYLIST category.
                            // Upon browsing event, this will be handled as per the case of other playlists
                            AutoMediaIDHelper.createMediaID(
                                    String.valueOf(entry.id),
                                    AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_PLAYLIST),
                            entry.name,
                            MusicUtil.isFavoritePlaylist(mContext, entry)
                                    ? R.drawable.ic_favorite_white_24dp
                                    : R.drawable.ic_queue_music_white_24dp,
                            BROWSABLE
                    ));
                }
                break;

            case AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_ALBUM:
                // TODO Provide hints to show these as either list or grid, according to the preference in full app
                for (Album entry : AlbumLoader.getAllAlbums()) {
                    mediaItems.add(createMediaItem(
                            AutoMediaIDHelper.createMediaID(String.valueOf(entry.getId()), mediaId),
                            entry.getTitle(),
                            MusicUtil.getAlbumInfoString(mContext, entry),
                            PLAYABLE
                    ));
                }
                break;

            case AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_ARTIST:
                // TODO Provide hints to show these as either list or grid, according to the preference in full app
                for (Artist entry : ArtistLoader.getAllArtists()) {
                    mediaItems.add(createMediaItem(
                            AutoMediaIDHelper.createMediaID(String.valueOf(entry.getId()), mediaId),
                            entry.getName(),
                            MusicUtil.getArtistInfoString(mContext, entry),
                            PLAYABLE
                    ));
                }
                break;

            case AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_QUEUE:
                // TODO Provide hints to show these as list
                final MusicService service = mMusicService.get();
                if (service != null) {
                    // Only show the queue starting from the currently played song
                    final List<Song> songs = truncatedList(service.getPlayingQueue(), service.getPosition());
                    for (Song s : songs) {
                        mediaItems.add(createMediaItem(
                                AutoMediaIDHelper.createMediaID(String.valueOf(s.id), mediaId),
                                s.title,
                                MusicUtil.getSongInfoString(s),
                                PLAYABLE
                        ));
                    }
                }
                break;

            default: // We get to the case of (smart/dumb) playlists here
                mediaItems.addAll(getPlayListChildren(mediaId));
                break;
        }

        return mediaItems;
    }

    private List<MediaBrowserCompat.MediaItem> getPlayListChildren(String mediaId) {
        // TODO Provide hints to show these as list
        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();

        List<Song> songs = null;
        switch (mediaId) {
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
                final String path = AutoMediaIDHelper.extractCategory(mediaId);
                if (path.equals(AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_PLAYLIST)) {
                    try {
                        long playListId = Long.parseLong(AutoMediaIDHelper.extractMusicID(mediaId));
                        songs = PlaylistSongLoader.getPlaylistSongList(mContext, playListId);
                    }
                    catch (NumberFormatException ignored) {}
                }
                break;
        }
        if (songs != null) {
            final List<Song> limitedSongs = truncatedList(songs, 0);
            final String path = AutoMediaIDHelper.extractCategory(mediaId);
            for (Song s : limitedSongs) {
                mediaItems.add(createMediaItem(
                        AutoMediaIDHelper.createMediaID(String.valueOf(s.id), path),
                        s.title,
                        MusicUtil.getSongInfoString(s),
                        PLAYABLE
                ));
            }
        }

        return mediaItems;
    }

    private MediaBrowserCompat.MediaItem createMediaItem(
            @NonNull String path,
            @NonNull String title,
            int iconDrawableId,
            int flag
    ) {
        return createMediaItem(path, title, null, iconDrawableId, flag);
    }

    private MediaBrowserCompat.MediaItem createMediaItem(
            @NonNull String path,
            @NonNull String title,
            @Nullable String subTitle,
            int flag
    ) {
        return createMediaItem(path, title, subTitle, 0, flag);
    }

    private MediaBrowserCompat.MediaItem createMediaItem(
            @NonNull String path,
            @NonNull String title,
            @Nullable String subTitle,
            int iconDrawableId,
            int flag
    ) {
        MediaDescriptionCompat.Builder builder = new MediaDescriptionCompat.Builder()
                .setMediaId(path)
                .setTitle(title);

        if (!TextUtils.isEmpty(subTitle)) {
            builder.setSubtitle(subTitle);
        }

        if (iconDrawableId != 0) {
            builder.setIconBitmap(ImageUtil.createBitmap(ImageUtil.getVectorDrawable(
                    mContext.getResources(),
                    iconDrawableId,
                    mContext.getTheme()
            )));
        }
        return new MediaBrowserCompat.MediaItem(builder.build(), flag);
    }

    private List<Song> truncatedList(@NonNull List<Song> songs, int startPosition) {
        // As per https://developer.android.com/training/cars/media
        // Android Auto and Android Automotive OS have strict limits on how many media items they can display in each level of the menu
        final int LISTING_SIZE_LIMIT = 100;

        final int fromPosition = Math.max(0, startPosition);
        final int toPosition = Math.min(songs.size(), fromPosition + LISTING_SIZE_LIMIT);

        return songs.subList(fromPosition, toPosition);
    }
}
