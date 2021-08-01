package com.poupa.vinylmusicplayer.misc.queue;


import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.poupa.vinylmusicplayer.helper.ShuffleHelper;
import com.poupa.vinylmusicplayer.model.Song;

import static com.poupa.vinylmusicplayer.service.MusicService.SHUFFLE_MODE_SHUFFLE;


public class ShufflingQueue {

    private boolean isShuffled;
    private int currentPosition;

    private int nextPosition; // is this really needed ???
    public SyncQueue<PositionSong> queue; // should be private

    public ShufflingQueue() {
        queue = new SyncQueue<>();
        isShuffled = false;
        currentPosition = -1;
    }

    public ShufflingQueue(ArrayList<PositionSong> restoreQueue, ArrayList<PositionSong> restoreOriginalQueue, int restoredPosition, boolean isShuffled) {
        queue = new SyncQueue<>(restoreQueue, restoreOriginalQueue);
        this.isShuffled = isShuffled;

        currentPosition = restoredPosition;
    }

    // This should be remove and only previous one should be used
    public ShufflingQueue(ArrayList<Song> restoreQueue, ArrayList<Song> restoreOriginalQueue, int restoredPosition) {
        queue = new SyncQueue<>();
        isShuffled = false;

        ArrayList<PositionSong> test = new ArrayList<>();
        int i=0;
        for (Song song : restoreQueue) {
            PositionSong toto = new PositionSong(song, i);
            test.add(toto);
            i++;
        }
        queue.getAll().addAll(test);

        ArrayList<PositionSong> test2 = new ArrayList<>();
        i=0;
        for (Song song : restoreOriginalQueue) {
            PositionSong toto = new PositionSong(song, i);
            test2.add(toto);
            i++;
        }
        queue.getAllPreviousState().addAll(test2);

        currentPosition = restoredPosition;
    }

    /* -------------------- queue modification (add, remove, move, ...) -------------------- */

    // add song at the end of both list
    public void add(Song song) {
        queue.getAll().add(new PositionSong(song, queue.getAll().size()));
        queue.getAllPreviousState().add(new PositionSong(song, queue.getAllPreviousState().size()));
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
        int oldSongAtPosition_Position = queue.get(position).position+1;
        position = position+1;

        queue.getAllPreviousState().add(oldSongAtPosition_Position, new PositionSong(song, oldSongAtPosition_Position));
        queue.getAll().add(position, new PositionSong(song, oldSongAtPosition_Position));
        for (int i = 0; i < queue.getAll().size(); i++) {
            queue.getAllPreviousState().get(i).position = i;

            if (i != position && queue.getAll().get(i).position >= oldSongAtPosition_Position) {
                queue.getAll().get(i).position = queue.getAll().get(i).position + 1;
            }
        }
    }

    public void addSongBackTo(int position, PositionSong song) {
        int oldSongAtPosition_Position = song.position;

        queue.getAllPreviousState().add(oldSongAtPosition_Position, new PositionSong(song.song, oldSongAtPosition_Position));
        queue.getAll().add(position, new PositionSong(song.song, oldSongAtPosition_Position));
        for (int i = 0; i < queue.getAll().size(); i++) {
            queue.getAllPreviousState().get(i).position = i;

            if (i != position && queue.getAll().get(i).position >= oldSongAtPosition_Position) {
                queue.getAll().get(i).position = queue.getAll().get(i).position + 1;
            }
        }
    }

    // add songs after and including position, numbering need to be redone for every song after this position (+number of song)
    public void addAllAfter(int position, @NonNull List<Song> songs) {
        if (position >= queue.size()) {
            position = queue.size()-1;
        }
        int oldSongAtPosition_Position = queue.get(position).position+1;
        position = position+1;

        int n = songs.size()-1;
        for (int i = n; i >= 0; i--) {
            queue.getAllPreviousState().add(oldSongAtPosition_Position, new PositionSong(songs.get(i), oldSongAtPosition_Position + i));

            queue.getAll().add(position, new PositionSong(songs.get(i), oldSongAtPosition_Position + i));
        }

        for (int i = 0; i < queue.getAll().size(); i++) {
            queue.getAllPreviousState().get(i).position = i;
            if (!(i >= position && i <= position+n) && queue.getAll().get(i).position >= oldSongAtPosition_Position) {
                queue.getAll().get(i).position = queue.getAll().get(i).position + n + 1;
            }
        }
    }

    // move song from from to to, position are conserved
    public void move(int from, int to) {
        if (from == to) return;
        final int currentPosition = this.currentPosition;

        PositionSong songToMove = queue.getAll().remove(from);
        queue.getAll().add(to, songToMove);

        if (!isShuffled) {
            PositionSong previousSongToMove = queue.getAllPreviousState().remove(from);
            queue.getAllPreviousState().add(to, previousSongToMove);

            for (int i = 0; i < queue.getAll().size(); i++) {
                queue.getAll().get(i).position = i;
                queue.getAllPreviousState().get(i).position = i;
            }
        }

        if (from > currentPosition && to <= currentPosition) {
            this.currentPosition = currentPosition + 1;
        } else if (from < currentPosition && to >= currentPosition) {
            this.currentPosition = currentPosition - 1;
        } else if (from == currentPosition) {
            this.currentPosition = to;
        }
    }

    private int rePosition(int deletedPosition) {
        int position = this.currentPosition;

        if (deletedPosition < position) {
            this.currentPosition = position - 1;
        } else if (deletedPosition == position) { //the current position was deleted
            if (queue.getAll().size() > deletedPosition) {
                return position;
            } else {
                return position - 1;
            }
        }

        return -1;
    }

    // remove song at index position, numbering need to be redone for every song after this position (-1)
    public int remove(int position) {
        PositionSong o = queue.getAll().remove(position);
        queue.getAllPreviousState().remove(o.position);

        for (int i = 0; i < queue.getAll().size(); i++) {
            queue.getAllPreviousState().get(i).position = i;
            if (queue.getAll().get(i).position >= o.position) {
                queue.getAll().get(i).position = queue.getAll().get(i).position - 1;
            }
        }

        return rePosition(position);
    }

    private int removeAllOccurrence(Song song) {
        int hasPositionChanged = -1;

        for (int i = queue.getAll().size() - 1; i >= 0; i--) {
            if (queue.getAll().get(i).song.id == song.id) {
                int temp = remove(i);
                if (temp != -1) {
                    hasPositionChanged = temp;
                }
            }
        }

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
        queue.clear();
    }

    /* -------------------- queue getter info -------------------- */

    public boolean openQueue(@Nullable final ArrayList<Song> playingQueue, final int startPosition, final boolean startPlaying, int shuffleMode) {
        if (playingQueue != null && !playingQueue.isEmpty() && startPosition >= 0 && startPosition < playingQueue.size()) {
            queue.clear();
            addAll(playingQueue);

            this.currentPosition = startPosition;

            if (shuffleMode == SHUFFLE_MODE_SHUFFLE) {
                setShuffle(true);
            }
            return true;
        }

        return false;
    }

    public ArrayList<Song> getPlayingQueue() { //stop using a SyncQueue and just put orig queue and position list in this class
        ArrayList<Song> songs = new ArrayList<>();

        for (PositionSong song : queue.getAll()) {
            songs.add(song.song);
        }
        return songs;
    }

    public ArrayList<Song> getOriginalPlayingQueue() { //stop using a SyncQueue and just put orig queue and position list in this class
        ArrayList<Song> songs = new ArrayList<>();

        for (PositionSong song : queue.getAllPreviousState()) {
            songs.add(song.song);
        }
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

    /* -------------------- song getter info -------------------- */

    public PositionSong getPositionSongAt(int position) {
        return queue.get(position);
    }

    public long getQueueDurationMillis(int position){
        long duration = 0;
        for (int i = position + 1; i < queue.size(); i++)
            duration += queue.getAll().get(i).song.duration;
        return duration;
    }

    /* -------------------- shuffle method -------------------- */

    public void setShuffle(boolean shuffle) {
        if (shuffle == isShuffled)
            return;

        if (!shuffle) {
            currentPosition = queue.getAll().get(currentPosition).position;
            queue.revert();
        } else {
            ShuffleHelper.makeShuffleListTest(queue.getAll(), currentPosition);
            currentPosition = 0;
        }

        isShuffled = shuffle;
    }

    public boolean getShuffleMode() {
        return isShuffled;
    }
}
