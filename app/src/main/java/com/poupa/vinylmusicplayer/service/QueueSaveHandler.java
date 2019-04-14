package com.poupa.vinylmusicplayer.service;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;

class QueueSaveHandler extends Handler {
    @NonNull
    private final WeakReference<MusicService> mService;

    public QueueSaveHandler(final MusicService service, @NonNull final Looper looper) {
        super(looper);
        mService = new WeakReference<>(service);
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        final MusicService service = mService.get();
        switch (msg.what) {
            case MusicService.SAVE_QUEUES:
                service.saveQueuesImpl();
                break;
        }
    }
}