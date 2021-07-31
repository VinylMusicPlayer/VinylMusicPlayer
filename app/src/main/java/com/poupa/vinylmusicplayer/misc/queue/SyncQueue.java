package com.poupa.vinylmusicplayer.misc.queue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
 * SyncQueue: ArrayList which can be restored to a backup state
 *
 */
public class SyncQueue<T> {
    private ArrayList<T> queue;       // list of element currently saved
    private ArrayList<T> backupQueue; // copy of the queue used to allow revert of history last operation

    public SyncQueue() {
        queue = new ArrayList<>();
        backupQueue = new ArrayList<>();
    }

    public SyncQueue(ArrayList<T> queue, ArrayList<T> backupQueue) {
        this.queue = new ArrayList<>(queue);
        this.backupQueue = new ArrayList<>(backupQueue);
    }

    public void revert() {
        queue = new ArrayList<>(backupQueue);
    }

    public void commit() {
        backupQueue = new ArrayList<>(queue);
    }

    public void addAll(List<T> item, boolean rebase) {
        queue.addAll(item);

        if (rebase) {
            backupQueue.addAll(item);
        }
    }

    public void addAll(int index, List<T> items, boolean rebase) {
        queue.addAll(index, items);

        if (rebase) {
            backupQueue.addAll(index, items);
        }
    }

    public void add(T item, boolean rebase) {
        queue.add(item);

        if (rebase) {
            backupQueue.add(item);
        }
    }

    public void add(int index, T items, boolean rebase) {
        queue.add(index, items);

        if (rebase) {
            backupQueue.add(index, items);
        }
    }

    public void remove(int position, boolean rebase) {
        T object = queue.remove(position);

        if (rebase) {
            backupQueue.remove(object);
        }
    }

    public void removePreviousState(int position) {
        backupQueue.remove(position);
    }

    public void move(int from, int to, boolean rebase) {
        T songToMove = queue.remove(from);
        queue.add(to, songToMove);

        if (!rebase) {
            songToMove = backupQueue.remove(from);
            backupQueue.add(to, songToMove);
        }
    }

    public ArrayList<T> getAll() {
        return queue;
    }

    public ArrayList<T> getAllPreviousState() {
        return backupQueue;
    }

    public T get(int index) {
        return queue.get(index);
    }

    public T getPreviousState(int index) {
        return backupQueue.get(index);
    }

    public int size() {
        return queue.size();
    }

    public void clear() {
        queue.clear();
        backupQueue.clear();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    @Override
    public String toString() {
        return "originalQueue: " + Arrays.toString(backupQueue.toArray()) + ", queue: " + Arrays.toString(queue.toArray()) + "}";
    }
}
