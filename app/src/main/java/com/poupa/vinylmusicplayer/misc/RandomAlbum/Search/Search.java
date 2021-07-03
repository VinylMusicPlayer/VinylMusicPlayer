package com.poupa.vinylmusicplayer.misc.RandomAlbum.Search;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import android.content.Context;

import com.poupa.vinylmusicplayer.misc.RandomAlbum.History;
import com.poupa.vinylmusicplayer.model.Album;
import com.poupa.vinylmusicplayer.model.Song;


/*
 * NextRandomAlbum: Abstract class used to put the random album shared search function
 *
 */
abstract public class Search {

    protected ArrayList<Album> albumArrayList;      // List of album that validate search condition
    protected ArrayList<Integer> forbiddenPosition; // List of forbidden albumArrayList position (per history)
    protected ArrayList<Long> forbiddenId;          // List of forbidden album id (per history)

    protected int currentSongPosition;              // Position in albumArrayList of currently listen to song
    protected int currentlyShownNextRandomAlbumPosition;  // Position in albumArrayList of proposed next random album, if nextRandomAlbum doesn't follow current search condition position will be invalid
    protected Album currentlyShownNextRandomAlbum;        // Proposed next random album

    public boolean isManual() {
        return false;
    }

    // do album validate search condition set by song (same genre or artist ...)
    abstract public boolean searchTypeIsTrue(Song song, Album album);
    // implementation of the search algorithm
    abstract public Album foundNextAlbum(Song song, ArrayList<Album> albums, long currentlyShownNextRandomAlbumId, History listenHistory, History searchHistory, Context context);

    // init global variable to ensure foundNextAlbum can easily find something
    protected void constructPositionAlbum(Song song, ArrayList<Album> albums, long currentlyShownNextRandomAlbumId, History searchHistory, History listenHistory) {
        int i = 0;

        currentSongPosition = -1;
        currentlyShownNextRandomAlbumPosition = -1;
        forbiddenPosition = new ArrayList<>();
        forbiddenId = new ArrayList<>();
        albumArrayList = new ArrayList<>();
        currentlyShownNextRandomAlbum = null;

        for (Album album : albums) {
            if (searchTypeIsTrue(song, album)) { // condition depend on current search type
                if (album.getId() == song.albumId) { // album is same as current song album
                    currentSongPosition = i;
                } else if (album.getId() == currentlyShownNextRandomAlbumId) {
                    currentlyShownNextRandomAlbumPosition = i;
                } else {
                    if (isManual()) { // Manual search only look at searchHistory
                        if (History.isIdForbidden(album.getId(), searchHistory.getHistory())) {
                            forbiddenPosition.add(i);
                            forbiddenId.add(album.getId());
                        }
                    } else { // Automatic search only look at listenHistory
                        if (History.isIdForbidden(album.getId(), listenHistory.getHistory())) {
                            forbiddenPosition.add(i);
                            forbiddenId.add(album.getId());
                        }
                    }
                }

                albumArrayList.add(album);
                i++;
            }

            if (album.getId() == currentlyShownNextRandomAlbumId) {
                currentlyShownNextRandomAlbum = album;
            }
        }
    }

    protected boolean albumSearch() {
        return true;
    }

    protected boolean artistSearch(Song song, Album album) {
        return album.songs != null && album.songs.size() > 0 &&
                song.artistId == album.songs.get(0).artistId;
    }

    protected boolean genreSearch(Song song, Album album) {
        return album.songs != null && album.songs.size() > 0 &&
                song.genre.equals(album.songs.get(0).genre);
    }

    // Found a random int in [0, bound] that is not in forbiddenInteger
    public int randomIntInBoundWithForbiddenNumber(int bound, ArrayList<Integer> forbiddenInteger) {
        Collections.sort(forbiddenInteger); // sorting is needed to simplify randomize exclusion iteration

        int reduceBound = bound - forbiddenInteger.size();
        if (reduceBound >= 0) {
            // get a random position in reduce interval
            int random = new Random().nextInt(reduceBound);

            // if random is forbidden, add 1
            int previousForbiddenNumber = -1;
            for (int forbiddenNumber : forbiddenInteger) {
                if (forbiddenNumber != previousForbiddenNumber && random >= forbiddenNumber) {
                    random++;
                } else if (random < forbiddenNumber) {
                    break;
                }
                previousForbiddenNumber = forbiddenNumber;
            }
            return random;
        }
        return -1;
    }

    public int getAlbumIdPosition(ArrayList<Long> albumIdList, long albumId) {
        // necessary as albumId are unique but neither in order, neither consecutive (only certainty is albumId > 0),
        // thus only basic iteration can do this
        int i = 0;
        for (Long id : albumIdList) {
            if (id == albumId) {
                return i;
            }
            i++;
        }
        return -1;
    }

    // Get a next random album position in an album list of size albumSize that is not currentlyShownNextRandomAlbumPosition or forbiddenPositionOfArray
    // return ERROR_ if it is not possible
    public static final int ERROR_ARRAY_SIZE_IS_1 = -2;
    public static final int ERROR_ARRAY_SIZE_IS_0 = -3;
    public static final int ERROR_UNRESOLVED = -4;
    public static final int ERROR_HISTORY = -5;
    public int getRandomAlbumPosition(int albumSize, int currentSongPosition, int currentlyShownNextRandomAlbumPosition, ArrayList<Integer> forbiddenPositionOfArray) {
        int randomAlbumPosition;

        if (albumSize > 0) {
            int authorizeAlbumNumber;
            if (currentlyShownNextRandomAlbumPosition != -1) {
                authorizeAlbumNumber = albumSize - forbiddenPositionOfArray.size() - 2; // -2 is to take into account currentSongPosition and currentlyShownNextRandomAlbumPosition
            } else {
                authorizeAlbumNumber = albumSize - forbiddenPositionOfArray.size() - 1; // -1 is to take into account currentSongPosition
            }

            if (albumSize == 1) {
                randomAlbumPosition = ERROR_ARRAY_SIZE_IS_1; // nothing other than current song album can be found
            } else if (authorizeAlbumNumber >  0) {
                // Align authorizeAlbumNumber and forbiddenPositionOfArray
                randomAlbumPosition = getRandomPositionFollowingForbiddenOne(albumSize, currentSongPosition, currentlyShownNextRandomAlbumPosition, forbiddenPositionOfArray);
            } else { // something forbid to find a new album, I want to know what
                authorizeAlbumNumber = authorizeAlbumNumber + forbiddenPositionOfArray.size();

                if (authorizeAlbumNumber > 0) { // history forbid any new search
                    randomAlbumPosition = ERROR_HISTORY;
                } else {
                    randomAlbumPosition = ERROR_UNRESOLVED;
                }
            }
        } else {
            randomAlbumPosition = ERROR_ARRAY_SIZE_IS_0;
        }

        return randomAlbumPosition;
    }

    protected int getRandomPositionFollowingForbiddenOne(int albumSize, int currentSongPosition, int currentlyShownNextRandomAlbumPosition, ArrayList<Integer> forbiddenPositionOfArray) {
        ArrayList<Integer> forbiddenPosition = new ArrayList<>(forbiddenPositionOfArray);
        forbiddenPosition.add(currentSongPosition);
        if (currentlyShownNextRandomAlbumPosition != -1) {
            forbiddenPosition.add(currentlyShownNextRandomAlbumPosition);
        }

        // Get possible random album
        return randomIntInBoundWithForbiddenNumber(albumSize, forbiddenPosition);
    }
}

