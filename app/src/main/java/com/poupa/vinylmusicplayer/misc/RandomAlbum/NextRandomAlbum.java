package com.poupa.vinylmusicplayer.misc.RandomAlbum;


import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import androidx.annotation.NonNull;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.discog.Discography;
import com.poupa.vinylmusicplayer.discog.tagging.MultiValuesTagUtil;
import com.poupa.vinylmusicplayer.model.Album;
import com.poupa.vinylmusicplayer.model.Song;


public class NextRandomAlbum {

    public static final int RANDOM_ALBUM_SONG_ID = -2;
    public static final int EMPTY_NEXT_RANDOM_ALBUM_ID = -3;

    static private NextRandomAlbum sInstance = new NextRandomAlbum();

    private History listenHistory; // already listen album
    private History searchHistory; // manually searched album (with 3-dot menu)
    private long nextRandomAlbumId;
    private long lastAlbumIdSearched;
    private Search searchFunction;


    static public NextRandomAlbum getInstance() {
        return sInstance;
    }

    private NextRandomAlbum() {
        int historySize = 5; // Doesn't need to be too big, as history is there only so that we don't listen to the same set of album endlessly

        searchHistory = new History(historySize);
        listenHistory = new History(historySize);

        lastAlbumIdSearched = -1;
        nextRandomAlbumId = -1;
    }

    public void initSearch(Search searchFunction) {
        this.searchFunction = searchFunction;
    }

    private Song addNewRandomAlbum(long albumId, String albumName, long artistId, @NonNull List<String> artistNames, Context context) {
        return new Song(RANDOM_ALBUM_SONG_ID, context.getResources().getString(R.string.next_album), 0, -1, -1, "",
                -1, -1, albumId, albumName, artistId, artistNames);
    }

    private Song addEmptyRandomAlbum(Context context) {
        return addNewRandomAlbum(EMPTY_NEXT_RANDOM_ALBUM_ID, "None", -1, MultiValuesTagUtil.split(context.getResources().getString(R.string.no_album_found)), context);
    }

    private Song addNewRandomAlbum(Album album, Context context) {
        if (album != null) {
            return addNewRandomAlbum(album.getId(), album.getTitle(), album.getArtistId(), MultiValuesTagUtil.split(album.getArtistName()), context);
        } else {
            return addEmptyRandomAlbum(context);
        }
    }

    public Song search(Song song, Context context) {

        ArrayList<Album> albums;
        synchronized (Discography.getInstance()) {
            albums = new ArrayList<>(Discography.getInstance().getAllAlbums());
        }

        lastAlbumIdSearched = song.albumId;

        Album album = searchFunction.foundNextAlbum(song, albums, nextRandomAlbumId, listenHistory, searchHistory, context);

        if (album != null) {
            nextRandomAlbumId = album.getId();
        }
        return addNewRandomAlbum(album, context);
    }

    public void resetHistories(long albumId) {
        searchHistory.clearHistory();
        listenHistory.clearHistory();
        nextRandomAlbumId = albumId;
    }

    public void commit(long albumId) {
        // add id to listen history, this should be the old album not the wanted one
        listenHistory.addIdToHistory(albumId);
        nextRandomAlbumId = -1; // until new search is done
        lastAlbumIdSearched = -1;
    }

    public Long getLastAlbumIdSearched() {
        return lastAlbumIdSearched;
    }

    // called when shuffling change
    public void stop() {
        // clear history search and listen
        searchHistory.stop();
        listenHistory.stop();
        nextRandomAlbumId = -1;
        lastAlbumIdSearched = -1;
    }

    public void clearSearchHistory() {
        searchHistory.clearHistory();
        lastAlbumIdSearched = -1;
    }
}