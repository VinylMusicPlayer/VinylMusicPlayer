package com.poupa.vinylmusicplayer.ui.activities.base;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.helper.MusicPlayerRemote;
import com.poupa.vinylmusicplayer.interfaces.MusicServiceEventListener;
import com.poupa.vinylmusicplayer.service.MusicService;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public abstract class AbsMusicServiceActivity extends AbsBaseActivity implements MusicServiceEventListener {

    private final ArrayList<MusicServiceEventListener> mMusicServiceEventListeners = new ArrayList<>();

    private Activity boundActivity;

    // TODO Merge this with the boolean right after
    private MusicStateReceiver musicStateReceiver;
    private boolean receiverRegistered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO Do this non UI code only on fresh start, ie when savedInstanceState == null.Combine with onRestoreInstanceState as well
        boundActivity = MusicPlayerRemote.bindToService(this, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                AbsMusicServiceActivity.this.onServiceConnected();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                AbsMusicServiceActivity.this.onServiceDisconnected();
            }
        });

        // TODO For debug only
        final String message = String.format(
                "AbsMusicServiceActivity@%s.onCreate activity=%s",
                Integer.toHexString(System.identityHashCode(this)),
                Integer.toHexString(System.identityHashCode(boundActivity))
        );
        Log.w("extended-sleep", message);

        setPermissionDeniedMessage(getString(R.string.permission_external_storage_denied));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MusicPlayerRemote.unbindFromService(boundActivity);

        // TODO For debug only
        final String message = String.format(
                "AbsMusicServiceActivity@%s.onDestroy activity=%s receiverRegistered=%s",
                Integer.toHexString(System.identityHashCode(this)),
                Integer.toHexString(System.identityHashCode(boundActivity)),
                receiverRegistered
        );
        Log.w("extended-sleep", message);

        if (receiverRegistered) {
            unregisterReceiver(musicStateReceiver);
            receiverRegistered = false;
        }
    }

    public void addMusicServiceEventListener(@NonNull final MusicServiceEventListener listener) {
        mMusicServiceEventListeners.add(listener);
    }

    public void removeMusicServiceEventListener(@NonNull final MusicServiceEventListener listener) {
        mMusicServiceEventListeners.remove(listener);
    }

    @Override
    public void onServiceConnected() {
        // TODO For debug only
        final String message = String.format(
                "AbsMusicServiceActivity@%s.onServiceConnected receiverRegistered=%s",
                Integer.toHexString(System.identityHashCode(this)),
                receiverRegistered
        );
        Log.w("extended-sleep", message);

        if (!receiverRegistered) {
            musicStateReceiver = new MusicStateReceiver(this);

            final IntentFilter filter = new IntentFilter();
            filter.addAction(MusicService.PLAY_STATE_CHANGED);
            filter.addAction(MusicService.SHUFFLE_MODE_CHANGED);
            filter.addAction(MusicService.REPEAT_MODE_CHANGED);
            filter.addAction(MusicService.META_CHANGED);
            filter.addAction(MusicService.QUEUE_CHANGED);
            filter.addAction(MusicService.MEDIA_STORE_CHANGED);
            filter.addAction(MusicService.FAVORITE_STATE_CHANGED);

            registerReceiver(musicStateReceiver, filter);
            // TODO Context-registered receivers receive broadcasts as long as their registering context is valid.
            // For an example, if you register within an Activity context, you receive broadcasts as
            // long as the activity is not destroyed. If you register with the Application context,
            // you receive broadcasts as long as the app is running.

            receiverRegistered = true;
        }

        for (MusicServiceEventListener listener : mMusicServiceEventListeners) {
            listener.onServiceConnected();
        }
    }

    @Override
    public void onServiceDisconnected() {
        // TODO This is not called!!!
        // TODO For debug only
        final String message = String.format(
                "AbsMusicServiceActivity@%s.onServiceDisconnected receiverRegisteredd=%s",
                Integer.toHexString(System.identityHashCode(this)),
                receiverRegistered
        );
        Log.w("extended-sleep", message);

        if (receiverRegistered) {
            unregisterReceiver(musicStateReceiver);
            receiverRegistered = false;
        }

        for (MusicServiceEventListener listener : mMusicServiceEventListeners) {
            listener.onServiceDisconnected();
        }
    }

    @Override
    public void onPlayingMetaChanged() {
        for (MusicServiceEventListener listener : mMusicServiceEventListeners) {
            listener.onPlayingMetaChanged();
        }
    }

    @Override
    public void onQueueChanged() {
        for (MusicServiceEventListener listener : mMusicServiceEventListeners) {
            listener.onQueueChanged();
        }
    }

    @Override
    public void onPlayStateChanged() {
        for (MusicServiceEventListener listener : mMusicServiceEventListeners) {
            listener.onPlayStateChanged();
        }
    }

    @Override
    public void onMediaStoreChanged() {
        for (MusicServiceEventListener listener : mMusicServiceEventListeners) {
            listener.onMediaStoreChanged();
        }
    }

    @Override
    public void onRepeatModeChanged() {
        for (MusicServiceEventListener listener : mMusicServiceEventListeners) {
            listener.onRepeatModeChanged();
        }
    }

    @Override
    public void onShuffleModeChanged() {
        for (MusicServiceEventListener listener : mMusicServiceEventListeners) {
            listener.onShuffleModeChanged();
        }
    }

    private static final class MusicStateReceiver extends BroadcastReceiver {

        private final WeakReference<AbsMusicServiceActivity> reference;

        public MusicStateReceiver(final AbsMusicServiceActivity activity) {
            reference = new WeakReference<>(activity);
        }

        @Override
        public void onReceive(final Context context, @NonNull final Intent intent) {
            final String action = intent.getAction();
            AbsMusicServiceActivity activity = reference.get();

            // TODO For debug only
            final String message = String.format(
                    "AbsMusicServiceActivity@%s.onReceive context=%s action=%s destinationActivity=%s@%s",
                    Integer.toHexString(System.identityHashCode(this)),
                    Integer.toHexString(System.identityHashCode(context)),
                    action,
                    activity,
                    Integer.toHexString(System.identityHashCode(activity))
            );
            Log.w("extended-sleep", message);

            if (activity != null) {
                switch (action) {
                    case MusicService.FAVORITE_STATE_CHANGED:
                    case MusicService.META_CHANGED:
                        activity.onPlayingMetaChanged();
                        break;
                    case MusicService.QUEUE_CHANGED:
                        activity.onQueueChanged();
                        break;
                    case MusicService.PLAY_STATE_CHANGED:
                        activity.onPlayStateChanged();
                        break;
                    case MusicService.REPEAT_MODE_CHANGED:
                        activity.onRepeatModeChanged();
                        break;
                    case MusicService.SHUFFLE_MODE_CHANGED:
                        activity.onShuffleModeChanged();
                        break;
                    case MusicService.MEDIA_STORE_CHANGED:
                        activity.onMediaStoreChanged();
                        break;
                }
            }
        }
    }

    @Override
    protected void onHasPermissionsChanged(boolean hasPermissions) {
        super.onHasPermissionsChanged(hasPermissions);
        Intent intent = new Intent(MusicService.MEDIA_STORE_CHANGED);
        intent.putExtra("from_permissions_changed", true); // just in case we need to know this at some point
        sendBroadcast(intent);
    }

    @Nullable
    @Override
    protected String[] getPermissionsToRequest() {
        return new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    }
}
