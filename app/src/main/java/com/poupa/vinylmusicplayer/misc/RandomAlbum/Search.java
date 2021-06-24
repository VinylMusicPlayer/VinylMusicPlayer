package com.poupa.vinylmusicplayer.misc.RandomAlbum;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import android.content.Context;

import com.poupa.vinylmusicplayer.model.Album;
import com.poupa.vinylmusicplayer.model.Song;


abstract public class Search {

    protected int lastSongPosition;
    protected int nextRandomAlbumPosition;
    protected ArrayList<Integer> forbiddenPosition;
    protected ArrayList<Long> forbiddenId;
    protected ArrayList<Album> albumArrayList;
    protected Album nextRandomAlbum;

    public boolean isManual() {
        return false;
    }

    abstract public boolean searchTypeIsTrue(Song song, Album album);
    abstract public Album foundNextAlbum(Song song, ArrayList<Album> albums, long nextRandomAlbumId, History listenHistory, History searchHistory, Context context);

    protected void constructPositionAlbum(Song song, ArrayList<Album> albums, long nextRandomAlbumId, History searchHistory, History listenHistory) {
        int i = 0;

        lastSongPosition = -1;
        nextRandomAlbumPosition = -1;
        forbiddenPosition = new ArrayList<>();
        forbiddenId = new ArrayList<>();
        albumArrayList = new ArrayList<>();
        nextRandomAlbum = null;

        for (Album album : albums) {
            if (searchTypeIsTrue(song, album)) { // condition depend of search type
                if (album.getId() == song.albumId) {
                    lastSongPosition = i;
                } else if (album.getId() == nextRandomAlbumId) {
                    nextRandomAlbumPosition = i;
                } else {
                    if (isManual()) {
                        if (History.isIdForbidden(album.getId(), searchHistory.getHistory())) {
                            forbiddenPosition.add(i);
                            forbiddenId.add(album.getId());
                        }
                    } else {
                        if (History.isIdForbidden(album.getId(), listenHistory.getHistory())) {
                            forbiddenPosition.add(i);
                            forbiddenId.add(album.getId());
                        }
                    }
                }

                albumArrayList.add(album);
                i++;
            }

            if (album.getId() == nextRandomAlbumId) {
                nextRandomAlbum = album;
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

    public int randomIntWithinForbiddenNumber(int bound, ArrayList<Integer> forbiddenPosition) {
        Collections.sort(forbiddenPosition); // sorting is needed to get sweet why of getting randomize exclusion

        int random = new Random().nextInt(bound - forbiddenPosition.size());
        int previousForbiddenNumber = -1;
        for (int forbiddenNumber : forbiddenPosition) {
            if (forbiddenNumber != previousForbiddenNumber && random >= forbiddenNumber) {
                random++;
            }
            previousForbiddenNumber = forbiddenNumber;
        }
        return random;
    }

    // necessary as albumId are unique but neither in order, neither consecutive (only certainty is albumId > 0)
    public int getAlbumIdPosition(ArrayList<Long> albumIdList, long albumId) {
        int i = 0;
        for (Long id : albumIdList) {
            if (id == albumId) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public static final int ERROR_ARRAY_SIZE_IS_1 = -2;
    public static final int ERROR_ARRAY_SIZE_IS_0 = -3;
    public static final int ERROR_UNRESOLVED = -4;
    public static final int ERROR_HISTORY = -5;
    public int getRandomAlbumPosition(int albumSize, int lastSongPosition, int nextRandomAlbumPosition, ArrayList<Integer> forbiddenPositionOfArray) {
        int randomAlbumPosition;

        if (albumSize > 0) {
            int authorizeAlbumNumber;
            if (nextRandomAlbumPosition != -1) {
                authorizeAlbumNumber = albumSize - forbiddenPositionOfArray.size() - 2; // -1 is to take into account lastSongPosition and nextRandomAlbumPosition
            } else {
                authorizeAlbumNumber = albumSize - forbiddenPositionOfArray.size() - 1; // -1 is to take into account lastSongPosition
            }

            if (albumSize == 1) {
                randomAlbumPosition = ERROR_ARRAY_SIZE_IS_1;
            } else if (albumSize == 2) {
                randomAlbumPosition = (lastSongPosition + 1) % albumSize;
            } else if (authorizeAlbumNumber >  0) {
                ArrayList<Integer> forbiddenPosition = new ArrayList<>();
                forbiddenPosition.add(lastSongPosition);
                if (nextRandomAlbumPosition != -1) {
                    forbiddenPosition.add(nextRandomAlbumPosition);
                }
                forbiddenPosition.addAll(forbiddenPositionOfArray);

                randomAlbumPosition = randomIntWithinForbiddenNumber(albumSize, forbiddenPosition);
            } else {
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
}

