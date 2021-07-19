package com.poupa.vinylmusicplayer.misc.AlbumShuffling;


import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;


/*
 * History: buffer with max size, if no more space, first added element will be removed
 *          a connection to a database can be done to allow to save the history even if app reboot
 *          /!\ only one database exist, thus if more that one history instance connect to the database, chaos will ensue
 *
 *          This buffer ensure that data are unique and in order of add operation
 */
public class History {
    private ArrayList<Long> history;         // list of element currently saved
    private ArrayList<Long> originalHistory; // copy of the history used to allow revert of history last operation

    private int maxSize;
    private final DB database;

    public History(int maxSize, boolean addDatabase) {
        this.maxSize = maxSize;

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

    public void setHistorySize(int size) {
        this.maxSize = size;
    }

    // Get first element added and remove it
    public long popHistory() {
        if (history.size() > 0) {
            long id = history.get(0);
            history.remove(0);

            return id;
        }
        return -1;
    }

    // undo last operation done on history
    public void revertHistory(boolean updateDatabase) {
        history = new ArrayList<>(originalHistory);

        if (updateDatabase) {
            synchronizeDatabase();
        }
    }

    // set originalHistory up to date with current history
    public void synchronizeHistory() {
        originalHistory = new ArrayList<>(history);
    }

    public void setHistory(History init) {
        this.originalHistory = new ArrayList<>(init.originalHistory);
        this.history = new ArrayList<>(init.history);
    }

    // set database up to date with current history
    public void synchronizeDatabase() {
        if (database != null) {
            database.clear();

            for (int i = 0; i < history.size(); i++) {
                database.addIdToHistory(history.get(i));
            }
        }
    }

    // get if id can be put in history and follow the no duplicate policy
    static public boolean isIdForbidden(long id, ArrayList<Long> array) {
        for (Long forbiddenId : array) {
            if (id == forbiddenId) {
                return true;
            }
        }
        return false;
    }

    // add new if if possible (no duplicate) and remove old one if needed
    public void addIdToHistory(long id, boolean updateDatabase) {
        while (history.size() >= maxSize) {
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

    // reset history to the database state
    public void fetchHistory() {
        if (database != null) {
            List<Long> nextRandomAlbums = database.fetchAllListenHistory();

            for (int i = 0; i <= nextRandomAlbums.size() - 1; i++) {
                addIdToHistory(nextRandomAlbums.get(i), false);
            }
        }
    }

    // get database NextRandomAlbum id
    synchronized Long fetchNextRandomAlbumId() {
        if (database != null) { return database.fetchNextRandomAlbumId(); }
        else { return (long)0; }
    }

    // set database NextRandomAlbum id
    public void setNextRandomAlbumId(@NonNull Long id) {
        if (database != null) {
            database.setNextRandomAlbumId(id);
        }
    }
}
