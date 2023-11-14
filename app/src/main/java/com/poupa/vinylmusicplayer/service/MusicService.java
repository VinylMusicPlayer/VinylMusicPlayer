package com.poupa.vinylmusicplayer.service;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.audiofx.AudioEffect;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Process;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Predicate;
import androidx.media.MediaBrowserServiceCompat;

import com.bumptech.glide.request.transition.Transition;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.appwidgets.AppWidgetBig;
import com.poupa.vinylmusicplayer.appwidgets.AppWidgetCard;
import com.poupa.vinylmusicplayer.appwidgets.AppWidgetClassic;
import com.poupa.vinylmusicplayer.appwidgets.AppWidgetSmall;
import com.poupa.vinylmusicplayer.discog.tagging.MultiValuesTagUtil;
import com.poupa.vinylmusicplayer.glide.GlideApp;
import com.poupa.vinylmusicplayer.glide.GlideRequest;
import com.poupa.vinylmusicplayer.glide.VinylGlideExtension;
import com.poupa.vinylmusicplayer.glide.VinylSimpleTarget;
import com.poupa.vinylmusicplayer.helper.PendingIntentCompat;
import com.poupa.vinylmusicplayer.misc.queue.IndexedSong;
import com.poupa.vinylmusicplayer.misc.queue.StaticPlayingQueue;
import com.poupa.vinylmusicplayer.model.Playlist;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.provider.HistoryStore;
import com.poupa.vinylmusicplayer.provider.MusicPlaybackQueueStore;
import com.poupa.vinylmusicplayer.provider.SongPlayCountStore;
import com.poupa.vinylmusicplayer.service.notification.IdleNotification;
import com.poupa.vinylmusicplayer.service.notification.PlayingNotification;
import com.poupa.vinylmusicplayer.service.notification.PlayingNotificationImplApi19;
import com.poupa.vinylmusicplayer.service.notification.PlayingNotificationImplApi24;
import com.poupa.vinylmusicplayer.service.playback.Playback;
import com.poupa.vinylmusicplayer.util.MusicUtil;
import com.poupa.vinylmusicplayer.util.OopsHandler;
import com.poupa.vinylmusicplayer.util.PackageValidator;
import com.poupa.vinylmusicplayer.util.PlaylistsUtil;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;
import com.poupa.vinylmusicplayer.util.SafeToast;
import com.poupa.vinylmusicplayer.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Karim Abou Zeid (kabouzeid), Andrew Neal
 */
public class MusicService extends MediaBrowserServiceCompat implements SharedPreferences.OnSharedPreferenceChangeListener, Playback.PlaybackCallbacks {

    public static final String TAG = MusicService.class.getSimpleName();

    static final String VINYL_MUSIC_PLAYER_PACKAGE_NAME = "com.poupa.vinylmusicplayer";
    private static final String MUSIC_PACKAGE_NAME = "com.android.music";

    public static final String ACTION_TOGGLE_PAUSE = VINYL_MUSIC_PLAYER_PACKAGE_NAME + ".togglepause";
    static final String ACTION_PLAY = VINYL_MUSIC_PLAYER_PACKAGE_NAME + ".play";
    public static final String ACTION_PLAY_PLAYLIST = VINYL_MUSIC_PLAYER_PACKAGE_NAME + ".play.playlist";
    static final String ACTION_PAUSE = VINYL_MUSIC_PLAYER_PACKAGE_NAME + ".pause";
    static final String ACTION_STOP = VINYL_MUSIC_PLAYER_PACKAGE_NAME + ".stop";
    public static final String ACTION_SKIP = VINYL_MUSIC_PLAYER_PACKAGE_NAME + ".skip";
    public static final String ACTION_REWIND = VINYL_MUSIC_PLAYER_PACKAGE_NAME + ".rewind";
    public static final String ACTION_QUIT = VINYL_MUSIC_PLAYER_PACKAGE_NAME + ".quitservice";
    public static final String ACTION_PENDING_QUIT = VINYL_MUSIC_PLAYER_PACKAGE_NAME + ".pendingquitservice";
    public static final String INTENT_EXTRA_PLAYLIST = VINYL_MUSIC_PLAYER_PACKAGE_NAME + "intentextra.playlist";
    public static final String INTENT_EXTRA_SHUFFLE_MODE = VINYL_MUSIC_PLAYER_PACKAGE_NAME + ".intentextra.shufflemode";

    public static final String APP_WIDGET_UPDATE = VINYL_MUSIC_PLAYER_PACKAGE_NAME + ".appwidgetupdate";
    public static final String EXTRA_APP_WIDGET_NAME = VINYL_MUSIC_PLAYER_PACKAGE_NAME + "app_widget_name";

    // Do not change these three strings as it will break support with other apps (e.g. last.fm scrobbling)
    public static final String META_CHANGED = VINYL_MUSIC_PLAYER_PACKAGE_NAME + ".metachanged";
    public static final String QUEUE_CHANGED = VINYL_MUSIC_PLAYER_PACKAGE_NAME + ".queuechanged";
    public static final String PLAY_STATE_CHANGED = VINYL_MUSIC_PLAYER_PACKAGE_NAME + ".playstatechanged";

    public static final String FAVORITE_STATE_CHANGED = VINYL_MUSIC_PLAYER_PACKAGE_NAME + ".favoritestatechanged";

    public static final String REPEAT_MODE_CHANGED = VINYL_MUSIC_PLAYER_PACKAGE_NAME + ".repeatmodechanged";
    public static final String SHUFFLE_MODE_CHANGED = VINYL_MUSIC_PLAYER_PACKAGE_NAME + ".shufflemodechanged";
    public static final String MEDIA_STORE_CHANGED = VINYL_MUSIC_PLAYER_PACKAGE_NAME + ".mediastorechanged";

    static final String CYCLE_REPEAT = VINYL_MUSIC_PLAYER_PACKAGE_NAME + ".cyclerepeat";
    static final String TOGGLE_SHUFFLE = VINYL_MUSIC_PLAYER_PACKAGE_NAME + ".toggleshuffle";
    public static final String TOGGLE_FAVORITE = VINYL_MUSIC_PLAYER_PACKAGE_NAME + ".togglefavorite";

    private static final String SAVED_POSITION = "POSITION";
    private static final String SAVED_POSITION_IN_TRACK = "POSITION_IN_TRACK";
    private static final String SAVED_SHUFFLE_MODE = "SHUFFLE_MODE";
    private static final String SAVED_REPEAT_MODE = "REPEAT_MODE";

    static final int RELEASE_WAKELOCK = 0;
    static final int TRACK_ENDED = 1;
    static final int TRACK_WENT_TO_NEXT = 2;
    static final int PLAY_SONG = 3;
    static final int PREPARE_NEXT = 4;
    static final int SET_POSITION = 5;
    static final int FOCUS_CHANGE = 6;
    static final int DUCK = 7;
    static final int UNDUCK = 8;
    static final int RESTORE_QUEUES = 9;

    public static final int RANDOM_START_POSITION_ON_SHUFFLE = StaticPlayingQueue.INVALID_POSITION;
    public static final int SHUFFLE_MODE_NONE = StaticPlayingQueue.SHUFFLE_MODE_NONE;
    public static final int SHUFFLE_MODE_SHUFFLE = StaticPlayingQueue.SHUFFLE_MODE_SHUFFLE;

    public static final int REPEAT_MODE_NONE = StaticPlayingQueue.REPEAT_MODE_NONE;
    public static final int REPEAT_MODE_ALL = StaticPlayingQueue.REPEAT_MODE_ALL;
    public static final int REPEAT_MODE_THIS = StaticPlayingQueue.REPEAT_MODE_THIS;

    static final int SAVE_QUEUES = 0;
    private static final int SKIP_THRESHOLD_MS = 5000;

    private final IBinder musicBind = new MusicBinder();

    public boolean pendingQuit = false;

    private final AppWidgetBig appWidgetBig = AppWidgetBig.getInstance();
    private final AppWidgetClassic appWidgetClassic = AppWidgetClassic.getInstance();
    private final AppWidgetSmall appWidgetSmall = AppWidgetSmall.getInstance();
    private final AppWidgetCard appWidgetCard = AppWidgetCard.getInstance();

    private StaticPlayingQueue playingQueue = new StaticPlayingQueue();

    private boolean queuesRestored;
    private boolean pausedByTransientLossOfFocus;

    private PlayingNotification playingNotification;
    private IdleNotification idleNotification;

    private AudioManager audioManager;
    private MediaSessionCompat mediaSession;
    private PowerManager.WakeLock wakeLock;

    @Nullable private Playback playback;
    private PlaybackHandler playbackHandler;
    private HandlerThread playbackHandlerThread;
    private final AudioManager.OnAudioFocusChangeListener audioFocusListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(final int focusChange) {
            synchronized (MusicService.this) {
                if (playbackHandlerThread.isAlive()) {
                    playbackHandler.obtainMessage(FOCUS_CHANGE, focusChange, 0).sendToTarget();
                }
            }
        }
    };

    private QueueSaveHandler queueSaveHandler;
    private HandlerThread queueSaveHandlerThread;

    private final SongPlayCountHelper songPlayCountHelper = new SongPlayCountHelper();
    private ThrottledSeekHandler throttledSeekHandler;
    private boolean becomingNoisyReceiverRegistered;
    private final IntentFilter becomingNoisyReceiverIntentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    private final BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, @NonNull Intent intent) {
            if (intent.getAction().equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
                pause();
            }
        }
    };
    private ContentObserver mediaStoreObserver;
    private boolean notHandledMetaChangedForCurrentTrack;

    private Handler uiThreadHandler;

    private PackageValidator mPackageValidator;
    private BrowsableMusicProvider mBrowsableMusicProvider;

    private static String getTrackUri(@NonNull Song song) {
        return MusicUtil.getSongFileUri(song.id).toString();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        final PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        wakeLock.setReferenceCounted(false);

        synchronized (this) {
            playbackHandlerThread = new HandlerThread("PlaybackHandler");
            playbackHandlerThread.start();
            playbackHandler = new PlaybackHandler(this, playbackHandlerThread.getLooper());

            playback = new MultiPlayer(this);
            playback.setCallbacks(this);
        }

        // queue saving needs to run on a separate thread so that it doesn't block the playback handler events
        queueSaveHandlerThread = new HandlerThread("QueueSaveHandler", Process.THREAD_PRIORITY_BACKGROUND);
        queueSaveHandlerThread.start();
        queueSaveHandler = new QueueSaveHandler(this, queueSaveHandlerThread.getLooper());

        setupMediaSession();

        uiThreadHandler = new Handler();

        registerReceiver(widgetIntentReceiver, new IntentFilter(APP_WIDGET_UPDATE));
        registerReceiver(updateFavoriteReceiver, new IntentFilter(FAVORITE_STATE_CHANGED));

        initNotification();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Android O+ requires a foreground service to post a notification asap
            updateNotification();
        }
        mediaStoreObserver = new MediaStoreObserver(this, playbackHandler);
        throttledSeekHandler = new ThrottledSeekHandler(this, playbackHandler);
        getContentResolver().registerContentObserver(
                MediaStore.Audio.Media.INTERNAL_CONTENT_URI, true, mediaStoreObserver);
        getContentResolver().registerContentObserver(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, true, mediaStoreObserver);

        PreferenceUtil.getInstance().registerOnSharedPreferenceChangedListener(this);

        restoreState();

        mPackageValidator = new PackageValidator(this, R.xml.allowed_media_browser_callers);
        mBrowsableMusicProvider = new BrowsableMusicProvider(this);

        sendBroadcast(new Intent(VINYL_MUSIC_PLAYER_PACKAGE_NAME + ".VINYL_MUSIC_PLAYER_MUSIC_SERVICE_CREATED"));
        mediaStoreObserver.onChange(true);
    }

    private AudioManager getAudioManager() {
        if (audioManager == null) {
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        }
        return audioManager;
    }

    private void setupMediaSession() {
        synchronized (this) {
            ComponentName mediaButtonReceiverComponentName = new ComponentName(this, MediaButtonIntentReceiver.class);

            Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
            mediaButtonIntent.setComponent(mediaButtonReceiverComponentName);

            PendingIntent mediaButtonReceiverPendingIntent = PendingIntentCompat.getBroadcast(this, 0, mediaButtonIntent, 0);

            MediaSessionCallback mMediaSessionCallback = new MediaSessionCallback(this);
            mediaSession = new MediaSessionCompat(this, "VinylMusicPlayer", mediaButtonReceiverComponentName, mediaButtonReceiverPendingIntent);
            mediaSession.setCallback(mMediaSessionCallback);
            mediaSession.setActive(true);
            mediaSession.setMediaButtonReceiver(mediaButtonReceiverPendingIntent);
            setSessionToken(mediaSession.getSessionToken());
        }
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        if ((intent != null) && (intent.getAction() != null)) {
            synchronized (this) {
                restoreQueuesAndPositionIfNecessary(); // TODO Not necessary? Since already called async via onCreate.restoreStates
                String action = intent.getAction();
                switch (action) {
                    case ACTION_TOGGLE_PAUSE:
                        if (isPlaying()) {
                            pause();
                        } else {
                            play();
                        }
                        break;
                    case ACTION_PAUSE:
                        pause();
                        break;
                    case ACTION_PLAY:
                        play();
                        break;
                    case ACTION_PLAY_PLAYLIST:
                        Playlist playlist = intent.getParcelableExtra(INTENT_EXTRA_PLAYLIST);
                        int shuffleMode = intent.getIntExtra(INTENT_EXTRA_SHUFFLE_MODE, playingQueue.getShuffleMode());
                        if (playlist != null) {
                            ArrayList<Song> playlistSongs = playlist.getSongs(this);
                            if (!playlistSongs.isEmpty()) {
                                openQueue(playlistSongs, RANDOM_START_POSITION_ON_SHUFFLE, true, shuffleMode);
                            } else {
                                SafeToast.show(this, R.string.playlist_is_empty);
                            }
                        } else {
                            SafeToast.show(this, R.string.playlist_is_empty);
                        }
                        break;
                    case ACTION_REWIND:
                        back(true);
                        break;
                    case ACTION_SKIP:
                        playNextSong(true);
                        break;
                    case TOGGLE_FAVORITE:
                        MusicUtil.toggleFavorite(this, getCurrentSong());
                        break;
                    case ACTION_STOP:
                    case ACTION_QUIT:
                        pendingQuit = false;
                        quit();
                        break;
                    case ACTION_PENDING_QUIT:
                        pendingQuit = true;
                        if (PreferenceUtil.getInstance().gaplessPlayback()) {
                            playback.setNextDataSource(null);
                        }
                        break;
                }
            }
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(widgetIntentReceiver);
        unregisterReceiver(updateFavoriteReceiver);

        synchronized (this) {
            if (becomingNoisyReceiverRegistered) {
                unregisterReceiver(becomingNoisyReceiver);
                becomingNoisyReceiverRegistered = false;
            }

            mediaSession.setActive(false);
        }

        quit();
        releaseResources();
        getContentResolver().unregisterContentObserver(mediaStoreObserver);
        PreferenceUtil.getInstance().unregisterOnSharedPreferenceChangedListener(this);
        wakeLock.release();

        sendBroadcast(new Intent(VINYL_MUSIC_PLAYER_PACKAGE_NAME + ".VINYL_MUSIC_PLAYER_MUSIC_SERVICE_DESTROYED"));
    }

    @Override
    public IBinder onBind(Intent intent) {
        // For Android auto, need to call super, or onGetRoot won't be called.
        if (intent != null && "android.media.browse.MediaBrowserService".equals(intent.getAction())) {
            return super.onBind(intent);
        }

        return musicBind;
    }

    void saveQueuesImpl() {
        ArrayList<IndexedSong> queue;
        ArrayList<IndexedSong> originalQueue;
        synchronized (this) {
            // Get a copy of the queues
            queue = new ArrayList<>(playingQueue.getPlayingQueue());
            originalQueue = new ArrayList<>(playingQueue.getOriginalPlayingQueue());
        }
        MusicPlaybackQueueStore.getInstance(this).saveQueues(
                queue,
                originalQueue
        );
    }

    private void savePosition() {
        PreferenceManager.getDefaultSharedPreferences(this).edit().putInt(SAVED_POSITION, getPosition()).apply();
    }

    void savePositionInTrack() {
        PreferenceManager.getDefaultSharedPreferences(this).edit().putInt(SAVED_POSITION_IN_TRACK, getSongProgressMillis()).apply();
    }

    private void saveState() {
        saveQueues();
        savePosition();
        savePositionInTrack();
    }

    private void saveQueues() {
        queueSaveHandler.removeMessages(SAVE_QUEUES);
        queueSaveHandler.sendEmptyMessage(SAVE_QUEUES);
    }

    private void restoreState() {
        synchronized (this) {
            playingQueue.restoreMode(
                    PreferenceManager.getDefaultSharedPreferences(this).getInt(SAVED_SHUFFLE_MODE, 0),
                    PreferenceManager.getDefaultSharedPreferences(this).getInt(SAVED_REPEAT_MODE, 0));
            handleAndSendChangeInternal(SHUFFLE_MODE_CHANGED);
            handleAndSendChangeInternal(REPEAT_MODE_CHANGED);

            if (playbackHandlerThread.isAlive()) {
                playbackHandler.removeMessages(RESTORE_QUEUES);
                playbackHandler.sendEmptyMessage(RESTORE_QUEUES);
            }
        }
    }

    void restoreQueuesAndPositionIfNecessary() {
        synchronized (this) {
            if (!queuesRestored && playingQueue.size() == 0) {
                try {
                    final MusicPlaybackQueueStore queueStore = MusicPlaybackQueueStore.getInstance(this);
                    ArrayList<IndexedSong> restoredQueue = queueStore.getSavedPlayingQueue();
                    ArrayList<IndexedSong> restoredOriginalQueue = queueStore.getSavedOriginalPlayingQueue();
                    int restoredPosition = PreferenceManager.getDefaultSharedPreferences(this).getInt(SAVED_POSITION, -1);
                    int restoredPositionInTrack = PreferenceManager.getDefaultSharedPreferences(this).getInt(SAVED_POSITION_IN_TRACK, -1);

                    if (!restoredQueue.isEmpty() && (restoredQueue.size() == restoredOriginalQueue.size()) && (restoredPosition != -1)) {
                        playingQueue = new StaticPlayingQueue(
                                restoredQueue,
                                restoredOriginalQueue,
                                restoredPosition,
                                playingQueue.getShuffleMode(),
                                playingQueue.getRepeatMode()
                        );

                        openCurrent();
                        prepareNext();

                        if (restoredPositionInTrack > 0) {
                            seek(restoredPositionInTrack);
                        }

                        notHandledMetaChangedForCurrentTrack = true;
                        sendChangeInternal(META_CHANGED);
                        sendChangeInternal(QUEUE_CHANGED);
                    }
                } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException queueCopiesOutOfSync) {
                    // fallback, when the copies of the restored queues are out of sync or the queues are corrupted
                    OopsHandler.copyStackTraceToClipboard(queueCopiesOutOfSync);
                    SafeToast.show(this, R.string.failed_restore_playing_queue);

                    final int shuffleMode = playingQueue.getShuffleMode();
                    playingQueue = new StaticPlayingQueue();
                    playingQueue.setShuffle(shuffleMode);
                }
            }
            queuesRestored = true;
        }
    }

    public void quit() {
        pause();

        playingNotification.stop();
        idleNotification.stop();
        stopForeground(true);

        closeAudioEffectSession();
        getAudioManager().abandonAudioFocus(audioFocusListener);
        stopSelf();
    }

    private void releaseResources() {
        queueSaveHandler.removeCallbacksAndMessages(null);
        queueSaveHandlerThread.quitSafely();

        synchronized (this) {
            playbackHandler.removeCallbacksAndMessages(null);
            playbackHandlerThread.quitSafely();

            playback.release();
            playback = null;

            mediaSession.release();
        }
    }

    public boolean isPlaying() {
        synchronized (this) {
            return (playback != null) && playback.isPlaying();
        }
    }

    public boolean isPlaying(@NonNull Song song) {
        synchronized (this) {
            if (!isPlaying()) {
                return false;
            }

            return getCurrentIndexedSong().isQuickEqual(song);
        }
    }

    public int getPosition() {
        synchronized (this) {
            return playingQueue.getCurrentPosition();
        }
    }

    public void playNextSong(boolean skippedLast) {
        synchronized (this) {
            playSongAt(playingQueue.getNextPosition(skippedLast), skippedLast);
        }
    }

    boolean openTrackAndPrepareNextAt(int position) {
        synchronized (this) {
            playingQueue.setCurrentPosition(position);
            boolean prepared = openCurrent();
            if (prepared) prepareNextImpl();
            notifyChange(META_CHANGED);
            notHandledMetaChangedForCurrentTrack = false;
            return prepared;
        }
    }

    private boolean openCurrent() {
        synchronized (this) {
            try {
                return (playback != null) && playback.setDataSource(getTrackUri(getCurrentSong()));
            } catch (Exception e) {
                OopsHandler.copyStackTraceToClipboard(e);
                return false;
            }
        }
    }

    private void prepareNext() {
        synchronized (this) {
            if (playbackHandlerThread.isAlive()) {
                playbackHandler.removeMessages(PREPARE_NEXT);
                playbackHandler.obtainMessage(PREPARE_NEXT).sendToTarget();
            }
        }
    }

    void prepareNextImpl() {
        synchronized (this) {
            try {
                int nextPosition = playingQueue.getNextPosition(false);
                if (getRepeatMode() == REPEAT_MODE_NONE && playingQueue.isLastTrack()) {
                    playback.setNextDataSource(null);
                } else {
                    playback.setNextDataSource(getTrackUri(getSongAt(nextPosition)));
                }
                playingQueue.setNextPosition(nextPosition);
            } catch (Exception e) {
                OopsHandler.copyStackTraceToClipboard(e);
            }
        }
    }

    private void closeAudioEffectSession() {
        synchronized (this) {
            final Intent audioEffectsIntent = new Intent(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION);
            audioEffectsIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, playback.getAudioSessionId());
            audioEffectsIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
            sendBroadcast(audioEffectsIntent);
        }
    }

    private boolean requestFocus() {
        return (getAudioManager().requestAudioFocus(audioFocusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
    }

    private void initNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !PreferenceUtil.getInstance().classicNotification()) {
            playingNotification = new PlayingNotificationImplApi24();
        } else {
            playingNotification = new PlayingNotificationImplApi19();
        }
        playingNotification.init(this,
                PlayingNotification.NOTIFICATION_CHANNEL_ID,
                R.string.playing_notification_name,
                R.string.playing_notification_description);

        idleNotification = new IdleNotification();
        idleNotification.init(this,
                IdleNotification.NOTIFICATION_CHANNEL_ID,
                R.string.idle_notification_name,
                R.string.idle_notification_description);
    }

    private void updateNotification() {
        if (getCurrentSong().id != Song.EMPTY_SONG.id) {
            idleNotification.stop();
            playingNotification.update();
        } else {
            playingNotification.stop();
            idleNotification.update();
        }
    }

    private final BroadcastReceiver updateFavoriteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            updateNotification();
        }
    };

    void updateMediaSessionPlaybackState() {
        synchronized (this) {
            PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
                    .setActions(MEDIA_SESSION_ACTIONS)
                    .setState(isPlaying() ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED,
                            getSongProgressMillis(), 1);

            setCustomAction(stateBuilder);

            mediaSession.setPlaybackState(stateBuilder.build());
        }
    }

    private void setCustomAction(PlaybackStateCompat.Builder stateBuilder) {
        int repeatIcon = R.drawable.ic_repeat_white_nocircle_48dp;  // REPEAT_MODE_NONE
        if (getRepeatMode() == REPEAT_MODE_THIS) {
            repeatIcon = R.drawable.ic_repeat_one_white_circle_48dp;
        } else if (getRepeatMode() == REPEAT_MODE_ALL) {
            repeatIcon = R.drawable.ic_repeat_white_circle_48dp;
        }
        stateBuilder.addCustomAction(new PlaybackStateCompat.CustomAction.Builder(
                CYCLE_REPEAT, getString(R.string.action_cycle_repeat), repeatIcon)
                .build());

        synchronized (this) {
            final int shuffleIcon = playingQueue.getShuffleMode() == MusicService.SHUFFLE_MODE_NONE ? R.drawable.ic_shuffle_white_nocircle_48dp : R.drawable.ic_shuffle_white_circle_48dp;
            stateBuilder.addCustomAction(new PlaybackStateCompat.CustomAction.Builder(
                    TOGGLE_SHUFFLE, getString(R.string.action_toggle_shuffle), shuffleIcon)
                    .build());
        }

        final int favoriteIcon = MusicUtil.isFavorite(this, getCurrentSong()) ? R.drawable.ic_favorite_white_circle_48dp : R.drawable.ic_favorite_border_white_nocircle_48dp;
        stateBuilder.addCustomAction(new PlaybackStateCompat.CustomAction.Builder(
                TOGGLE_FAVORITE, getString(R.string.action_toggle_favorite), favoriteIcon)
                .build());
    }

    private void updateMediaSessionMetaData() {
        final Song song = getCurrentSong();

        if (song.id == -1) {
            synchronized (this) {
                mediaSession.setMetadata(null);
            }
            return;
        }

        final MediaMetadataCompat.Builder metaData = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, MultiValuesTagUtil.infoString(song.artistNames))
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST, MultiValuesTagUtil.infoString(song.albumArtistNames))
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, song.albumName)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, song.duration)
                .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, getPosition() + 1)
                .putLong(MediaMetadataCompat.METADATA_KEY_YEAR, song.year);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            synchronized (this) {
                metaData.putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, playingQueue.size());
            }
        }

        // Note: For Android Auto and for Android 13, it is necessary to provide METADATA_KEY_ALBUM_ART
        //       or similar to the MediaSession to have a hi-res cover image displayed,
        //       respectively on the Auto's now playing screen and Android 13's now playing notification/lockscreen
        final Point screenSize = Util.getScreenSize(this);
        GlideRequest<Bitmap> request = GlideApp.with(this)
                .asBitmap()
                .load(VinylGlideExtension.getSongModel(song))
                .transition(VinylGlideExtension.getDefaultTransition())
                .songOptions(song);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                request.into(new VinylSimpleTarget<Bitmap>(screenSize.x, screenSize.y) {
                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);

                        synchronized (MusicService.this) {
                            mediaSession.setMetadata(metaData.build());
                        }
                    }

                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> glideAnimation) {
                        synchronized (MusicService.this) {
                            metaData.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, copy(resource));
                            mediaSession.setMetadata(metaData.build());
                        }
                    }
                });
            }
        });
    }

    private static Bitmap copy(Bitmap bitmap) {
        Bitmap.Config config = bitmap.getConfig();
        if (config == null) {
            config = Bitmap.Config.RGB_565;
        }
        try {
            return bitmap.copy(config, false);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    public void runOnUiThread(Runnable runnable) {
        uiThreadHandler.post(runnable);
    }

    public Song getCurrentSong() {
        return getCurrentIndexedSong();
    }

    public IndexedSong getCurrentIndexedSong() {
        return getIndexedSongAt(getPosition());
    }

    private Song getSongAt(int position) {
        return getIndexedSongAt(position);
    }

    public IndexedSong getIndexedSongAt(int position) {
        synchronized (this) {
            if (position >= 0 && position < playingQueue.size()) {
                return playingQueue.getPlayingQueue().get(position);
            } else {
                return IndexedSong.EMPTY_INDEXED_SONG;
            }
        }
    }

    boolean isLastTrack() {
        synchronized (this) {
            return playingQueue.isLastTrack();
        }
    }

    public ArrayList<Song> getPlayingQueue() {
        synchronized (this) {
            return playingQueue.getPlayingQueueSongOnly();
        }
    }

    public int getRepeatMode() {
        synchronized (this) {
            return playingQueue.getRepeatMode();
        }
    }

    public void cycleRepeatMode() {
        synchronized (this) {
            playingQueue.cycleRepeatMode();
        }
        propagateRepeatChange();
    }

    private void propagateRepeatChange() {
        synchronized (this) {
            PreferenceManager.getDefaultSharedPreferences(this).edit()
                    .putInt(SAVED_REPEAT_MODE, playingQueue.getRepeatMode())
                    .apply();
            prepareNext();
            handleAndSendChangeInternal(REPEAT_MODE_CHANGED);
        }
    }

    private void propagateShuffleChange() {
        synchronized (this) {
            PreferenceManager.getDefaultSharedPreferences(this).edit()
                    .putInt(SAVED_SHUFFLE_MODE, playingQueue.getShuffleMode())
                    .apply();
            handleAndSendChangeInternal(SHUFFLE_MODE_CHANGED);
            notifyChange(QUEUE_CHANGED);
        }
    }

    public int getShuffleMode() {
        synchronized (this) {
            return playingQueue.getShuffleMode();
        }
    }

    public void toggleShuffle() {
        synchronized (this) {
            playingQueue.toggleShuffle();
            propagateShuffleChange();
        }
    }

    public void setShuffleMode(final int shuffleMode) {
        synchronized (this) {
            playingQueue.setShuffle(shuffleMode);
            propagateShuffleChange();
        }
    }

    public void openQueue(@Nullable final ArrayList<Song> playingQueue, final int startPosition, final boolean startPlaying, final int shuffleMode) {
        synchronized (this) {
            int position = startPosition;
            if (playingQueue != null && shuffleMode != MusicService.SHUFFLE_MODE_NONE && startPosition == MusicService.RANDOM_START_POSITION_ON_SHUFFLE) {
                position = new Random().nextInt(playingQueue.size());
            }

            if (this.playingQueue.openQueue(playingQueue, position, shuffleMode)) {
                if (startPlaying) {
                    playSongAt(this.playingQueue.getCurrentPosition(), false);
                } else {
                    setPosition(position);
                }
                notifyChange(QUEUE_CHANGED);
            }
        }
    }

    public void openQueue(@Nullable final ArrayList<Song> playingQueue, final int startPosition, final boolean startPlaying) {
        synchronized (this) {
            openQueue(playingQueue, startPosition, startPlaying, this.playingQueue.getShuffleMode());
        }
    }

    public void addSongAfter(int position, Song song) {
        synchronized (this) {
            playingQueue.addAfter(position, song);
            notifyChange(QUEUE_CHANGED);
        }
    }

    public void addSongBackTo(int position, IndexedSong song) {
        synchronized (this) {
            playingQueue.addSongBackTo(position, song);
            notifyChange(QUEUE_CHANGED);
        }
    }

    public void addSongsAfter(int position, List<Song> songs) {
        synchronized (this) {
            playingQueue.addAllAfter(position, songs);
            notifyChange(QUEUE_CHANGED);
        }
    }

    public void addSong(Song song) {
        synchronized (this) {
            playingQueue.add(song);
            notifyChange(QUEUE_CHANGED);
        }
    }

    public void addSongs(List<Song> songs) {
        synchronized (this) {
            playingQueue.addAll(songs);
            notifyChange(QUEUE_CHANGED);
        }
    }

    public void removeSong(int position) { // better to test is playing here and have only one signal than calling playNextSong and then removeSong (two signal need time in between to work ok)
        synchronized (this) {
            boolean isPlaying = isPlaying(playingQueue.getPlayingQueue().get(position));

            int newPosition = playingQueue.remove(position);
            if (newPosition != -1) {
                if (isPlaying) {playSongAt(newPosition, false);}
                else {setPosition(newPosition);}
            }

            notifyChange(QUEUE_CHANGED);
        }
    }

    public void removeSongs(@NonNull List<Song> songs) {
        synchronized (this) {
            int newPosition = playingQueue.removeSongs(songs);
            if (newPosition != -1) {
                setPosition(newPosition);
            }

            notifyChange(QUEUE_CHANGED);
        }
    }

    public void moveSong(int from, int to) {
        synchronized (this) {
            playingQueue.move(from, to);
            notifyChange(QUEUE_CHANGED);
        }
    }

    public void clearQueue() {
        synchronized (this) {
            playingQueue.clear();

            setPosition(-1);
            notifyChange(QUEUE_CHANGED);
        }
    }

    public void playSongAt(final int position, boolean skippedLast) {
        synchronized (this) {
            if (skippedLast && PreferenceUtil.getInstance().maintainSkippedSongsPlaylist()) {
                final int songProgressMs = getSongProgressMillis();
                final int songDurationMs = getSongDurationMillis();
                if ((songProgressMs > SKIP_THRESHOLD_MS) // not just started
                        && (songDurationMs - songProgressMs > SKIP_THRESHOLD_MS) // not about to end
                ) {
                    // Mark the current song as skipped
                    final Song song = getCurrentSong();
                    final long playlistId = MusicUtil.getOrCreateSkippedPlaylist(this).id;
                    if (!PlaylistsUtil.doesPlaylistContain(playlistId, song.id)) {
                        PlaylistsUtil.addToPlaylist(this, song, playlistId, true);
                    }
                }
            }

            if (playbackHandlerThread.isAlive()) {
                playbackHandler.removeMessages(PLAY_SONG);
                playbackHandler.obtainMessage(PLAY_SONG, position, 0).sendToTarget();
            }
        }
    }

    public void setPosition(final int position) {
        synchronized (this) {
            if (playbackHandlerThread.isAlive()) {
                playbackHandler.removeMessages(SET_POSITION);
                playbackHandler.obtainMessage(SET_POSITION, position, 0).sendToTarget();
            }
        }
    }

    void setPositionToNextPosition() {
        synchronized (this) {
            playingQueue.setPositionToNextPosition();
        }
    }

    void playSongAtImpl(int position) {
        if (openTrackAndPrepareNextAt(position)) {
            play();
        } else {
            SafeToast.show(this, getResources().getString(R.string.unplayable_file));
        }
    }

    public void pause() {
        synchronized (this) {
            pausedByTransientLossOfFocus = false;
            if (playback.isPlaying()) {
                playback.pause();
                notifyChange(PLAY_STATE_CHANGED);
            }
        }
    }

    public void play() {
        synchronized (this) {
            if (requestFocus()) {
                if (!playback.isPlaying()) {
                    if (!playback.isInitialized()) {
                        playSongAt(getPosition(), false);
                    } else {
                        playback.start();
                        if (!becomingNoisyReceiverRegistered) {
                            registerReceiver(becomingNoisyReceiver, becomingNoisyReceiverIntentFilter);
                            becomingNoisyReceiverRegistered = true;
                        }
                        if (notHandledMetaChangedForCurrentTrack) {
                            handleChangeInternal(META_CHANGED);
                            notHandledMetaChangedForCurrentTrack = false;
                        }
                        notifyChange(PLAY_STATE_CHANGED);

                        // fixes a bug where the volume would stay ducked because the AudioManager.AUDIOFOCUS_GAIN event is not sent
                        if (playbackHandlerThread.isAlive()) {
                            playbackHandler.removeMessages(DUCK);
                            playbackHandler.sendEmptyMessage(UNDUCK);
                        }
                    }
                }
            } else {
                SafeToast.show(this, getResources().getString(R.string.audio_focus_denied));
            }
        }
    }

    private void applyReplayGain() {
        synchronized (this) {
            byte mode = PreferenceUtil.getInstance().getReplayGainSourceMode();
            if (mode != PreferenceUtil.RG_SOURCE_MODE_NONE) {
                Song song = getCurrentSong();

                float adjustDB = 0.0f;
                float peak = 1.0f;

                float rgTrack = song.replayGainTrack;
                float rgAlbum = song.replayGainAlbum;
                float rgpTrack = song.replayGainPeakTrack;
                float rgpAlbum = song.replayGainPeakAlbum;

                if (mode == PreferenceUtil.RG_SOURCE_MODE_ALBUM) {
                    adjustDB = (rgTrack == 0.0f ? adjustDB : rgTrack);
                    adjustDB = (rgAlbum == 0.0f ? adjustDB : rgAlbum);
                    peak = (rgpTrack == 1.0f ? peak : rgpTrack);
                    peak = (rgpAlbum == 1.0f ? peak : rgpAlbum);
                } else if (mode == PreferenceUtil.RG_SOURCE_MODE_TRACK) {
                    adjustDB = (rgAlbum == 0.0f ? adjustDB : rgAlbum);
                    adjustDB = (rgTrack == 0.0f ? adjustDB : rgTrack);
                    peak = (rgpAlbum == 1.0f ? peak : rgpAlbum);
                    peak = (rgpTrack == 1.0f ? peak : rgpTrack);
                }

                if (adjustDB == 0) {
                    adjustDB = PreferenceUtil.getInstance().getRgPreampWithoutTag();
                } else {
                    adjustDB += PreferenceUtil.getInstance().getRgPreampWithTag();

                    float peakDB = -20.0f * ((float) Math.log10(peak));
                    adjustDB = Math.min(adjustDB, peakDB);
                }

                playback.setReplayGain(adjustDB);
            } else {
                playback.setReplayGain(Float.NaN);
            }
        }
    }

    public void playPreviousSong(boolean skippedLast) {
        synchronized (this) {
            playSongAt(playingQueue.getPreviousPosition(skippedLast), skippedLast);
        }
    }

    public void back(boolean skippedLast) {
        if (getSongProgressMillis() > SKIP_THRESHOLD_MS) {
            seek(0);
        } else {
            playPreviousSong(skippedLast);
        }
    }

    public int getSongProgressMillis() {
        synchronized (this) {
            return playback.position();
        }
    }

    public int getSongDurationMillis() {
        synchronized (this) {
            return playback.duration();
        }
    }

    private long getQueueDurationMillis(int position) {
        synchronized (this) {
            return playingQueue.getQueueDurationMillis(position);
        }
    }

    public void seek(int millis) {
        synchronized (this) {
            if (playback != null) {
                playback.seek(millis);
            }
        }
        throttledSeekHandler.notifySeek();
    }

    void notifyChange(@NonNull final String what) {
        handleAndSendChangeInternal(what);
        sendPublicIntent(what);
    }

    void handleAndSendChangeInternal(@NonNull final String what) {
        handleChangeInternal(what);
        sendChangeInternal(what);
    }

    // to let other apps know whats playing. i.E. last.fm (scrobbling) or musixmatch
    void sendPublicIntent(@NonNull final String what) {
        final Intent intent = new Intent(what.replace(VINYL_MUSIC_PLAYER_PACKAGE_NAME, MUSIC_PACKAGE_NAME));

        final Song song = getCurrentSong();

        intent.putExtra("id", song.id);

        intent.putExtra("artist", MultiValuesTagUtil.infoString(song.artistNames));
        intent.putExtra("album", song.albumName);
        intent.putExtra("track", song.title);

        intent.putExtra("duration", song.duration);
        intent.putExtra("position", (long) getSongProgressMillis());

        intent.putExtra("playing", isPlaying());

        intent.putExtra("scrobbling_source", VINYL_MUSIC_PLAYER_PACKAGE_NAME);

        sendStickyBroadcast(intent);
    }

    private void sendChangeInternal(final String what) {
        sendBroadcast(new Intent(what));
        appWidgetBig.notifyChange(this, what);
        appWidgetClassic.notifyChange(this, what);
        appWidgetSmall.notifyChange(this, what);
        appWidgetCard.notifyChange(this, what);
    }

    private static final long MEDIA_SESSION_ACTIONS = PlaybackStateCompat.ACTION_PLAY
            | PlaybackStateCompat.ACTION_PAUSE
            | PlaybackStateCompat.ACTION_PLAY_PAUSE
            | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
            | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
            | PlaybackStateCompat.ACTION_STOP
            | PlaybackStateCompat.ACTION_SEEK_TO;

    private void handleChangeInternal(@NonNull final String what) {
        switch (what) {
            case PLAY_STATE_CHANGED:
                updateNotification();
                updateMediaSessionPlaybackState();
                final boolean isPlaying = isPlaying();
                if (!isPlaying && getSongProgressMillis() > 0) {
                    savePositionInTrack();
                }
                songPlayCountHelper.notifyPlayStateChanged(isPlaying);
                break;
            case FAVORITE_STATE_CHANGED:
                updateNotification();
                break;
            case META_CHANGED:
                updateNotification();
                updateMediaSessionMetaData();
                updateMediaSessionPlaybackState();

                savePosition();
                savePositionInTrack();
                applyReplayGain();

                final Song currentSong = getCurrentSong();
                HistoryStore.getInstance(this).addSongId(currentSong.id);
                if (PreferenceUtil.getInstance().maintainTopTrackPlaylist() && songPlayCountHelper.shouldBumpPlayCount()) {
                    SongPlayCountStore.getInstance(this).bumpPlayCount(songPlayCountHelper.getSong().id);
                }
                songPlayCountHelper.notifySongChanged(currentSong);
                break;
            case QUEUE_CHANGED:
                updateMediaSessionMetaData(); // because playing queue size might have changed
                saveState();
                int queueSize = 0;
                synchronized (this) {
                    queueSize = playingQueue.size();
                }

                if (queueSize > 0) {
                    prepareNext();
                } else {
                    updateNotification();
                }
                break;
        }
    }

    public int getAudioSessionId() {
        synchronized (this) {
            return playback.getAudioSessionId();
        }
    }

    public MediaSessionCompat getMediaSession() {
        synchronized (this) {
            return mediaSession;
        }
    }

    void releaseWakeLock() {
        if (wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    private void acquireWakeLock(long milli) {
        wakeLock.acquire(milli);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        synchronized (this) {
            switch (key) {
                case PreferenceUtil.GAPLESS_PLAYBACK:
                    if (sharedPreferences.getBoolean(key, false)) {
                        prepareNext();
                    } else {
                        playback.setNextDataSource(null);
                    }
                    break;
                case PreferenceUtil.COLORED_NOTIFICATION:
                    updateNotification();
                    break;
                case PreferenceUtil.CLASSIC_NOTIFICATION:
                    initNotification();
                    updateNotification();
                    break;
                case PreferenceUtil.TRANSPARENT_BACKGROUND_WIDGET:
                    sendChangeInternal(MusicService.META_CHANGED);
                    break;
                case PreferenceUtil.RG_SOURCE_MODE_V2:
                case PreferenceUtil.RG_PREAMP_WITH_TAG:
                case PreferenceUtil.RG_PREAMP_WITHOUT_TAG:
                    applyReplayGain();
                    break;
            }
        }
    }

    @Override
    public void onTrackWentToNext() {
        synchronized (this) {
            if (playbackHandlerThread.isAlive()) {
                playbackHandler.sendEmptyMessage(TRACK_WENT_TO_NEXT);
            }
        }
    }

    @Override
    public void onTrackEnded() {
        acquireWakeLock(30000);
        synchronized (this) {
            if (playbackHandlerThread.isAlive()) {
                playbackHandler.sendEmptyMessage(TRACK_ENDED);
            }
        }
    }

    public class MusicBinder extends Binder {
        @NonNull
        public MusicService getService() {
            return MusicService.this;
        }
    }

    private final BroadcastReceiver widgetIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String command = intent.getStringExtra(EXTRA_APP_WIDGET_NAME);
            if (command == null) {return;}

            final int[] ids = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
            switch (command) {
                case AppWidgetClassic.NAME: {
                    appWidgetClassic.performUpdate(MusicService.this, ids);
                    break;
                }
                case AppWidgetSmall.NAME: {
                    appWidgetSmall.performUpdate(MusicService.this, ids);
                    break;
                }
                case AppWidgetBig.NAME: {
                    appWidgetBig.performUpdate(MusicService.this, ids);
                    break;
                }
                case AppWidgetCard.NAME: {
                    appWidgetCard.performUpdate(MusicService.this, ids);
                    break;
                }
            }
        }
    };

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        // Check origin to ensure we're not allowing any arbitrary app to browse app contents
        if (!mPackageValidator.isKnownCaller(clientPackageName, clientUid)) {
            return null;
        }

        // System UI query (Android 11+)
        Predicate<Bundle> isSystemMediaQuery = (hints) -> {
            if (hints == null) {return false;}
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {return false;}
            if (hints.getBoolean(BrowserRoot.EXTRA_RECENT)) {return true;}
            if (hints.getBoolean(BrowserRoot.EXTRA_SUGGESTED)) {return true;}
            if (hints.getBoolean(BrowserRoot.EXTRA_OFFLINE)) {return true;}
            return false;
        };
        if (isSystemMediaQuery.test(rootHints)) {
            // By returning null, we explicitly disable support for content discovery/suggestions
            return null;
        }

        return new BrowserRoot(BrowsableMediaIDHelper.MEDIA_ID_ROOT, null);
    }

    @Override
    public void onLoadChildren(@NonNull final String parentId, @NonNull final Result<List<MediaBrowserCompat.MediaItem>> result) {
        result.sendResult(mBrowsableMusicProvider.getChildren(parentId, getResources()));
    }

    Playback getPlayback() {
        synchronized (this) {
            return playback;
        }
    }

    void setPausedByTransientLossOfFocus(boolean pausedByTransientLossOfFocus) {
        synchronized (this) {
            this.pausedByTransientLossOfFocus = pausedByTransientLossOfFocus;
        }
    }

    boolean isPausedByTransientLossOfFocus() {
        synchronized (this) {
            return pausedByTransientLossOfFocus;
        }
    }

    @NonNull
    public String getQueueInfoString() {
        synchronized (this) {
            final long duration = getQueueDurationMillis(getPosition());

            return MusicUtil.buildInfoString(
                    getResources().getString(R.string.up_next),
                    MusicUtil.getReadableDurationString(duration),
                    (getPosition() + 1) + "/" + playingQueue.size()
            );
        }
    }
}
