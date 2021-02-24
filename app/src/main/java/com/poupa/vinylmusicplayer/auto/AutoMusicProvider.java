package com.poupa.vinylmusicplayer.auto;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.discog.tagging.MultiValuesTagUtil;
import com.poupa.vinylmusicplayer.loader.AlbumLoader;
import com.poupa.vinylmusicplayer.loader.ArtistLoader;
import com.poupa.vinylmusicplayer.loader.LastAddedLoader;
import com.poupa.vinylmusicplayer.loader.PlaylistLoader;
import com.poupa.vinylmusicplayer.loader.TopAndRecentlyPlayedTracksLoader;
import com.poupa.vinylmusicplayer.model.Album;
import com.poupa.vinylmusicplayer.model.Artist;
import com.poupa.vinylmusicplayer.model.CategoryInfo;
import com.poupa.vinylmusicplayer.model.Playlist;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.service.MusicService;
import com.poupa.vinylmusicplayer.util.ImageUtil;
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

    // This is a hack - for some reason the listing of big list takes forever
    // for Queue and Playlists, but not for Albums and Artist
    // so instead we deliver only a portion of it to Auto
    // TODO Drop this hack, restore the full load of different lists
    private final int LISTING_SIZE_LIMIT = 1000;

    public AutoMusicProvider(MusicService musicService) {
        mContext = musicService;
        mMusicService = new WeakReference<>(musicService);
    }

    public List<MediaBrowserCompat.MediaItem> getChildren(String mediaId, Resources resources) {
        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();

        if (!AutoMediaIDHelper.isBrowseable(mediaId)) {
            return mediaItems;
        }

        switch (mediaId) {
            case AutoMediaIDHelper.MEDIA_ID_ROOT:
                // Follow the same library order as the full app
                ArrayList<CategoryInfo> categories = PreferenceUtil.getInstance().getLibraryCategoryInfos();
                for (CategoryInfo categoryInfo : categories) {
                    if (categoryInfo.visible) {
                        switch (categoryInfo.category) {
                            case ALBUMS:
                                mediaItems.add(createBrowsableMediaItem(AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_ALBUM, resources.getString(R.string.albums_label), R.drawable.ic_album_white_24dp));
                                break;
                            case ARTISTS:
                                mediaItems.add(createBrowsableMediaItem(AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_ARTIST, resources.getString(R.string.artists_label), R.drawable.ic_people_white_24dp));
                                break;
                            case PLAYLISTS:
                                mediaItems.add(createBrowsableMediaItem(AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_PLAYLIST, resources.getString(R.string.playlists_label), R.drawable.ic_queue_music_white_24dp));
                                break;
                            case SONGS:
                                mediaItems.add(createPlayableMediaItem(AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_SHUFFLE, resources.getString(R.string.action_shuffle_all), R.drawable.ic_shuffle_white_24dp));
                                break;
                            default:
                                break;
                        }
                    }
                }

                // Other items
                mediaItems.add(createBrowsableMediaItem(AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_QUEUE, resources.getString(R.string.queue_label), R.drawable.ic_playlist_play_white_24dp));
                break;

            case AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_PLAYLIST:
                // Smart playlists
                mediaItems.add(createBrowsableMediaItem(AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_LAST_ADDED, resources.getString(R.string.last_added), R.drawable.ic_library_add_white_24dp));
                mediaItems.add(createBrowsableMediaItem(AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_HISTORY, resources.getString(R.string.history_label), R.drawable.ic_access_time_white_24dp));
                mediaItems.add(createBrowsableMediaItem(AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_NOT_RECENTLY_PLAYED, resources.getString(R.string.not_recently_played), R.drawable.ic_watch_later_white_24dp));
                mediaItems.add(createBrowsableMediaItem(AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_TOP_TRACKS, resources.getString(R.string.top_tracks_label), R.drawable.ic_trending_up_white_24dp));

                // Static playlists
                for (Playlist entry : PlaylistLoader.getAllPlaylists(mContext)) {
                    // TODO Make this browsable
                    mediaItems.add(createPlayableMediaItem(mediaId, String.valueOf(entry.id), entry.name, null));
                }
                break;

            case AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_ALBUM:
                for (Album entry : AlbumLoader.getAllAlbums()) {
                    mediaItems.add(createPlayableMediaItem(mediaId, String.valueOf(entry.getId()), entry.getTitle(), entry.getArtistName()));
                }
                break;

            case AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_ARTIST:
                for (Artist entry : ArtistLoader.getAllArtists()) {
                    mediaItems.add(createPlayableMediaItem(mediaId, String.valueOf(entry.getId()), entry.getName(), null));
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
                        final String artists = MultiValuesTagUtil.infoString(s.artistNames);
                        mediaItems.add(createPlayableMediaItem(mediaId, String.valueOf(s.id), s.title, artists));
                    }
                }
                break;

            default:
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
                }
                if (songs != null) {
                    final int fromPosition = 0;
                    final int toPosition = Math.min(songs.size() - 1, fromPosition + LISTING_SIZE_LIMIT);
                    final List<Song> limitedSongs = songs.subList(fromPosition, toPosition);

                    for (Song s : limitedSongs) {
                        final String artists = MultiValuesTagUtil.infoString(s.artistNames);
                        mediaItems.add(createPlayableMediaItem(mediaId, String.valueOf(s.id), s.title, artists));
                    }
                }
                break;
        }

        return mediaItems;
    }

    private MediaBrowserCompat.MediaItem createBrowsableMediaItem(String mediaId, String title, int iconDrawableId) {
        MediaDescriptionCompat.Builder builder = new MediaDescriptionCompat.Builder()
                .setMediaId(mediaId)
                .setTitle(title)
                .setIconBitmap(ImageUtil.createBitmap(ImageUtil.getVectorDrawable(mContext.getResources(), iconDrawableId, mContext.getTheme())));

        return new MediaBrowserCompat.MediaItem(builder.build(),
                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE);
    }

    private MediaBrowserCompat.MediaItem createPlayableMediaItem(@NonNull final String mediaId, @NonNull final String musicId,
                                                                 String title, @Nullable String subtitle) {
        MediaDescriptionCompat.Builder builder = new MediaDescriptionCompat.Builder()
                .setMediaId(AutoMediaIDHelper.createMediaID(musicId, mediaId))
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
                .setIconBitmap(ImageUtil.createBitmap(ImageUtil.getVectorDrawable(mContext.getResources(), iconDrawableId, mContext.getTheme())));

        return new MediaBrowserCompat.MediaItem(builder.build(),
                MediaBrowserCompat.MediaItem.FLAG_PLAYABLE);
    }
}
