package com.poupa.vinylmusicplayer.helper;

import java.lang.ref.WeakReference;
import java.util.function.Consumer;

public class WeakMethodReference<T> implements Runnable {
    private final WeakReference<T> weakReference;
    private final Consumer<T> consumer;

    public WeakMethodReference(T containingObject, Consumer<T> consumer) {
        weakReference = new WeakReference<>(containingObject);
        this.consumer = consumer;
    }

    public void run() {
        T obj = weakReference.get();
        if (obj != null) {
            consumer.accept(obj);
        }
    }
}