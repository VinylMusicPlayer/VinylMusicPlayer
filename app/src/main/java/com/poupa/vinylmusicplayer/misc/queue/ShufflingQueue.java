package com.poupa.vinylmusicplayer.misc.queue;


import java.util.List;

import androidx.annotation.NonNull;
import com.poupa.vinylmusicplayer.helper.ShuffleHelper;
import com.poupa.vinylmusicplayer.model.Song;


public class ShufflingQueue {

    private boolean isShuffled;
    private int currentPosition;
    public SyncQueue<PositionSong> queue;

    public ShufflingQueue() {
        queue = new SyncQueue<>();
        isShuffled = false;
        currentPosition = -1;
    }

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

    private boolean rePosition(int deletedPosition) {
        int position = this.currentPosition;

        if (deletedPosition < position) {
            this.currentPosition = position - 1;
        } else if (deletedPosition == position) { //the current position was deleted
            if (queue.getAll().size() > deletedPosition) {
                this.currentPosition = position;
            } else {
                this.currentPosition = position - 1;
            }
            return true;
        }

        return false;
    }

    // remove song at index position, numbering need to be redone for every song after this position (-1)
    public boolean remove(int position) {
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

    private boolean removeAllOccurrence(Song song) {
        boolean hasPositionChanged = false;

        for (int i = queue.getAll().size() - 1; i >= 0; i--) {
            if (queue.getAll().get(i).song.id == song.id) {
                //queue.getAll().remove(i);
                if (remove(i)) {
                    hasPositionChanged = true;
                }
            }
            /*if (queue.getAllPreviousState().get(i).song.id == song.id) {
                queue.getAllPreviousState().remove(i);
            }*/
        }

        return hasPositionChanged;
    }

    public boolean removeSongs(@NonNull List<Song> songs) {
        boolean hasPositionChanged = false;

        for (Song song : songs) {
            if (removeAllOccurrence(song)) {
                hasPositionChanged = true;
            }
        }

        /*
        // can know be done in MusicService
        if (hasPositionChanged) {
            setPosition(newPosition);
        } */

        return hasPositionChanged;
    }

    public int getPosition() {
        return currentPosition;
    }

    public void setPosition(int position) {
        if (position >= queue.size())
            return;

        currentPosition = position;
    }

    public void toggleShuffle() {
        isShuffled = !isShuffled;

        if (!isShuffled) {
            queue.revert();
        } else {
            ShuffleHelper.makeShuffleListTest(queue.getAll(), currentPosition);
        }
    }
}
