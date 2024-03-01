package com.poupa.vinylmusicplayer.util;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.text.TextUtils;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.poupa.vinylmusicplayer.App;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.discog.tagging.MultiValuesTagUtil;
import com.poupa.vinylmusicplayer.helper.MusicPlayerRemote;
import com.poupa.vinylmusicplayer.model.Album;
import com.poupa.vinylmusicplayer.model.Artist;
import com.poupa.vinylmusicplayer.model.Genre;
import com.poupa.vinylmusicplayer.model.Playlist;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.model.lyrics.AbsSynchronizedLyrics;
import com.poupa.vinylmusicplayer.provider.StaticPlaylist;
import com.poupa.vinylmusicplayer.service.MusicService;

import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.images.Artwork;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class MusicUtil {
    public static @Nullable Bitmap getMediaStoreAlbumCover(@NonNull final Song song) {
        final Context context = App.getStaticContext();
        try (AutoCloseAudioFile audio = SAFUtil.loadReadOnlyAudioFile(context, song)) {
            return getMediaStoreAlbumCover(audio);
        } catch (Exception e) {
            OopsHandler.collectStackTrace(e);
            return null;
        }
    }

    public static @Nullable Bitmap getMediaStoreAlbumCover(@Nullable final AutoCloseAudioFile audio) {
        try {
            if (audio == null) {
                return null;
            }
            final Artwork artworkTag = audio.get().getTagOrCreateAndSetDefault().getFirstArtwork();
            if (artworkTag != null) {
                final byte[] artworkBinaryData = artworkTag.getBinaryData();
                return BitmapFactory.decodeByteArray(artworkBinaryData, 0, artworkBinaryData.length);
            }
            return null;
        } catch (Exception ignored) {
            return null;
        }
    }

    public static Uri getSongFileUri(long songId) {
        return ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songId);
    }

    @NonNull
    public static Intent createShareSongFileIntent(@NonNull final Song song, Context context) {
        try {
            return new Intent()
                    .setAction(Intent.ACTION_SEND)
                    .putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName(), new File(song.data)))
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    .setType("audio/*");
        } catch (IllegalArgumentException e) {
            // TODO the path is most likely not like /storage/emulated/0/... but something like /storage/28C7-75B0/...
            e.printStackTrace();
            SafeToast.show(context, "Could not share this file, I'm aware of the issue.");
            return new Intent();
        }
    }

    @NonNull
    public static String getArtistInfoString(@NonNull final Context context, @NonNull final Artist artist) {
        int albumCount = artist.getAlbumCount();
        int songCount = artist.getSongCount();

        return MusicUtil.buildInfoString(
            MusicUtil.getAlbumCountString(context, albumCount),
            MusicUtil.getSongCountString(context, songCount)
        );
    }

    @NonNull
    public static String getAlbumInfoString(@NonNull final Context context, @NonNull final Album album) {
        int songCount = album.getSongCount();

        return MusicUtil.buildInfoString(
            album.getArtistName(),
            MusicUtil.getSongCountString(context, songCount)
        );
    }

    @NonNull
    public static String getSongInfoString(@NonNull final Song song) {
        return MusicUtil.buildInfoString(
                PreferenceUtil.getInstance().showSongNumber() ? MusicUtil.getTrackNumberInfoString(song) : null,
                MultiValuesTagUtil.infoString(song.artistNames),
                song.albumName
        );
    }

    @NonNull
    public static String getGenreInfoString(@NonNull final Context context, @NonNull final Genre genre) {
        int songCount = genre.songCount;
        return MusicUtil.getSongCountString(context, songCount);
    }

    @NonNull
    public static String getPlaylistInfoString(@NonNull final Context context, @NonNull List<? extends Song> songs) {
        final long duration = getTotalDuration(songs);

        return MusicUtil.buildInfoString(
            MusicUtil.getSongCountString(context, songs.size()),
            MusicUtil.getReadableDurationString(duration)
        );
    }

    @NonNull
    public static String getSongCountString(@NonNull final Context context, int songCount) {
        final String songString = songCount == 1 ? context.getResources().getString(R.string.song) : context.getResources().getString(R.string.songs);
        return songCount + " " + songString;
    }

    @NonNull
    public static String getAlbumCountString(@NonNull final Context context, int albumCount) {
        final String albumString = albumCount == 1 ? context.getResources().getString(R.string.album) : context.getResources().getString(R.string.albums);
        return albumCount + " " + albumString;
    }

    @NonNull
    public static String getYearString(int year) {
        return year > 0 ? String.valueOf(year) : "-";
    }

    public static long getTotalDuration(@NonNull List<? extends Song> songs) {
        long duration = 0;
        for (int i = 0; i < songs.size(); i++) {
            duration += songs.get(i).duration;
        }
        return duration;
    }

    public static String getReadableDurationString(long songDurationMillis) {
        long minutes = (songDurationMillis / 1000) / 60;
        long seconds = (songDurationMillis / 1000) % 60;
        if (minutes < 60) {
            return String.format(Locale.getDefault(), "%01d:%02d", minutes, seconds);
        } else {
            long hours = minutes / 60;
            minutes = minutes % 60;
            return String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds);
        }
    }

    /**
     * Build a concatenated string from the provided arguments
     * The intended purpose is to show extra annotations
     * to a music library item.
     * Ex: for a given album --> buildInfoString(album.artist, album.songCount)
     */
    @NonNull
    public static String buildInfoString(final String... values)
    {
        return MusicUtil.buildInfoString("  •  ", values);
    }

    @NonNull
    public static String buildInfoString(@NonNull final String separator, @NonNull final String[] values)
    {
        StringBuilder result = new StringBuilder();
        for (String value : values) {
            if (TextUtils.isEmpty(value)) continue;
            if (result.length() > 0) result.append(separator);
            result.append(value);
        }
        return result.toString();
    }

    @NonNull
    public static String getTrackNumberInfoString(@NonNull final Song song) {
        String result = "";
        if (song.discNumber > 0) {
            result = song.discNumber + "-";
        }
        if (song.trackNumber > 0) {
            result += String.valueOf(song.trackNumber);
        }
        else if (result.isEmpty()) {
            result = "-";
        }
        return result;
    }

    public static void deleteTracks(@NonNull final Fragment fragment, ActivityResultLauncher<IntentSenderRequest> deleteRequestApi30, @NonNull final List<Song> songs, @Nullable final List<Uri> safUris) {
        final int songCount = songs.size();

        Activity activity = fragment.requireActivity();

        try {
            // Step 1: Remove selected tracks from the current playlist
            MusicPlayerRemote.removeFromQueue(songs);

            // Step 2: Remove selected tracks from the database
            final StringBuilder selection = new StringBuilder();
            selection.append(BaseColumns._ID + " IN (");
            for (int i = 0; i < songCount - 1; i++) {
                selection.append(songs.get(i).id);
                selection.append(",");
            }
            // The last element of a batch
            selection.append(songs.get(songCount - 1).id);
            selection.append(")");

            activity.getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    selection.toString(), null);

            // Step 3: Remove files from card
            if (Build.VERSION.SDK_INT < VERSION_CODES.Q) {
                for (int i = 0; i < songCount; i++) {
                    final Uri safUri = safUris == null || safUris.size() <= i ? null : safUris.get(i);
                    SAFUtil.delete(activity, songs.get(i).data, safUri);
                }
            }
            else if (Build.VERSION.SDK_INT == VERSION_CODES.Q) {
                // Android Q takes care of this if the element is remove via MediaStore
            }
            else { // Android R and after
                List<Uri> urisToDelete = new ArrayList<>();
                for (Song song: songs) {
                    // See: https://stackoverflow.com/questions/64472765/java-lang-illegalargumentexception-all-requested-items-must-be-referenced-by-sp
                    urisToDelete.add(ContentUris.withAppendedId(MediaStore.Audio.Media.getContentUri("external"), song.id));
                }
                PendingIntent editPendingIntent = MediaStore.createDeleteRequest(activity.getContentResolver(),
                        urisToDelete);

                deleteRequestApi30.launch(new IntentSenderRequest.Builder(editPendingIntent).build());
            }
        } catch (SecurityException e) { // | SendIntentException e) {
            OopsHandler.collectStackTrace(e);
        }

        if (Build.VERSION.SDK_INT < VERSION_CODES.R) {
            activity.getContentResolver().notifyChange(Uri.parse("content://media"), null);

            SafeToast.show(activity, activity.getString(R.string.deleted_x_songs, songCount));
        }
    }

    public static boolean isFavoritePlaylist(@NonNull final Context context, @NonNull final Playlist playlist) {
        return playlist.name != null && isFavoritePlaylist(context, playlist.name);
    }

    public static boolean isFavoritePlaylist(@NonNull final Context context, @NonNull final String playlistName) {
        return playlistName.equals(context.getString(R.string.favorites));
    }

    @Nullable
    public static Playlist getFavoritesPlaylist(@NonNull final Context context) {
        StaticPlaylist playlist = StaticPlaylist.getPlaylist(context.getString(R.string.favorites));
        if (playlist != null) {return playlist.asPlaylist();}
        return null;
    }

    private static Playlist getOrCreateFavoritesPlaylist(@NonNull final Context context) {
        return StaticPlaylist.getOrCreatePlaylist(context.getString(R.string.favorites)).asPlaylist();
    }

    public static Playlist getOrCreateSkippedPlaylist(@NonNull final Context context) {
        return StaticPlaylist.getOrCreatePlaylist(context.getString(R.string.skipped_songs)).asPlaylist();
    }

    public static boolean isFavorite(@NonNull final Context context, @NonNull final Song song) {
        Playlist playlist = getFavoritesPlaylist(context);
        if (playlist == null) {return false;}
        return PlaylistsUtil.doesPlaylistContain(playlist.id, song.id);
    }

    public static void toggleFavorite(@NonNull final Context context, @NonNull final Song song) {
        if (isFavorite(context, song)) {
            PlaylistsUtil.removeFromPlaylist(context, song, getFavoritesPlaylist(context).id);
        } else {
            PlaylistsUtil.addToPlaylist(context, song, getOrCreateFavoritesPlaylist(context).id, false);
        }

        context.sendBroadcast(new Intent(MusicService.FAVORITE_STATE_CHANGED));
    }

    public static boolean isArtistNameUnknown(@Nullable String artistName) {
        return isNameUnknown(artistName, Artist.UNKNOWN_ARTIST_DISPLAY_NAME);
    }

    public static boolean isAlbumNameUnknown(@Nullable String albumName) {
        return isNameUnknown(albumName, Album.UNKNOWN_ALBUM_DISPLAY_NAME);
    }

    public static boolean isGenreNameUnknown(@Nullable String genreName) {
        return isNameUnknown(genreName, Genre.UNKNOWN_GENRE_DISPLAY_NAME);
    }

    private static boolean isNameUnknown(@Nullable String name, @NonNull final String defaultDisplayName) {
        if ((name == null) || (name.length() == 0)) return true;
        if (name.equals(defaultDisplayName)) return true;
        name = name.trim().toLowerCase();
        return (name.equals("unknown") || name.equals("<unknown>"));
    }

    @NonNull
    public static String getNameWithoutArticle(@Nullable final String title) {
        if (TextUtils.isEmpty(title)) {return "";}

        String strippedTitle = title.trim();

        final List<String> articles = List.of(
                "a ", "an ", "the ", // English ones
                "l'", "le ", "la ", "les " // French ones
        );
        String lowerCaseTitle = strippedTitle.toLowerCase();
        for (final String article : articles) {
            if (lowerCaseTitle.startsWith(article)) {
                strippedTitle = strippedTitle.substring(article.length());
                break;
            }
        }
        return strippedTitle;
    }

    public static int indexOfSongInList(List<Song> songs, long songId) {
        for (int i = 0; i < songs.size(); i++) {
            if (songs.get(i).id == songId) {
                return i;
            }
        }
        return -1;
    }

    @Nullable
    public static String getLyrics(@NonNull final Context context, @NonNull final Song song) {
        if (song.id == Song.EMPTY_SONG.id) {return null;}

        String lyrics = null;
        try (AutoCloseAudioFile audio = SAFUtil.loadReadOnlyAudioFile(context, song)) {
            lyrics = audio.get().getTagOrCreateDefault().getFirst(FieldKey.LYRICS);
        } catch (@NonNull Exception | NoSuchMethodError | VerifyError e) {
            OopsHandler.collectStackTrace(e);
        }

        if (lyrics == null || lyrics.trim().isEmpty() || !AbsSynchronizedLyrics.isSynchronized(lyrics)) {
            try {
                File file = new File(song.data);
                File dir = file.getAbsoluteFile().getParentFile();

                if (dir != null && dir.exists() && dir.isDirectory()) {
                    String format = ".*%s.*\\.(lrc|txt)";
                    String filename = Pattern.quote(FileUtil.stripExtension(file.getName()));
                    String songTitle = Pattern.quote(song.title);

                    final ArrayList<Pattern> patterns = new ArrayList<>();
                    patterns.add(Pattern.compile(String.format(format, filename), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE));
                    patterns.add(Pattern.compile(String.format(format, songTitle), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE));

                    File[] files = dir.listFiles(f -> {
                        for (Pattern pattern : patterns) {
                            if (pattern.matcher(f.getName()).matches()) return true;
                        }
                        return false;
                    });

                    if (files != null && files.length > 0) {
                        for (File f : files) {
                            String newLyrics = FileUtil.read(f);
                            if (!newLyrics.trim().isEmpty()) {
                                if (AbsSynchronizedLyrics.isSynchronized(newLyrics)) {
                                    return newLyrics;
                                }
                                lyrics = newLyrics;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                OopsHandler.collectStackTrace(e);
            }
        }

        return lyrics;
    }
}
