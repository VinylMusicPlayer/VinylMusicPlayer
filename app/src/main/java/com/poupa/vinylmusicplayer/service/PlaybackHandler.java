package com.poupa.vinylmusicplayer.service;

import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.poupa.vinylmusicplayer.loader.AlbumLoader;
import com.poupa.vinylmusicplayer.misc.AlbumShuffling.NextRandomAlbum;
import com.poupa.vinylmusicplayer.model.Album;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

final class PlaybackHandler extends Handler {
    @NonNull
    private final WeakReference<MusicService> mService;
    private float currentDuckVolume = 1.0f;

    public PlaybackHandler(final MusicService service, @NonNull final Looper looper) {
        super(looper);
        mService = new WeakReference<>(service);
    }

    private boolean accessNextAlbum(MusicService service, int nextPosition) {
        if (nextPosition >= 0 && nextPosition < service.getPlayingQueue().size()) {
            Song song = service.getPlayingQueue().get(nextPosition);

            if (NextRandomAlbum.IsRandomAlbum(song.id) && !NextRandomAlbum.IsEmptyNextRandomAlbum(song.albumId)) { // next random album need to be loaded in
                // Add last listen song to next random album history to stop looping on the same three song
                Song previousSong = service.getPlayingQueue().get(nextPosition - 1);
                NextRandomAlbum.getInstance().commit(previousSong.albumId);

                // Load next album
                Album album = AlbumLoader.getAlbum(song.albumId);
                ArrayList<Song> songs = album.songs;

                service.clearQueue();
                service.openQueue(songs,0,true);

                service.setShuffleMode(MusicService.SHUFFLE_MODE_SHUFFLE_ALBUM);

                service.notifyChange(MusicService.QUEUE_CHANGED);

                return true;
            } else if (NextRandomAlbum.IsRandomAlbum(song.id)) { // if no next random album where found, stop playing
                stopPlaying(service);
                return true;
            }
        }
        return false;
    }

    private boolean stopPlaying(MusicService service) {
        service.notifyChange(MusicService.PLAY_STATE_CHANGED);
        service.seek(0);
        if (service.pendingQuit) {
            service.pendingQuit = false;
            service.quit();
            return true;
        }
        service.notifyChange(MusicService.PLAY_STATE_CHANGED);
        service.seek(0);
        return false;
    }

    @Override
    public void handleMessage(@NonNull final Message msg) {
        final MusicService service = mService.get();
        if (service == null) {
            return;
        }

        switch (msg.what) {
            case MusicService.DUCK:
                if (PreferenceUtil.getInstance().audioDucking()) {
                    currentDuckVolume -= .05f;
                    if (currentDuckVolume > .2f) {
                        sendEmptyMessageDelayed(MusicService.DUCK, 10);
                    } else {
                        currentDuckVolume = .2f;
                    }
                } else {
                    currentDuckVolume = 1f;
                }
                service.getPlayback().setDuckingFactor(currentDuckVolume);
                break;

            case MusicService.UNDUCK:
                if (PreferenceUtil.getInstance().audioDucking()) {
                    currentDuckVolume += .03f;
                    if (currentDuckVolume < 1f) {
                        sendEmptyMessageDelayed(MusicService.UNDUCK, 10);
                    } else {
                        currentDuckVolume = 1f;
                    }
                } else {
                    currentDuckVolume = 1f;
                }
                service.getPlayback().setDuckingFactor(currentDuckVolume);
                break;

            case MusicService.TRACK_WENT_TO_NEXT:
                if (service.getRepeatMode() == MusicService.REPEAT_MODE_NONE && service.isLastTrack()) {
                    service.pause();
                } else {
                    service.setPositionToNextPosition();
                    service.prepareNextImpl();
                    service.notifyChange(MusicService.META_CHANGED);
                }
                break;

            case MusicService.TRACK_ENDED:
                if (checkPendingQuit(service)) {break;}

                boolean shuffleModeAlbum = false;
                if (service.getShuffleMode() == MusicService.SHUFFLE_MODE_SHUFFLE_ALBUM) {
                    shuffleModeAlbum = accessNextAlbum(service, service.getNextPosition(false));
                }

                if (!shuffleModeAlbum) {
                    if (service.getRepeatMode() == MusicService.REPEAT_MODE_NONE && service.isLastTrack()) {
                        service.notifyChange(MusicService.PLAY_STATE_CHANGED);
                    } else {
                        service.playNextSong(false);
                    }
                }
                sendEmptyMessage(MusicService.RELEASE_WAKELOCK);
                break;

            case MusicService.RELEASE_WAKELOCK:
                service.releaseWakeLock();
                break;

            case MusicService.PLAY_SONG:
                shuffleModeAlbum = false;
                if (service.getShuffleMode() == MusicService.SHUFFLE_MODE_SHUFFLE_ALBUM) {
                    shuffleModeAlbum = accessNextAlbum(service, msg.arg1);
                }

                if (!shuffleModeAlbum) {
                    service.playSongAtImpl(msg.arg1);
                }
                break;

            case MusicService.SET_POSITION:
                service.openTrackAndPrepareNextAt(msg.arg1);
                service.notifyChange(MusicService.PLAY_STATE_CHANGED);
                break;

            case MusicService.PREPARE_NEXT:
                service.prepareNextImpl();
                break;

            case MusicService.RESTORE_QUEUES:
                service.restoreQueuesAndPositionIfNecessary();
                break;

            case MusicService.FOCUS_CHANGE:
                switch (msg.arg1) {
                    case AudioManager.AUDIOFOCUS_GAIN:
                        if (!service.isPlaying() && service.isPausedByTransientLossOfFocus()) {
                            service.play();
                            service.setPausedByTransientLossOfFocus(false);
                        }
                        removeMessages(MusicService.DUCK);
                        sendEmptyMessage(MusicService.UNDUCK);
                        break;

                    case AudioManager.AUDIOFOCUS_LOSS:
                        // Lost focus for an unbounded amount of time: stop playback and release media playback
                        service.pause();
                        break;

                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        // Lost focus for a short time, but we have to stop
                        // playback. We don't release the media playback because playback
                        // is likely to resume
                        boolean wasPlaying = service.isPlaying();
                        service.pause();
                        service.setPausedByTransientLossOfFocus(wasPlaying);
                        break;

                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        // Lost focus for a short time, but it's ok to keep playing
                        // at an attenuated level
                        removeMessages(MusicService.UNDUCK);
                        sendEmptyMessage(MusicService.DUCK);
                        break;
                }
                break;
        }
    }

    // if there is a timer finished, don't continue
    private boolean checkPendingQuit(@NonNull final MusicService service) {
        if (!service.pendingQuit) {return false;}

        service.notifyChange(MusicService.PLAY_STATE_CHANGED);
        service.pendingQuit = false;
        service.quit();
        return true;
    }
}
