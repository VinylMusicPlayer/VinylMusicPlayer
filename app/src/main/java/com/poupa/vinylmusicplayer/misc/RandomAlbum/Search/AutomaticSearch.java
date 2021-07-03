package com.poupa.vinylmusicplayer.misc.RandomAlbum.Search;


import java.util.ArrayList;

import android.content.Context;

import com.poupa.vinylmusicplayer.misc.RandomAlbum.History;
import com.poupa.vinylmusicplayer.model.Album;
import com.poupa.vinylmusicplayer.model.Song;


public class AutomaticSearch extends Search {
    @Override
    public boolean isManual() {
        return false;
    }

    public boolean searchTypeIsTrue(Song song, Album album) {
        return artistSearch(song, album);
        //return albumSearch();
    }

    @Override
    public Album foundNextAlbum(Song song, ArrayList<Album> albums, long previousNextRandomAlbumId, History listenHistory, History searchHistory, Context context) {
        Album album = null;

        constructPositionAlbum(song, albums, previousNextRandomAlbumId, searchHistory, listenHistory);

        int albumPosition = getRandomAlbumPosition(albumArrayList.size(), currentSongPosition, currentlyShownNextRandomAlbumPosition, forbiddenPosition);

        if (albumPosition >= 0) {
            album = albumArrayList.get(albumPosition);
        } /*else { // will be use when fallback is Implemented
                listenHistory.revertHistory();
        }*/

        return album;
    }
}
