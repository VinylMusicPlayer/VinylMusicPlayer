package com.poupa.vinylmusicplayer.misc.AlbumShuffling.Search;


import java.util.ArrayList;

import android.content.Context;
import android.widget.Toast;

import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.misc.AlbumShuffling.History;
import com.poupa.vinylmusicplayer.model.Album;
import com.poupa.vinylmusicplayer.model.Song;


/*
 * ManualSearch: Abstract class used to agglomerate the random album manual shared search functions (access via 3-dot menu)
 *               For each search criteria (artist/genre/random) the concept is the same: get all albums not present in searchHistory that follow the criteria
 *               Randomly get one of them, if no album can be found remove the oldest album stocked into searchHistory and try again
 *               If after removing all album from searchHistory nothing is found tell it to the user
 */
abstract public class ManualSearch extends Search {
    @Override
    public boolean isManual() {
        return true;
    }

    @Override
    public Album foundNextAlbum(Song song, ArrayList<Album> albums, long previousNextRandomAlbumId, History listenHistory, History searchHistory, Context context)
    {
        constructPositionAlbum(song, albums, previousNextRandomAlbumId, searchHistory, listenHistory);

        Album album = null;

        // Get new random album
        int albumSize = albumArrayList.size();
        int albumPosition = getRandomAlbumPosition(albumSize, currentSongPosition, currentlyShownNextRandomAlbumPosition, forbiddenPosition);
        boolean albumPositionAsChange = (albumPosition >= 0);

        if (albumPosition == Search.ERROR_ARRAY_SIZE_IS_1) {
            Toast.makeText(context, context.getResources().getString(R.string.error_random_album_only_one_album), Toast.LENGTH_SHORT).show();
        } else if (albumPosition == Search.ERROR_HISTORY) { //clear-up history until something is found
            // Get first history element that follow current search criteria
            int firstElementPos;
            do {
                firstElementPos = getAlbumIdPosition(forbiddenId, searchHistory.popHistory()); // first element of history is now authorized
            } while (firstElementPos == -1);

            // Get position of popped searchHistory element
            forbiddenPosition.remove(firstElementPos);
            albumPosition = getRandomPositionFollowingForbiddenOne(albumSize, currentSongPosition, currentlyShownNextRandomAlbumPosition, forbiddenPosition);
            albumPositionAsChange = true;
        }

        // Get album if possible
        if (albumPositionAsChange) {
            if (albumPosition >= 0) {
                album = albumArrayList.get(albumPosition);
            } else if (currentlyShownNextRandomAlbumPosition != -1) {
                Toast.makeText(context, context.getResources().getString(R.string.error_random_album_no_new_found), Toast.LENGTH_SHORT).show();
            }
        }

        // If something new is found, put current nextRandomAlbum in search history
        if (album != null) {
            if (currentlyShownNextRandomAlbum != null) {
                searchHistory.addIdToHistory(currentlyShownNextRandomAlbum.getId(), false);
            }
            searchHistory.synchronizeHistory();
        } else {
            album = currentlyShownNextRandomAlbum; // if nothing is found, put back the old one
            searchHistory.revertHistory(false); //revert operation done on history as they didn't help
        }

        return album;
    }
}
