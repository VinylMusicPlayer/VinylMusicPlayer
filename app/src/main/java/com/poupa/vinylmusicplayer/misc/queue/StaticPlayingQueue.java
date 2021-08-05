package com.poupa.vinylmusicplayer.misc.queue;


import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.poupa.vinylmusicplayer.helper.ShuffleHelper;
import com.poupa.vinylmusicplayer.model.Song;


public class StaticPlayingQueue {

    public static final int REPEAT_MODE_NONE = 0;
    public static final int REPEAT_MODE_ALL = 1;
    public static final int REPEAT_MODE_THIS = 2;

    private int repeatMode;

    public static final int SHUFFLE_MODE_NONE = 0;
    public static final int SHUFFLE_MODE_SHUFFLE = 1;
    private int shuffleMode;

    private int currentPosition;

    private int nextPosition;

    private boolean songsIsStale;
    private final ArrayList<Song> songs;              // necessary as all implementation use MusicPlayerRemove.getPlayingQueue and want a pointer that doesn't change to a list of song
    private ArrayList<IndexedSong> queue;             // list of element currently saved (way better than songs to ensure only the correct occurrence of a song is modified)
    private final ArrayList<IndexedSong> backupQueue; // copy of the queue used to allow revert of history last operation


    public StaticPlayingQueue() {
        songs = new ArrayList<>();
        queue = new ArrayList<>();
        backupQueue = new ArrayList<>();
        shuffleMode = SHUFFLE_MODE_NONE;
        currentPosition = -1;
        songsIsStale = false;
    }

    public StaticPlayingQueue(ArrayList<IndexedSong> restoreQueue, ArrayList<IndexedSong> restoreOriginalQueue, int restoredPosition, int shuffleMode) {
        this.queue = new ArrayList<>(restoreQueue);
        this.backupQueue = new ArrayList<>(restoreOriginalQueue);
        this.shuffleMode = shuffleMode;

        currentPosition = restoredPosition;

        songs = new ArrayList<>();
        songsIsStale = true;
        resetSongs();
    }

    public void restoreMode(int shuffleMode, int repeatMode) {
        this.shuffleMode = shuffleMode;
        this.repeatMode = repeatMode;
    }

    /* -------------------- queue modification (add, remove, move, ...) -------------------- */

    // add song at the end of both list
    public void add(Song song) {
        queue.add(new IndexedSong(song, queue.size()));
        backupQueue.add(new IndexedSong(song, backupQueue.size()));

        songsIsStale = true;
    }

    // add list of song at the end of both list
    public void addAll(@NonNull List<Song> songs) {
        for (Song song : songs) {
            add(song);
        }
    }

    // add song after and including position, numbering need to be redone for every song after this position (+1)
    public void addAfter(int position, Song song) {
        if (position >= queue.size()) {
            position = queue.size()-1;
        }
        int previousPosition = queue.get(position).index + 1;
        position = position + 1;

        backupQueue.add(previousPosition, new IndexedSong(song, previousPosition));
        queue.add(position, new IndexedSong(song, previousPosition));
        for (int i = 0; i < queue.size(); i++) {
            backupQueue.get(i).index = i;

            if (i != position && queue.get(i).index >= previousPosition) {
                queue.get(i).index = queue.get(i).index + 1;
            }
        }

        if (position < this.currentPosition) {
            this.currentPosition++;
        }

        songsIsStale = true;
    }

    public void addSongBackTo(int position, IndexedSong song) {
        int previousPosition = song.index;

        backupQueue.add(previousPosition, new IndexedSong(song.song, previousPosition));
        queue.add(position, new IndexedSong(song.song, previousPosition));
        for (int i = 0; i < queue.size(); i++) {
            backupQueue.get(i).index = i;

            if (i != position && queue.get(i).index >= previousPosition) {
                queue.get(i).index = queue.get(i).index + 1;
            }
        }

        if (position <= this.currentPosition) {
            this.currentPosition++;
        }

        songsIsStale = true;
    }

    // add songs after and including position, numbering need to be redone for every song after this position (+number of song)
    public void addAllAfter(int position, @NonNull List<Song> songs) {
        if (position >= queue.size()) {
            position = queue.size()-1;
        }
        int previousPosition = queue.get(position).index + 1;
        position = position + 1;

        int n = songs.size()-1;
        for (int i = n; i >= 0; i--) {
            backupQueue.add(previousPosition, new IndexedSong(songs.get(i), previousPosition + i));

            queue.add(position, new IndexedSong(songs.get(i), previousPosition + i));

            if (position < this.currentPosition) {
                this.currentPosition++;
            }
        }

        for (int i = 0; i < queue.size(); i++) {
            backupQueue.get(i).index = i;
            if (!(i >= position && i <= position+n) && queue.get(i).index >= previousPosition  ) {
                queue.get(i).index = queue.get(i).index + n + 1;
            }
        }

        songsIsStale = true;
    }

    // move song from from to to, position are conserved
    public void move(int from, int to) {
        if (from == to) return;
        final int currentPosition = this.currentPosition;

        IndexedSong songToMove = queue.remove(from);
        queue.add(to, songToMove);

        if (shuffleMode == SHUFFLE_MODE_NONE) {
            IndexedSong previousSongToMove = backupQueue.remove(from);
            backupQueue.add(to, previousSongToMove);

            for (int i = 0; i < queue.size(); i++) {
                queue.get(i).index = i;
                backupQueue.get(i).index = i;
            }
        }

        if (from > currentPosition && to <= currentPosition) {
            this.currentPosition = currentPosition + 1;
        } else if (from < currentPosition && to >= currentPosition) {
            this.currentPosition = currentPosition - 1;
        } else if (from == currentPosition) {
            this.currentPosition = to;
        }

        songsIsStale = true;
    }

    private int rePosition(int deletedPosition) {
        int position = this.currentPosition;

        if (deletedPosition < position) {
            this.currentPosition = position - 1;
        } else if (deletedPosition == position) { //the current position was deleted
            if (queue.size() > deletedPosition) {
                return position;
            } else {
                return position - 1;
            }
        }

        return -1;
    }

    // remove song at index position, numbering need to be redone for every song after this position (-1)
    public int remove(int position) {
        IndexedSong o = queue.remove(position);
        backupQueue.remove(o.index);

        for (int i = 0; i < queue.size(); i++) {
            backupQueue.get(i).index = i;
            if (queue.get(i).index >= o.index) {
                queue.get(i).index = queue.get(i).index - 1;
            }
        }

        songsIsStale = true;

        return rePosition(position);
    }

    private int removeAllOccurrence(Song song) {
        int hasPositionChanged = -1;

        for (int i = queue.size() - 1; i >= 0; i--) {
            if (queue.get(i).song.id == song.id) {
                int temp = remove(i);
                if (temp != -1) {
                    hasPositionChanged = temp;
                }
            }
        }

        songsIsStale = true;

        return hasPositionChanged;
    }

    public int removeSongs(@NonNull List<Song> songs) {
        int hasPositionChanged = -1;

        for (Song song : songs) {
            int temp = removeAllOccurrence(song);
            if (temp != -1) {
                hasPositionChanged = temp;
            }
        }

        return hasPositionChanged;
    }

    public void clear() {
        songs.clear();
        queue.clear();
        backupQueue.clear();
    }

    private void revert() {
        queue = new ArrayList<>(backupQueue);
        songsIsStale = true;
    }

    /* -------------------- queue getter info -------------------- */

    public boolean openQueue(@Nullable final ArrayList<Song> playingQueue, final int startPosition, final boolean startPlaying, int shuffleMode) {
        if (playingQueue != null && !playingQueue.isEmpty() && startPosition >= 0 && startPosition < playingQueue.size()) {
            clear();
            addAll(playingQueue);

            this.currentPosition = startPosition;

            setShuffle(shuffleMode);

            return true;
        }

        return false;
    }

    public ArrayList<IndexedSong> getPlayingQueue() {
        return queue;
    }

    public ArrayList<IndexedSong> getOriginalPlayingQueue() {
        return backupQueue;
    }

    private void resetSongs() {
        this.songs.clear();

        for (IndexedSong song : queue) {
            songs.add(song.song);
        }
        songsIsStale = false;
    }

    public ArrayList<Song> getPlayingQueueSongOnly() {
        if (songsIsStale)
            resetSongs();
        return songs;
    }

    public int size() {
        return queue.size();
    }

    /* -------------------- position method -------------------- */

    public int getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(int position) {
        if (position >= queue.size())
            return;

        currentPosition = position;
    }

    public void setPositionToNextPosition() {
        currentPosition = nextPosition;
    }

    public void setNextPosition(int position) {
        if (position >= queue.size())
            return;

        nextPosition = position;
    }

    public boolean isLastTrack() {
        return getCurrentPosition() == queue.size() - 1;
    }

    public int getNextPosition(boolean force) {
        int position = getCurrentPosition() + 1;
        switch (getRepeatMode()) {
            case REPEAT_MODE_ALL:
                if (isLastTrack()) {
                    position = 0;
                }
                break;
            case REPEAT_MODE_THIS:
                if (force) {
                    if (isLastTrack()) {
                        position = 0;
                    }
                } else {
                    position -= 1;
                }
                break;
            default:
            case REPEAT_MODE_NONE:
                if (isLastTrack()) {
                    position -= 1;
                }
                break;
        }
        return position;
    }

    public int getPreviousPosition(boolean force) {
        int newPosition = getCurrentPosition() - 1;
        switch (repeatMode) {
            case REPEAT_MODE_ALL:
                if (newPosition < 0) {
                    newPosition = queue.size() - 1;
                }
                break;
            case REPEAT_MODE_THIS:
                if (force) {
                    if (newPosition < 0) {
                        newPosition = queue.size() - 1;
                    }
                } else {
                    newPosition = getCurrentPosition();
                }
                break;
            default:
            case REPEAT_MODE_NONE:
                if (newPosition < 0) {
                    newPosition = 0;
                }
                break;
        }
        return newPosition;
    }

    /* -------------------- song getter info -------------------- */

    public IndexedSong getIndexedSongAt(int position) {
        return queue.get(position);
    }

    public long getQueueDurationMillis(int position){
        long duration = 0;
        for (int i = position + 1; i < queue.size(); i++)
            duration += queue.get(i).song.duration;
        return duration;
    }

    /* -------------------- shuffle method -------------------- */

    public void setShuffle(int shuffleMode) {
        if (this.shuffleMode == shuffleMode)
            return;

        switch (shuffleMode) {
            case SHUFFLE_MODE_NONE:
                currentPosition = queue.get(currentPosition).index;
                revert();
                break;
            case SHUFFLE_MODE_SHUFFLE:
                ShuffleHelper.makeShuffleListWithIndexes(queue, currentPosition);
                songsIsStale = true;
                currentPosition = 0;
                break;
        }

        this.shuffleMode = shuffleMode;
    }

    public void toggleShuffle() {
        switch (shuffleMode) {
            case SHUFFLE_MODE_NONE:
                setShuffle(SHUFFLE_MODE_SHUFFLE);
                break;
            case SHUFFLE_MODE_SHUFFLE:
                setShuffle(SHUFFLE_MODE_NONE);
                break;
        }
    }

    public int getShuffleMode() {
        return shuffleMode;
    }

    /* -------------------- repeat method -------------------- */
    public void setRepeatMode(final int repeatMode) {
        if (this.repeatMode == repeatMode)
            return;

        switch (repeatMode) {
            case REPEAT_MODE_NONE:
            case REPEAT_MODE_ALL:
            case REPEAT_MODE_THIS:
                this.repeatMode = repeatMode;
                break;
        }
    }

    public int getRepeatMode() {
        return repeatMode;
    }

    public void cycleRepeatMode() {
        switch (getRepeatMode()) {
            case REPEAT_MODE_NONE:
                setRepeatMode(REPEAT_MODE_ALL);
                break;
            case REPEAT_MODE_ALL:
                setRepeatMode(REPEAT_MODE_THIS);
                break;
            default:
                setRepeatMode(REPEAT_MODE_NONE);
                break;
        }
    }
}
