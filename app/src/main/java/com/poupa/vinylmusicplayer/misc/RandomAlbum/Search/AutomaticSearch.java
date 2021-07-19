package com.poupa.vinylmusicplayer.misc.RandomAlbum.Search;


import java.util.ArrayList;

import android.content.Context;
import android.util.Log;

import com.poupa.vinylmusicplayer.misc.RandomAlbum.History;
import com.poupa.vinylmusicplayer.model.Album;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;


public class AutomaticSearch extends Search {
    @Override
    public boolean isManual() {
        return false;
    }

    int searchType;
    int fallbackLevel;

    public AutomaticSearch() {
        super();
        initSearchType();
    }

    public final int ARTIST_SEARCH = 1;
    public final int GENRE_SEARCH = 2;
    public final int RANDOM_SEARCH = 3;
    public boolean searchTypeIsTrue(Song song, Album album) {
        switch (searchType) {
            case ARTIST_SEARCH:
                return artistSearch(song, album);
            case GENRE_SEARCH:
                return genreSearch(song, album);
            case RANDOM_SEARCH:
                return albumSearch();
            default:
                return false;
        }
    }

    @Override
    public Album foundNextAlbum(Song song, ArrayList<Album> albums, long previousNextRandomAlbumId, History listenHistory, History searchHistory, Context context) {
        Album album = null;

        constructPositionAlbum(song, albums, previousNextRandomAlbumId, searchHistory, listenHistory);

        int albumPosition = getRandomAlbumPosition(albumArrayList.size(), currentSongPosition, currentlyShownNextRandomAlbumPosition, forbiddenPosition);

        if (albumPosition >= 0) {
            album = albumArrayList.get(albumPosition);
        } else if (setNextSearchType()) { // will be use when fallback is Implemented
                //listenHistory.revertHistory(); really ??? only if error_history is taken into account (doesn't seems to be useful for auto)
                //set new search type via global variable + exit condition with null album
            return foundNextAlbum(song, albums, previousNextRandomAlbumId, listenHistory,
                    searchHistory, context);
        }

        return album;
    }

    // Preference Case:
    //  [ARTIST_SEARCH]
    //  [GENRE_SEARCH]
    //  [RANDOM_SEARCH]
    //  [ARTIST_SEARCH, GENRE_SEARCH]
    //  [GENRE_SEARCH, ARTIST_SEARCH]
    //  [ARTIST_SEARCH, GENRE_SEARCH, RANDOM_SEARCH]
    //  [GENRE_SEARCH, ARTIST_SEARCH, RANDOM_SEARCH]

    // Preference 1
    //  - Type of automatic search: artist | genre | random
    // If not random, add option:
    //  - Type of automatic fallback (if search criteria return nothing): genre | genre & random | random  ||  artist | artist & random | random

    // Preference 2
    //  - Type of automatic search: artist | genre | random
    // If not random, add option:
    //  - Type of automatic fallback (if search criteria return nothing): genre | random  ||  artist | random
    // If not random, add option:
    //  - Type of second automatic fallback (if first fallback criteria return nothing): none | random  ||  none | random

    // Preference History size ??

    // preference should give me a array so that i can iterate on it
    private void initSearchType() {
        fallbackLevel = 0;
        setNextSearchType();
    }

    private boolean setNextSearchType() {
        boolean hasNext = true;

        fallbackLevel++;

        String key;
        if (fallbackLevel == 1)
            key = PreferenceUtil.SEARCH_STYLE;
        else if (fallbackLevel == 2)
            key = PreferenceUtil.SECONDARY_SEARCH_STYLE;
        else
            key = PreferenceUtil.TERTIARY_SEARCH_STYLE;

        String keyEntry = PreferenceUtil.getInstance().getNextRandomAlbumSearchHistory(key);

        switch (keyEntry) {
            case "artist":
                searchType = ARTIST_SEARCH;
                break;
            case "genre":
                searchType = GENRE_SEARCH;
                break;
            case "random":
                searchType = RANDOM_SEARCH;
                break;
            default:
                hasNext = false;
                break;
        }

        return hasNext;
    }
}
