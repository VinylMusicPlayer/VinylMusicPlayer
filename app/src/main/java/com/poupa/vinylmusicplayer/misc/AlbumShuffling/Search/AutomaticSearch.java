package com.poupa.vinylmusicplayer.misc.AlbumShuffling.Search;


import java.util.ArrayList;

import android.content.Context;

import com.poupa.vinylmusicplayer.misc.AlbumShuffling.History;
import com.poupa.vinylmusicplayer.model.Album;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;


/*
 * AutomaticSearch: Class that search the next random album to be played
 *                  For this, all albums that follow the search criteria set in preferences but not present in listenHistory are found and take one randomly
 *                  If no album are found, go to the next preferences criteria until something is found
 *                  If nothing is possible given the criteria, no album will be played at the end of the playlist
 */
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

    private void initSearchType() {
        fallbackLevel = 0;
        setNextSearchType();
    }

    private boolean setNextSearchType() {
        boolean hasNext = true;

        fallbackLevel++;

        String key;
        if (fallbackLevel == 1)
            key = PreferenceUtil.AS_FIRST_SEARCH_CRITERIA;
        else if (fallbackLevel == 2)
            key = PreferenceUtil.AS_SECOND_SEARCH_CRITERIA;
        else
            key = PreferenceUtil.AS_THIRD_SEARCH_CRITERIA;

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
