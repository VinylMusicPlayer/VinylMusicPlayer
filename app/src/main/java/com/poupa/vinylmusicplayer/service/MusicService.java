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
import android.util.Log;
import android.widget.Toast;

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
import com.poupa.vinylmusicplayer.auto.AutoMediaIDHelper;
import com.poupa.vinylmusicplayer.auto.AutoMusicProvider;
import com.poupa.vinylmusicplayer.discog.tagging.MultiValuesTagUtil;
import com.poupa.vinylmusicplayer.glide.BlurTransformation;
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
import com.poupa.vinylmusicplayer.service.notification.PlayingNotification;
import com.poupa.vinylmusicplayer.service.notification.PlayingNotificationImpl;
import com.poupa.vinylmusicplayer.service.notification.PlayingNotificationImpl24;
import com.poupa.vinylmusicplayer.service.playback.Playback;
import com.poupa.vinylmusicplayer.util.MusicUtil;
import com.poupa.vinylmusicplayer.util.PackageValidator;
import com.poupa.vinylmusicplayer.util.PlaylistsUtil;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;
import com.poupa.vinylmusicplayer.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Karim Abou Zeid (kabouzeid), Andrew Neal
 */
public class MusicService extends MediaBrowserServiceCompat implements SharedPreferences.OnSharedPreferenceChangeListener, Playback.PlaybackCallbacks {

    public static final String TAG = MusicService.class.getSimpleName();

    public static final String VINYL_MUSIC_PLAYER_PACKAGE_NAME = "com.poupa.vinylmusicplayer";
    public static final String MUSIC_PACKAGE_NAME = "com.android.music";

    public static final String ACTION_TOGGLE_PAUSE = VINYL_MUSIC_PLAYER_PACKAGE_NAME + ".togglepause";
    public static final String ACTION_PLAY = VINYL_MUSIC_PLAYER_PACKAGE_NAME + ".play";
    public static final String ACTION_PLAY_PLAYLIST = VINYL_MUSIC_PLAYER_PACKAGE_NAME + ".play.playlist";
    public static final String ACTION_PAUSE = VINYL_MUSIC_PLAYER_PACKAGE_NAME + ".pause";
    public static final String ACTION_STOP = VINYL_MUSIC_PLAYER_PACKAGE_NAME + ".stop";
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

    public static final String FAVORITE_STATE_CHANGED = VINYL_MUSIC_PLAYER_PACKAGE_NAME + "favoritestatechanged";

    public static final String REPEAT_MODE_CHANGED = VINYL_MUSIC_PLAYER_PACKAGE_NAME + ".repeatmodechanged";
    public static final String SHUFFLE_MODE_CHANGED = VINYL_MUSIC_PLAYER_PACKAGE_NAME + ".shufflemodechanged";
    public static final String MEDIA_STORE_CHANGED = VINYL_MUSIC_PLAYER_PACKAGE_NAME + ".mediastorechanged";

    public static final String CYCLE_REPEAT = VINYL_MUSIC_PLAYER_PACKAGE_NAME + ".cyclerepeat";
    public static final String TOGGLE_SHUFFLE = VINYL_MUSIC_PLAYER_PACKAGE_NAME + ".toggleshuffle";
    public static final String TOGGLE_FAVORITE = VINYL_MUSIC_PLAYER_PACKAGE_NAME + ".togglefavorite";

    public static final String SAVED_POSITION = "POSITION";
    public static final String SAVED_POSITION_IN_TRACK = "POSITION_IN_TRACK";
    public static final String SAVED_SHUFFLE_MODE = "SHUFFLE_MODE";
    public static final String SAVED_REPEAT_MODE = "REPEAT_MODE";

    public static final int RELEASE_WAKELOCK = 0;
    public static final int TRACK_ENDED = 1;
    public static final int TRACK_WENT_TO_NEXT = 2;
    public static final int PLAY_SONG = 3;
    public static final int PREPARE_NEXT = 4;
    public static final int SET_POSITION = 5;
    public static final int FOCUS_CHANGE = 6;
    public static final int DUCK = 7;
    public static final int UNDUCK = 8;
    public static final int RESTORE_QUEUES = 9;

    public static final int RANDOM_START_POSITION_ON_SHUFFLE = StaticPlayingQueue.INVALID_POSITION;
    public static final int SHUFFLE_MODE_NONE = StaticPlayingQueue.SHUFFLE_MODE_NONE;
    public static final int SHUFFLE_MODE_SHUFFLE = StaticPlayingQueue.SHUFFLE_MODE_SHUFFLE;

    public static final int REPEAT_MODE_NONE = StaticPlayingQueue.REPEAT_MODE_NONE;
    public static final int REPEAT_MODE_ALL = StaticPlayingQueue.REPEAT_MODE_ALL;
    public static final int REPEAT_MODE_THIS = StaticPlayingQueue.REPEAT_MODE_THIS;

    public static final int SAVE_QUEUES = 0;

    private final IBinder musicBind = new MusicBinder();

    public boolean pendingQuit = false;

    private final AppWidgetBig appWidgetBig = AppWidgetBig.getInstance();
    private final AppWidgetClassic appWidgetClassic = AppWidgetClassic.getInstance();
    private final AppWidgetSmall appWidgetSmall = AppWidgetSmall.getInstance();
    private final AppWidgetCard appWidgetCard = AppWidgetCard.getInstance();

    private Playback playback;

    private StaticPlayingQueue playingQueue = new StaticPlayingQueue();

    private boolean queuesRestored;
    private boolean pausedByTransientLossOfFocus;
    private PlayingNotification playingNotification;
    private AudioManager audioManager;
    private MediaSessionCompat mediaSession;
    private PowerManager.WakeLock wakeLock;
    private PlaybackHandler playerHandler;
    private final AudioManager.OnAudioFocusChangeListener audioFocusListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(final int focusChange) {
            playerHandler.obtainMessage(FOCUS_CHANGE, focusChange, 0).sendToTarget();
        }
    };
    private QueueSaveHandler queueSaveHandler;
    private HandlerThread musicPlayerHandlerThread;
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

    private MediaSessionCallback mMediaSessionCallback;

    private PackageValidator mPackageValidator;

    private AutoMusicProvider mMusicProvider;

    private static String getTrackUri(@NonNull Song song) {
        return MusicUtil.getSongFileUri(song.id).toString();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        final PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        wakeLock.setReferenceCounted(false);

        musicPlayerHandlerThread = new HandlerThread("PlaybackHandler");
        musicPlayerHandlerThread.start();
        playerHandler = new PlaybackHandler(this, musicPlayerHandlerThread.getLooper());

        playback = new MultiPlayer(this);
        playback.setCallbacks(this);

        setupMediaSession();

        // queue saving needs to run on a separate thread so that it doesn't block the playback handler events
        queueSaveHandlerThread = new HandlerThread("QueueSaveHandler", Process.THREAD_PRIORITY_BACKGROUND);
        queueSaveHandlerThread.start();
        queueSaveHandler = new QueueSaveHandler(this, queueSaveHandlerThread.getLooper());

        uiThreadHandler = new Handler();

        registerReceiver(widgetIntentReceiver, new IntentFilter(APP_WIDGET_UPDATE));
        registerReceiver(updateFavoriteReceiver, new IntentFilter(FAVORITE_STATE_CHANGED));

        initNotification();

        mediaStoreObserver = new MediaStoreObserver(this, playerHandler);
        throttledSeekHandler = new ThrottledSeekHandler(this, playerHandler);
        getContentResolver().registerContentObserver(
                MediaStore.Audio.Media.INTERNAL_CONTENT_URI, true, mediaStoreObserver);
        getContentResolver().registerContentObserver(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, true, mediaStoreObserver);

        PreferenceUtil.getInstance().registerOnSharedPreferenceChangedListener(this);

        restoreState();

        mPackageValidator = new PackageValidator(this, R.xml.allowed_media_browser_callers);
        mMusicProvider = new AutoMusicProvider(this);

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
        ComponentName mediaButtonReceiverComponentName = new ComponentName(getApplicationContext(), MediaButtonIntentReceiver.class);

        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setComponent(mediaButtonReceiverComponentName);

        PendingIntent mediaButtonReceiverPendingIntent = PendingIntentCompat.getBroadcast(getApplicationContext(), 0, mediaButtonIntent, 0);

        mMediaSessionCallback = new MediaSessionCallback(this, getApplicationContext());
        mediaSession = new MediaSessionCompat(this, "VinylMusicPlayer", mediaButtonReceiverComponentName, mediaButtonReceiverPendingIntent);
        mediaSession.setCallback(mMediaSessionCallback);
        mediaSession.setActive(true);
        mediaSession.setMediaButtonReceiver(mediaButtonReceiverPendingIntent);
        setSessionToken(mediaSession.getSessionToken());
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        if (intent != null) {
            if (intent.getAction() != null) {
                restoreQueuesAndPositionIfNecessary();
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
                            ArrayList<Song> playlistSongs = playlist.getSongs(getApplicationContext());
                            if (!playlistSongs.isEmpty()) {
                                openQueue(playlistSongs, MusicService.RANDOM_START_POSITION_ON_SHUFFLE, true, shuffleMode);
                            } else {
                                Toast.makeText(getApplicationContext(), R.string.playlist_is_empty, Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.playlist_is_empty, Toast.LENGTH_LONG).show();
                        }
                        break;
                    case ACTION_REWIND:
                        back(true);
                        break;
                    case ACTION_SKIP:
                        playNextSong(true);
                        break;
                    case TOGGLE_FAVORITE:
                        MusicUtil.toggleFavorite(getApplicationContext(), getCurrentSong());
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
        if (becomingNoisyReceiverRegistered) {
            unregisterReceiver(becomingNoisyReceiver);
            becomingNoisyReceiverRegistered = false;
        }
        mediaSession.setActive(false);
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

    public void saveQueuesImpl() {
        MusicPlaybackQueueStore.getInstance(this).saveQueues(playingQueue.getPlayingQueue(), playingQueue.getOriginalPlayingQueue());
    }

    private void savePosition() {
        PreferenceManager.getDefaultSharedPreferences(this).edit().putInt(SAVED_POSITION, getPosition()).apply();
    }

    public void savePositionInTrack() {
        PreferenceManager.getDefaultSharedPreferences(this).edit().putInt(SAVED_POSITION_IN_TRACK, getSongProgressMillis()).apply();
    }

    public void saveState() {
        saveQueues();
        savePosition();
        savePositionInTrack();
    }

    private void saveQueues() {
        queueSaveHandler.removeMessages(SAVE_QUEUES);
        queueSaveHandler.sendEmptyMessage(SAVE_QUEUES);
    }

    private void restoreState() {
        playingQueue.restoreMode(
                PreferenceManager.getDefaultSharedPreferences(this).getInt(SAVED_SHUFFLE_MODE, 0),
                PreferenceManager.getDefaultSharedPreferences(this).getInt(SAVED_REPEAT_MODE, 0));
        handleAndSendChangeInternal(SHUFFLE_MODE_CHANGED);
        handleAndSendChangeInternal(REPEAT_MODE_CHANGED);

        playerHandler.removeMessages(RESTORE_QUEUES);
        playerHandler.sendEmptyMessage(RESTORE_QUEUES);
    }

    public synchronized void restoreQueuesAndPositionIfNecessary() {
        if (!queuesRestored && playingQueue.size()==0) {
            ArrayList<IndexedSong> restoredQueue = MusicPlaybackQueueStore.getInstance(this).getSavedPlayingQueue();
            ArrayList<IndexedSong> restoredOriginalQueue = MusicPlaybackQueueStore.getInstance(this).getSavedOriginalPlayingQueue();
            int restoredPosition = PreferenceManager.getDefaultSharedPreferences(this).getInt(SAVED_POSITION, -1);
            int restoredPositionInTrack = PreferenceManager.getDefaultSharedPreferences(this).getInt(SAVED_POSITION_IN_TRACK, -1);

            if (restoredQueue.size() > 0 && restoredQueue.size() == restoredOriginalQueue.size() && restoredPosition != -1) {
                try {
                    playingQueue = new StaticPlayingQueue(restoredQueue, restoredOriginalQueue, restoredPosition, playingQueue.getShuffleMode());
                } catch (ArrayIndexOutOfBoundsException queueCopiesOutOfSync) {
                    // fallback, when the copies of the restored queues are out of sync or the queues are corrupted
                    Log.e(TAG, "Restored queues are corrupted", queueCopiesOutOfSync);
                    final int shuffleMode = playingQueue.getShuffleMode();
                    playingQueue = new StaticPlayingQueue();
                    playingQueue.setShuffle(shuffleMode);
                }

                openCurrent();
                prepareNext();

                if (restoredPositionInTrack > 0) seek(restoredPositionInTrack);

                notHandledMetaChangedForCurrentTrack = true;
                sendChangeInternal(META_CHANGED);
                sendChangeInternal(QUEUE_CHANGED);
            }
        }
        queuesRestored = true;
    }

    public void quit() {
        pause();
        playingNotification.stop();

        closeAudioEffectSession();
        getAudioManager().abandonAudioFocus(audioFocusListener);
        stopSelf();
    }

    private void releaseResources() {
        playerHandler.removeCallbacksAndMessages(null);
        if (Build.VERSION.SDK_INT >= 18) {
            musicPlayerHandlerThread.quitSafely();
        } else {
            musicPlayerHandlerThread.quit();
        }
        queueSaveHandler.removeCallbacksAndMessages(null);
        if (Build.VERSION.SDK_INT >= 18) {
            queueSaveHandlerThread.quitSafely();
        } else {
            queueSaveHandlerThread.quit();
        }
        playback.release();
        playback = null;
        mediaSession.release();
    }

    public boolean isPlaying() {
        return playback != null && playback.isPlaying();
    }

    public boolean isPlaying(@NonNull Song song) {
        if (!isPlaying()) {return false;}

        return getCurrentIndexedSong().isQuickEqual(song);
    }

    public int getPosition() {
        return playingQueue.getCurrentPosition();
    }

    public void playNextSong(boolean force) {
        if (force && PreferenceUtil.getInstance().maintainSkippedSongsPlaylist()) {
            final long playlistId = MusicUtil.getOrCreateSkippedPlaylist(this).id;
            PlaylistsUtil.addToPlaylist(this, getCurrentSong(), playlistId, true);
        }

        playSongAt(playingQueue.getNextPosition(force));
    }

    public boolean openTrackAndPrepareNextAt(int position) {
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
                return playback.setDataSource(getTrackUri(getCurrentSong()));
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    private void prepareNext() {
        playerHandler.removeMessages(PREPARE_NEXT);
        playerHandler.obtainMessage(PREPARE_NEXT).sendToTarget();
    }

    public void prepareNextImpl() {
        synchronized (this) {
            try {
                int nextPosition = playingQueue.getNextPosition(false);
                if (getRepeatMode() == MusicService.REPEAT_MODE_NONE && playingQueue.isLastTrack()) {
                    playback.setNextDataSource(null);
                } else {
                    playback.setNextDataSource(getTrackUri(getSongAt(nextPosition)));
                }
                playingQueue.setNextPosition(nextPosition);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void closeAudioEffectSession() {
        final Intent audioEffectsIntent = new Intent(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION);
        audioEffectsIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, playback.getAudioSessionId());
        audioEffectsIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
        sendBroadcast(audioEffectsIntent);
    }

    private boolean requestFocus() {
        return (getAudioManager().requestAudioFocus(audioFocusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
    }

    public void initNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !PreferenceUtil.getInstance().classicNotification()) {
            playingNotification = new PlayingNotificationImpl24();
        } else {
            playingNotification = new PlayingNotificationImpl();
        }
        playingNotification.init(this);
    }

    public void updateNotification() {
        if (playingNotification != null && getCurrentSong().id != -1) {
            playingNotification.update();
        }
    }

    private final BroadcastReceiver updateFavoriteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            updateNotification();
        }
    };

    public void updateMediaSessionPlaybackState() {
        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(MEDIA_SESSION_ACTIONS)
                .setState(isPlaying() ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED,
                        getSongProgressMillis(), 1);

        setCustomAction(stateBuilder);

        mediaSession.setPlaybackState(stateBuilder.build());
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

        final int shuffleIcon = playingQueue.getShuffleMode() == MusicService.SHUFFLE_MODE_NONE ? R.drawable.ic_shuffle_white_nocircle_48dp : R.drawable.ic_shuffle_white_circle_48dp;
        stateBuilder.addCustomAction(new PlaybackStateCompat.CustomAction.Builder(
                TOGGLE_SHUFFLE, getString(R.string.action_toggle_shuffle), shuffleIcon)
                .build());

        final int favoriteIcon = MusicUtil.isFavorite(getApplicationContext(), getCurrentSong()) ? R.drawable.ic_favorite_white_circle_48dp : R.drawable.ic_favorite_border_white_nocircle_48dp;
        stateBuilder.addCustomAction(new PlaybackStateCompat.CustomAction.Builder(
                TOGGLE_FAVORITE, getString(R.string.action_toggle_favorite), favoriteIcon)
                .build());
    }

    private void updateMediaSessionMetaData() {
        final Song song = getCurrentSong();

        if (song.id == -1) {
            mediaSession.setMetadata(null);
            return;
        }

        final MediaMetadataCompat.Builder metaData = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, MultiValuesTagUtil.infoString(song.artistNames))
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST, MultiValuesTagUtil.infoString(song.albumArtistNames))
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, song.albumName)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, song.duration)
                .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, getPosition() + 1)
                .putLong(MediaMetadataCompat.METADATA_KEY_YEAR, song.year)
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            metaData.putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, playingQueue.size());
        }

        if (PreferenceUtil.getInstance().albumArtOnLockscreen()) {
            final Point screenSize = Util.getScreenSize(MusicService.this);

            GlideRequest<Bitmap> request = GlideApp.with(MusicService.this)
                    .asBitmap()
                    .load(VinylGlideExtension.getSongModel(song))
                    .transition(VinylGlideExtension.getDefaultTransition())
                    .songOptions(song);
            if (PreferenceUtil.getInstance().blurredAlbumArt()) {
                request.transform(new BlurTransformation.Builder(MusicService.this).build());
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    request.into(new VinylSimpleTarget<Bitmap>(screenSize.x, screenSize.y) {
                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            super.onLoadFailed(errorDrawable);
                            mediaSession.setMetadata(metaData.build());
                        }

                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> glideAnimation) {
                            metaData.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, copy(resource));
                            mediaSession.setMetadata(metaData.build());
                        }
                    });
                }
            });
        } else {
            mediaSession.setMetadata(metaData.build());
        }
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
        return (Song)getCurrentIndexedSong();
    }

    public IndexedSong getCurrentIndexedSong() {
        return getIndexedSongAt(getPosition());
    }

    public Song getSongAt(int position) {
        return (Song)getIndexedSongAt(position);
    }

    public IndexedSong getIndexedSongAt(int position) {
        if (position >= 0 && position < playingQueue.size()) {
            return playingQueue.getPlayingQueue().get(position);
        } else {
            return IndexedSong.EMPTY_INDEXED_SONG;
        }
    }

    public boolean isLastTrack() {
        return playingQueue.isLastTrack();
    }

    public ArrayList<Song> getPlayingQueue() {
        return playingQueue.getPlayingQueueSongOnly();
    }

    public int getRepeatMode() {
        return playingQueue.getRepeatMode();
    }

    public void setRepeatMode(final int repeatMode) {
        playingQueue.setRepeatMode(repeatMode);

        propagateRepeatChange();
    }

    public void cycleRepeatMode() {
        playingQueue.cycleRepeatMode();

        propagateRepeatChange();
    }


    private void propagateRepeatChange() {
        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putInt(SAVED_REPEAT_MODE, playingQueue.getRepeatMode())
                .apply();
        prepareNext();
        handleAndSendChangeInternal(REPEAT_MODE_CHANGED);
    }

    private void propagateShuffleChange() {
        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putInt(SAVED_SHUFFLE_MODE, playingQueue.getShuffleMode())
                .apply();
        handleAndSendChangeInternal(SHUFFLE_MODE_CHANGED);
        notifyChange(QUEUE_CHANGED);
    }

    public int getShuffleMode() {
        return playingQueue.getShuffleMode();
    }

    public void toggleShuffle() {
        playingQueue.toggleShuffle();

        propagateShuffleChange();
    }

    public void setShuffleMode(final int shuffleMode) {
        playingQueue.setShuffle(shuffleMode);

        propagateShuffleChange();
    }

    public void openQueue(@Nullable final ArrayList<Song> playingQueue, final int startPosition, final boolean startPlaying, final int shuffleMode) {
        int position = startPosition;
        if (playingQueue != null && shuffleMode != MusicService.SHUFFLE_MODE_NONE && startPosition == MusicService.RANDOM_START_POSITION_ON_SHUFFLE) {
            position = new Random().nextInt(playingQueue.size());
        }

        if (this.playingQueue.openQueue(playingQueue, position, startPlaying, MusicService.SHUFFLE_MODE_NONE)) {

            setShuffleMode(shuffleMode);

            if (startPlaying) {
                playSongAt(this.playingQueue.getCurrentPosition());
            } else {
                setPosition(position);
            }
            notifyChange(QUEUE_CHANGED);
        }
    }

    public void openQueue(@Nullable final ArrayList<Song> playingQueue, final int startPosition, final boolean startPlaying) {
        openQueue(playingQueue, startPosition, startPlaying, this.playingQueue.getShuffleMode());
    }

    public void addSongAfter(int position, Song song) {
        playingQueue.addAfter(position, song);
        notifyChange(QUEUE_CHANGED);
    }

    public void addSongBackTo(int position, IndexedSong song) {
        playingQueue.addSongBackTo(position, song);
        notifyChange(QUEUE_CHANGED);
    }

    public void addSongsAfter(int position, List<Song> songs) {
        playingQueue.addAllAfter(position, songs);
        notifyChange(QUEUE_CHANGED);
    }

    public void addSong(Song song) {
        playingQueue.add(song);
        notifyChange(QUEUE_CHANGED);
    }

    public void addSongs(List<Song> songs) {
        playingQueue.addAll(songs);
        notifyChange(QUEUE_CHANGED);
    }

    public void removeSong(int position) { // better to test is playing here and have only one signal than calling playNextSong and then removeSong (two signal need time in between to work ok)
        boolean isPlaying = isPlaying(playingQueue.getPlayingQueue().get(position));

        int newPosition = playingQueue.remove(position);
        if (newPosition != -1) {
            if (isPlaying)
                playSongAt(newPosition);
            else
                setPosition(newPosition);
        }

        notifyChange(QUEUE_CHANGED);
    }

    public void removeSongs(@NonNull List<Song> songs) {
        int newPosition = playingQueue.removeSongs(songs);
        if (newPosition != -1) {
            setPosition(newPosition);
        }

        notifyChange(QUEUE_CHANGED);
    }

    public void moveSong(int from, int to) {
        playingQueue.move(from, to);

        notifyChange(QUEUE_CHANGED);
    }

    public void clearQueue() {
        playingQueue.clear();

        setPosition(-1);
        notifyChange(QUEUE_CHANGED);
    }

    public void playSongAt(final int position) {
        // handle this on the handlers thread to avoid blocking the ui thread
        playerHandler.removeMessages(PLAY_SONG);
        playerHandler.obtainMessage(PLAY_SONG, position, 0).sendToTarget();
    }

    public void setPosition(final int position) {
        // handle this on the handlers thread to avoid blocking the ui thread
        playerHandler.removeMessages(SET_POSITION);
        playerHandler.obtainMessage(SET_POSITION, position, 0).sendToTarget();
    }

    public void setPositionToNextPosition() {
        playingQueue.setPositionToNextPosition();
    }

    public void playSongAtImpl(int position) {
        if (openTrackAndPrepareNextAt(position)) {
            play();
        } else {
            Toast.makeText(this, getResources().getString(R.string.unplayable_file), Toast.LENGTH_SHORT).show();
        }
    }

    public void pause() {
        pausedByTransientLossOfFocus = false;
        if (playback.isPlaying()) {
            playback.pause();
            notifyChange(PLAY_STATE_CHANGED);
        }
    }

    public void play() {
        synchronized (this) {
            if (requestFocus()) {
                if (!playback.isPlaying()) {
                    if (!playback.isInitialized()) {
                        playSongAt(getPosition());
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
                        playerHandler.removeMessages(DUCK);
                        playerHandler.sendEmptyMessage(UNDUCK);
                    }
                }
            } else {
                Toast.makeText(this, getResources().getString(R.string.audio_focus_denied), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void applyReplayGain() {
        byte mode = PreferenceUtil.getInstance().getReplayGainSourceMode();
        if (mode != PreferenceUtil.RG_SOURCE_MODE_NONE) {
            Song song = getCurrentSong();

            float adjust = 0f;
            float rgTrack = song.replayGainTrack;
            float rgAlbum = song.replayGainAlbum;

            if (mode == PreferenceUtil.RG_SOURCE_MODE_ALBUM) {
                adjust = (rgTrack != 0 ? rgTrack : adjust);
                adjust = (rgAlbum != 0 ? rgAlbum : adjust);
            } else if (mode == PreferenceUtil.RG_SOURCE_MODE_TRACK) {
                adjust = (rgAlbum != 0 ? rgAlbum : adjust);
                adjust = (rgTrack != 0 ? rgTrack : adjust);
            }

            if (adjust == 0) {
                adjust = PreferenceUtil.getInstance().getRgPreampWithoutTag();
            } else {
                adjust += PreferenceUtil.getInstance().getRgPreampWithTag();
            }

            float rgResult = ((float) Math.pow(10, (adjust / 20)));
            rgResult = Math.max(0, Math.min(1, rgResult));

            playback.setReplayGain(rgResult);
        } else {
            playback.setReplayGain(Float.NaN);
        }
    }

    public void playPreviousSong(boolean force) {
        playSongAt(playingQueue.getPreviousPosition(force));
    }

    public void back(boolean force) {
        if (getSongProgressMillis() > 5000) {
            seek(0);
        } else {
            playPreviousSong(force);
        }
    }

    public int getSongProgressMillis() {
        return playback.position();
    }

    public int getSongDurationMillis() {
        return playback.duration();
    }

    public long getQueueDurationMillis(int position) {
        return playingQueue.getQueueDurationMillis(position);
    }

    public void seek(int millis) {
        synchronized (this) {
            try {
                playback.seek(millis);
                throttledSeekHandler.notifySeek();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void notifyChange(@NonNull final String what) {
        handleAndSendChangeInternal(what);
        sendPublicIntent(what);
    }

    public void handleAndSendChangeInternal(@NonNull final String what) {
        handleChangeInternal(what);
        sendChangeInternal(what);
    }

    // to let other apps know whats playing. i.E. last.fm (scrobbling) or musixmatch
    public void sendPublicIntent(@NonNull final String what) {
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
                if (playingQueue.size() > 0) {
                    prepareNext();
                } else {
                    playingNotification.stop();
                }
                break;
        }
    }

    public int getAudioSessionId() {
        return playback.getAudioSessionId();
    }

    public MediaSessionCompat getMediaSession() {
        return mediaSession;
    }

    public void releaseWakeLock() {
        if (wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    public void acquireWakeLock(long milli) {
        wakeLock.acquire(milli);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case PreferenceUtil.GAPLESS_PLAYBACK:
                if (sharedPreferences.getBoolean(key, false)) {
                    prepareNext();
                } else {
                    playback.setNextDataSource(null);
                }
                break;
            case PreferenceUtil.ALBUM_ART_ON_LOCKSCREEN:
            case PreferenceUtil.BLURRED_ALBUM_ART:
                updateMediaSessionMetaData();
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

    @Override
    public void onTrackWentToNext() {
        playerHandler.sendEmptyMessage(TRACK_WENT_TO_NEXT);
    }

    @Override
    public void onTrackEnded() {
        acquireWakeLock(30000);
        playerHandler.sendEmptyMessage(TRACK_ENDED);
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
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {return false;}
            if (hints.getBoolean(BrowserRoot.EXTRA_RECENT)) {return true;}
            if (hints.getBoolean(BrowserRoot.EXTRA_SUGGESTED)) {return true;}
            if (hints.getBoolean(BrowserRoot.EXTRA_OFFLINE)) {return true;}
            return false;
        };
        if (isSystemMediaQuery.test(rootHints)) {
            // By returning null, we explicitly disable support for content discovery/suggestions
            return null;
        }

        // TODO Limit to Android Auto
        return new BrowserRoot(AutoMediaIDHelper.MEDIA_ID_ROOT, null);
    }

    @Override
    public void onLoadChildren(@NonNull final String parentId, @NonNull final Result<List<MediaBrowserCompat.MediaItem>> result) {
        result.sendResult(mMusicProvider.getChildren(parentId, getResources()));
    }

    public Playback getPlayback() {
        return playback;
    }

    public void setPausedByTransientLossOfFocus(boolean pausedByTransientLossOfFocus) {
        this.pausedByTransientLossOfFocus = pausedByTransientLossOfFocus;
    }

    public boolean isPausedByTransientLossOfFocus() {
        return pausedByTransientLossOfFocus;
    }

    @NonNull
    public String getQueueInfoString() {
        final long duration = getQueueDurationMillis(getPosition());

        return MusicUtil.buildInfoString(
                getResources().getString(R.string.up_next),
                MusicUtil.getReadableDurationString(duration),
                (getPosition() + 1) + "/" + playingQueue.size()
        );
    }
}
