package com.poupa.vinylmusicplayer.misc.RandomAlbum;


import java.util.ArrayList;

import android.content.Context;
import android.widget.Toast;

import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.model.Album;
import com.poupa.vinylmusicplayer.model.Song;


abstract public class ManualSearch extends Search {
    @Override
    public boolean isManual() {
        return true;
    }

    @Override
    public Album foundNextAlbum(Song song, ArrayList<Album> albums, long nextRandomAlbumId, History listenHistory, History searchHistory, Context context)
    {
        constructPositionAlbum(song, albums, nextRandomAlbumId, searchHistory, listenHistory);

        Album album = nextRandomAlbum;

        boolean albumPositionAsChange = true;
        int albumSize = albumArrayList.size();
        int albumPosition = getRandomAlbumPosition(albumSize, lastSongPosition, nextRandomAlbumPosition, forbiddenPosition);

        if (albumPosition == Search.ERROR_ARRAY_SIZE_IS_1) {
            Toast.makeText(context, context.getResources().getString(R.string.error_random_album_only_one_album), Toast.LENGTH_SHORT).show();
            albumPositionAsChange = false;
        } else if (albumPosition == Search.ERROR_HISTORY) {
            ArrayList<Integer> newForbiddenPosition = new ArrayList<>();
            newForbiddenPosition.add(lastSongPosition);
            if (nextRandomAlbumPosition != -1) {
                newForbiddenPosition.add(nextRandomAlbumPosition);
            }

            int firstElementPos = -1;
            while (firstElementPos == -1) {
                firstElementPos = getAlbumIdPosition(forbiddenId, searchHistory.popHistory()); // first element is now authorized
            }
            forbiddenPosition.remove(firstElementPos);
            newForbiddenPosition.addAll(forbiddenPosition);

            albumPosition = randomIntWithinForbiddenNumber(albumSize, newForbiddenPosition);
        }

        if (albumPositionAsChange) {
            if (albumPosition >= 0) {
                album = albumArrayList.get(albumPosition);
                if (album.getId() == nextRandomAlbum.getId() || History.isIdForbidden(album.getId(), searchHistory.getHistory())) {
                    Toast.makeText(context, context.getResources().getString(R.string.error_random_album_only_two_album), Toast.LENGTH_SHORT).show();
                }
            } else if (nextRandomAlbumPosition != -1) {
                album = albumArrayList.get(nextRandomAlbumPosition);
                Toast.makeText(context, context.getResources().getString(R.string.error_random_album_no_new_found), Toast.LENGTH_SHORT).show();
            }
        }

        if (album != null) {
            searchHistory.addIdToHistory(nextRandomAlbum.getId(), false);
            searchHistory.synchronizeHistory();
        } else {
            searchHistory.revertHistory();
        }

        return album;
    }
}
