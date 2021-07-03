package com.poupa.vinylmusicplayer.misc.RandomAlbum;


import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;


public class History {
    private ArrayList<Long> history;
    private ArrayList<Long> originalHistory;

    private final int historySize;
    private final DB database;

    public History(int historySize, boolean addDatabase) {
        this.historySize = historySize;

        history = new ArrayList<>();
        originalHistory = new ArrayList<>();

        if (addDatabase) { database = new DB(); }
        else { database = null; }
    }

    public ArrayList<Long> getHistory() {
        return history;
    }

    public void clearHistory() {
        history.clear();
    }

    public void clearOriginalHistory() {
        originalHistory.clear();
    }

    public void stop() {
        if (database != null) { database.clear(); }
        clearHistory();
        clearOriginalHistory();
    }

    public long popHistory() {
        if (history.size() > 0) {
            long id = history.get(0);
            history.remove(0);

            return id;
        }
        return -1;
    }

    public void revertHistory() {
        history = new ArrayList<>(originalHistory);
    }

    public void synchronizeHistory() {
        originalHistory = new ArrayList<>(history);
    }

    static public boolean isIdForbidden(long id, ArrayList<Long> array) {
        for (Long forbiddenId : array) {
            if (id == forbiddenId) {
                return true;
            }
        }
        return false;
    }

    public void addIdToHistory(long id, boolean updateDatabase) {
        if (history.size() >= historySize) {
            if (database != null && updateDatabase) { database.removeFirstAlbumOfHistory(); }
            history.remove(0);
            originalHistory.remove(0);
        }
        if (!isIdForbidden(id, history)) { // i don't want duplication in this array as it complicate what to do when no new random album can be found because of this array
            if (database != null && updateDatabase) { database.addIdToHistory(id); }
            history.add(id);
            originalHistory.add(id);
        }
    }

    synchronized Long fetchNextRandomAlbumId() {
        if (database != null) { return database.fetchNextRandomAlbumId(); }
        else { return (long)0; }
    }

    public void fetchHistory() {
        if (database != null) {
            List<Long> nextRandomAlbums = database.fetchAllListenHistory();

            for (int i = 0; i <= nextRandomAlbums.size() - 1; i++) {
                addIdToHistory(nextRandomAlbums.get(i), false);
            }
        }
    }

    public void setNextRandomAlbumId(@NonNull Long id) {
        if (database != null) {
            database.setNextRandomAlbumId(id);
        }
    }
}
