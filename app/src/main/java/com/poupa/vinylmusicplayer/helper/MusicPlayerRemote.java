package com.poupa.vinylmusicplayer.helper;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.provider.DocumentsContract;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.poupa.vinylmusicplayer.App;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.discog.Discography;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.service.MusicService;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.WeakHashMap;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class MusicPlayerRemote {

    public static final String TAG = MusicPlayerRemote.class.getSimpleName();

    // TODO Reduce the visibility of this member (should not be exposed outside)
    @Nullable
    public static MusicService musicService;

    private static final WeakHashMap<Activity, ServiceBinder> mConnectionMap = new WeakHashMap<>();

    public static Activity bindToService(@NonNull final Activity activity,
                                             final ServiceConnection callback) {
        Activity realActivity = activity.getParent();
        if (realActivity == null) {
            realActivity = activity;
        }

        realActivity.startService(new Intent(realActivity, MusicService.class));

        final ServiceBinder binder = new ServiceBinder(callback);

        if (realActivity.bindService(new Intent().setClass(realActivity, MusicService.class), binder, Context.BIND_AUTO_CREATE)) {
            mConnectionMap.put(realActivity, binder);
            return realActivity;
        }
        return null;
    }

    public static void unbindFromService(@Nullable final Activity activity) {
        if (activity == null) {
            return;
        }
        final ServiceBinder mBinder = mConnectionMap.remove(activity);
        if (mBinder == null) {
            // TODO For debug only
            final String message = String.format(
                    "MusicPlayerRemote.unbindFromService Unknown activity=%s@%s",
                    activity,
                    Integer.toHexString(System.identityHashCode(activity))
            );
            Log.e("extended-sleep", message);

            return;
        }
        activity.unbindService(mBinder);
        if (mConnectionMap.isEmpty()) {
            if (!musicService.isPlaying()) {
                musicService.quit();
            }
            musicService = null;
        }
    }

    /**
     * This class intercepts the ServiceConnection callbacks to do reference housekeeping
     * before forwarding the event to the original callback recipient
     */
    static final class ServiceBinder implements ServiceConnection {
        private final ServiceConnection mCallback;

        public ServiceBinder(final ServiceConnection callback) {
            mCallback = callback;
        }

        @Override
        public void onServiceConnected(final ComponentName className, final IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            if (mCallback != null) {
                mCallback.onServiceConnected(className, service);
            }
        }

        @Override
        public void onServiceDisconnected(final ComponentName className) {
            if (mCallback != null) {
                mCallback.onServiceDisconnected(className);
            }
            musicService = null;
        }
    }

    /**
     * Async
     */
    public static void playSongAt(final int position) {
        if (musicService != null) {
            musicService.playSongAt(position);
        }
    }

    /**
     * Async
     */
    public static void setPosition(final int position) {
        if (musicService != null) {
            musicService.setPosition(position);
        }
    }

    public static void pauseSong() {
        if (musicService != null) {
            musicService.pause();
        }
    }

    /**
     * Async
     */
    public static void playNextSong() {
        if (musicService != null) {
            musicService.playNextSong(true);
        }
    }

    /**
     * Async
     */
    public static void playPreviousSong() {
        if (musicService != null) {
            musicService.playPreviousSong(true);
        }
    }

    /**
     * Async
     */
    public static void back() {
        if (musicService != null) {
            musicService.back(true);
        }
    }

    public static boolean isPlaying() {
        return musicService != null && musicService.isPlaying();
    }

    public static boolean isPlaying(@NonNull Song song) {
        if (!isPlaying()) {return false;}
        return (song.id == getCurrentSong().id);
    }

    public static void resumePlaying() {
        if (musicService != null) {
            musicService.play();
        }
    }

    /**
     * Async
     */
    public static void openQueue(final ArrayList<Song> queue, final int startPosition, final boolean startPlaying) {
        if (!tryToHandleOpenPlayingQueue(queue, startPosition, startPlaying) && musicService != null) {
            musicService.openQueue(queue, startPosition, startPlaying);
            if (!PreferenceUtil.getInstance().rememberShuffle()){
                setShuffleMode(MusicService.SHUFFLE_MODE_NONE);
            }
        }
    }

    /**
     * Async
     */
    public static void openAndShuffleQueue(final ArrayList<Song> queue, boolean startPlaying) {
        int startPosition = 0;
        if (!queue.isEmpty()) {
            startPosition = new Random().nextInt(queue.size());
        }

        if (!tryToHandleOpenPlayingQueue(queue, startPosition, startPlaying) && musicService != null) {
            openQueue(queue, startPosition, startPlaying);
            setShuffleMode(MusicService.SHUFFLE_MODE_SHUFFLE);
        }
    }

    private static boolean tryToHandleOpenPlayingQueue(final ArrayList<Song> queue, final int startPosition, final boolean startPlaying) {
        if (getPlayingQueue() == queue) {
            if (startPlaying) {
                playSongAt(startPosition);
            } else {
                setPosition(startPosition);
            }
            return true;
        }
        return false;
    }

    public static Song getCurrentSong() {
        if (musicService != null) {
            return musicService.getCurrentSong();
        }
        return Song.EMPTY_SONG;
    }

    public static int getPosition() {
        if (musicService != null) {
            return musicService.getPosition();
        }
        return -1;
    }

    public static ArrayList<Song> getPlayingQueue() {
        if (musicService != null) {
            return musicService.getPlayingQueue();
        }
        return new ArrayList<>();
    }

    public static int getSongProgressMillis() {
        if (musicService != null) {
            return musicService.getSongProgressMillis();
        }
        return -1;
    }

    public static int getSongDurationMillis() {
        if (musicService != null) {
            return musicService.getSongDurationMillis();
        }
        return -1;
    }

    public static void seekTo(int millis) {
        if (musicService != null) {
            musicService.seek(millis);
        }
    }

    public static int getRepeatMode() {
        if (musicService != null) {
            return musicService.getRepeatMode();
        }
        return MusicService.REPEAT_MODE_NONE;
    }

    public static int getShuffleMode() {
        if (musicService != null) {
            return musicService.getShuffleMode();
        }
        return MusicService.SHUFFLE_MODE_NONE;
    }

    public static void cycleRepeatMode() {
        if (musicService != null) {
            musicService.cycleRepeatMode();
        }
    }

    public static void toggleShuffleMode() {
        if (musicService != null) {
            musicService.toggleShuffle();
        }
    }

    public static void setShuffleMode(final int shuffleMode) {
        if (musicService != null) {
            musicService.setShuffleMode(shuffleMode);
        }
    }

    public static void playNext(Song song) {
        if (musicService != null) {
            if (getPlayingQueue().size() > 0) {
                musicService.addSong(getPosition() + 1, song);
            } else {
                ArrayList<Song> queue = new ArrayList<>();
                queue.add(song);
                openQueue(queue, 0, false);
            }
            Toast.makeText(musicService, musicService.getResources().getString(R.string.added_title_to_playing_queue), Toast.LENGTH_SHORT).show();
        }
    }

    public static void playNext(@NonNull ArrayList<Song> songs) {
        if (musicService != null) {
            if (getPlayingQueue().size() > 0) {
                musicService.addSongs(getPosition() + 1, songs);
            } else {
                openQueue(songs, 0, false);
            }
            final String toast = songs.size() == 1 ? musicService.getResources().getString(R.string.added_title_to_playing_queue) : musicService.getResources().getString(R.string.added_x_titles_to_playing_queue, songs.size());
            Toast.makeText(musicService, toast, Toast.LENGTH_SHORT).show();
        }
    }

    public static void enqueue(Song song) {
        if (musicService != null) {
            if (getPlayingQueue().size() > 0) {
                musicService.addSong(song);
            } else {
                ArrayList<Song> queue = new ArrayList<>();
                queue.add(song);
                openQueue(queue, 0, false);
            }
            Toast.makeText(musicService, musicService.getResources().getString(R.string.added_title_to_playing_queue), Toast.LENGTH_SHORT).show();
        }
    }

    public static void enqueue(@NonNull ArrayList<Song> songs) {
        if (musicService != null) {
            if (getPlayingQueue().size() > 0) {
                musicService.addSongs(songs);
            } else {
                openQueue(songs, 0, false);
            }
            final String toast = songs.size() == 1 ? musicService.getResources().getString(R.string.added_title_to_playing_queue) : musicService.getResources().getString(R.string.added_x_titles_to_playing_queue, songs.size());
            Toast.makeText(musicService, toast, Toast.LENGTH_SHORT).show();
        }
    }

    public static void removeFromQueue(@NonNull Song song) {
        if (musicService != null) {
            musicService.removeSong(song);
        }
    }

    public static void removeFromQueue(@NonNull List<Song> songs) {
        if (musicService != null) {
            musicService.removeSongs(songs);
        }
    }

    public static void removeFromQueue(int position) {
        if (musicService != null && position >= 0 && position < getPlayingQueue().size()) {
            musicService.removeSong(position);
        }
    }

    public static void moveSong(int from, int to) {
        if (musicService != null && from >= 0 && to >= 0 && from < getPlayingQueue().size() && to < getPlayingQueue().size()) {
            musicService.moveSong(from, to);
        }
    }

    public static void addSong(int to, @NonNull Song song) {
        if (musicService != null) {
            musicService.addSong(to,song);
        }
    }

    public static void clearQueue() {
        if (musicService != null) {
            musicService.clearQueue();
        }
    }

    public static int getAudioSessionId() {
        if (musicService != null) {
            return musicService.getAudioSessionId();
        }
        return -1;
    }

    @NonNull
    public static String getQueueInfoString() {
        if (musicService != null) {
            return musicService.getQueueInfoString();
        }
        return "";
    }

    public static void playFromUri(Uri uri) {
        if (musicService != null) {
            Song song = Song.EMPTY_SONG;

            // Get by id
            if (uri.getScheme() != null && uri.getAuthority() != null) {
                String songId = null;
                if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
                    if (uri.getAuthority().equals("com.android.providers.media.documents")) {
                        songId = getSongIdFromMediaProvider(uri);
                    } else if (uri.getAuthority().equals("media")) {
                        songId = uri.getLastPathSegment();
                    }
                }
                if (!TextUtils.isEmpty(songId)) {
                    try {
                        song = Discography.getInstance().getSong(Integer.parseInt(songId));
                    } catch (NumberFormatException ignored) {}
                }
            }

            // Get by path
            if (song.equals(Song.EMPTY_SONG)) {
                File songFile = null;
                if (uri.getAuthority() != null && uri.getAuthority().equals("com.android.externalstorage.documents")) {
                    songFile = new File(Environment.getExternalStorageDirectory(), uri.getPath().split(":", 2)[1]);
                }
                if (songFile == null) {
                    String path = getFilePathFromUri(musicService, uri);
                    if (path != null)
                        songFile = new File(path);
                }
                if (songFile == null && uri.getPath() != null) {
                    songFile = new File(uri.getPath());
                }
                if (songFile != null) {
                    song = Discography.getInstance().getSongByPath(songFile.getAbsolutePath());
                }
            }

            if (!song.equals(Song.EMPTY_SONG)) {
                openQueue(new ArrayList<>(Arrays.asList(song)), 0, true);
            } else {
                Log.e(TAG, "No song found for URI: " + uri);
            }
        }
    }

    @Nullable
    private static String getFilePathFromUri(Context context, Uri uri)
    {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, null, null,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static String getSongIdFromMediaProvider(Uri uri) {
        return DocumentsContract.getDocumentId(uri).split(":")[1];
    }
}
