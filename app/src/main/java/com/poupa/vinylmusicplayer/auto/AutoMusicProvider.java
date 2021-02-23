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

    private final WeakReference<MusicService> mMusicService;

    private final Context mContext;

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
                    mediaItems.add(createPlayableMediaItem(mediaId, uri, entry.name, null));
                }
                break;

            case AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_ALBUM:
                for (Album entry : AlbumLoader.getAllAlbums()) {
                    Uri uri = Uri.parse(BASE_URI).buildUpon()
                            .appendPath(String.valueOf(entry.getId()))
                            .appendPath(entry.getTitle())
                            .appendPath(entry.getArtistName())
                            .build();
                    mediaItems.add(createPlayableMediaItem(mediaId, uri, entry.getTitle(), entry.getArtistName()));
                }
                break;

            case AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_ARTIST:
                for (Artist entry : ArtistLoader.getAllArtists()) {
                    Uri uri = Uri.parse(BASE_URI).buildUpon()
                            .appendPath(String.valueOf(entry.getId()))
                            .appendPath(entry.getName())
                            .build();
                    mediaItems.add(createPlayableMediaItem(mediaId, uri, entry.getName(), null));
                }
                break;

            case AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_QUEUE:
                final MusicService service = mMusicService.get();
                if (service != null) {
                    // Only show the queue starting from the currently played song
                    final List<Song> queue = service.getPlayingQueue();
                    final int fromPosition = Math.max(0, service.getPosition());
                    final int toPosition = queue.size() - 1;
                    final List<Song> songs = queue.subList(fromPosition, toPosition);

                    for (Song s : songs) {
                        final String artists = MultiValuesTagUtil.infoString(s.artistNames);
                        Uri uri = Uri.parse(BASE_URI).buildUpon()
                                .appendPath(String.valueOf(s.id))
                                .appendPath(s.title)
                                .appendPath(artists)
                                .build();
                        mediaItems.add(createPlayableMediaItem(mediaId, uri, s.title, artists));
                    }
                }
                break;

            default:
                Supplier<List<Song>> loader = null;
                switch (mediaId) {
                    case AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_LAST_ADDED:
                        loader = LastAddedLoader::getLastAddedSongs;
                        break;
                    case AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_HISTORY:
                        loader = () -> TopAndRecentlyPlayedTracksLoader.getRecentlyPlayedTracks(mContext);
                        break;
                    case AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_NOT_RECENTLY_PLAYED:
                        loader = () -> TopAndRecentlyPlayedTracksLoader.getNotRecentlyPlayedTracks(mContext);
                        break;
                    case AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_TOP_TRACKS:
                        loader = () -> TopAndRecentlyPlayedTracksLoader.getTopTracks(mContext);
                        break;
                }
                if (loader != null) {
                    final List<Song> allSongs = loader.get();
                    final int fromPosition = 0;
                    final int toPosition = allSongs.size() - 1;
                    final List<Song> songs = allSongs.subList(fromPosition, toPosition);

                    for (Song s : songs) {
                        final String artists = MultiValuesTagUtil.infoString(s.artistNames);
                        Uri uri = Uri.parse(BASE_URI).buildUpon()
                                .appendPath(String.valueOf(s.id))
                                .appendPath(s.title)
                                .appendPath(artists)
                                .appendPath(String.valueOf(s.albumId))
                                .build();
                        final List<String> segments = uri.getPathSegments();
                        mediaItems.add(createPlayableMediaItem(mediaId, uri, s.title, artists));
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
