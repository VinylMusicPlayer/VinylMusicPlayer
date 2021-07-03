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

    private static final int RANDOM_ALBUM_SONG_ID = Song.EMPTY_SONG_ID-1; //-2;
    private static final int EMPTY_NEXT_RANDOM_ALBUM_ID = Song.EMPTY_SONG_ID-1; //-3;

    static private final NextRandomAlbum sInstance = new NextRandomAlbum();

    static public boolean IsRandomAlbum(long song_id) { return song_id == RANDOM_ALBUM_SONG_ID; }
    static public boolean IsEmptyNextRandomAlbum(long song_id) { return song_id == EMPTY_NEXT_RANDOM_ALBUM_ID; }

    private History listenHistory; // already listen album
    private History searchHistory; // manually searched album (with 3-dot menu) on this playlist (before going to next random album)
    private long nextRandomAlbumId;
    private boolean isSearchEnded;
    private Search searchFunction;


    // ----------------------------------------- PUBLIC METHOD  -----------------------------------------
    static public NextRandomAlbum getInstance() {
        return sInstance;
    }

    public NextRandomAlbum() {
        int historySize = 5; // Doesn't need to be too big, as history is there only so that we don't listen to the same set of album endlessly

        searchHistory = new History(historySize, false);
        listenHistory = new History(historySize, true);

        endSearch();
    }

    public Song search(Song song, Context context) {

        ArrayList<Album> albums;
        synchronized (Discography.getInstance()) {
            albums = new ArrayList<>(Discography.getInstance().getAllAlbums());
        }

        isSearchEnded = false;

        Album album = searchFunction.foundNextAlbum(song, albums, nextRandomAlbumId, listenHistory, searchHistory, context);

        if (album != null) {
            nextRandomAlbumId = album.getId();
            listenHistory.setNextRandomAlbumId(nextRandomAlbumId);
        }
        return addNewRandomAlbum(album, context);
    }

    public Song resetHistories(Context context) {
        searchHistory.clearHistory();
        endSearch();

        listenHistory.fetchHistory();

        nextRandomAlbumId = listenHistory.fetchNextRandomAlbumId();
        return addNewRandomAlbum(Discography.getInstance().getAlbum(nextRandomAlbumId), context);
    }

    public void commit(long albumId) {
        // add id to listen history, this should be the old album not the wanted one
        listenHistory.addIdToHistory(albumId, true);

        endSearch();
    }

    public boolean searchHasEnded() {
        return this.isSearchEnded;
    }

    // called when shuffling change
    public void stop() {
        // clear history search and listen
        searchHistory.stop();
        listenHistory.stop();

        endSearch();
    }

    public void clearSearchHistory() {
        searchHistory.clearHistory();

        endSearch();
    }

    public void initSearch(Search searchFunction) {
        this.searchFunction = searchFunction;
    }

    // ----------------------------------------- PRIVATE METHOD -----------------------------------------
    private void endSearch() {
        isSearchEnded = true;
        nextRandomAlbumId = -1;
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
}