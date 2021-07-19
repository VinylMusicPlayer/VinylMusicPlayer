package com.poupa.vinylmusicplayer.misc.RandomAlbum;


import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import androidx.annotation.NonNull;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.discog.Discography;
import com.poupa.vinylmusicplayer.discog.tagging.MultiValuesTagUtil;
import com.poupa.vinylmusicplayer.misc.RandomAlbum.Search.Search;
import com.poupa.vinylmusicplayer.model.Album;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;


/*
 * NextRandomAlbum: Class that manage the next random album to be played
 *                  To do this, a false song is added at the end of the playing queue with info
 *                  pointing to the next alum and a invalid song id to find it easily
 *
 */
public class NextRandomAlbum {

    private static final int RANDOM_ALBUM_SONG_ID = Song.EMPTY_SONG_ID-1; // a valid next album song id
    private static final int EMPTY_NEXT_RANDOM_ALBUM_ID = Song.EMPTY_SONG_ID-1; // an invalid next album song id

    static private final NextRandomAlbum sInstance = new NextRandomAlbum();

    static public boolean IsRandomAlbum(long song_id) { return song_id == RANDOM_ALBUM_SONG_ID; }
    static public boolean IsEmptyNextRandomAlbum(long song_id) { return song_id == EMPTY_NEXT_RANDOM_ALBUM_ID; }

    private History listenHistory; // already listen album
    private History searchHistory; // manually searched album (with 3-dot menu) on this playlist (before going to next random album)

    private long nextRandomAlbumId; // save the next random album chosen by search
    private boolean isSearchEnded;  // has search been run but not ended (i.e. commit)

    private Search searchFunction; // type of search to be done: auto, manual by genre/artist/album

    private NextRandomAlbum() {
        int historySize = PreferenceUtil.getInstance().getNextRandomAlbumHistorySize(); // Doesn't need to be too big, as history is there only so that we don't listen to the same set of album endlessly

        searchHistory = new History(historySize, false);
        listenHistory = new History(historySize, true);

        endSearch();
    }

    // ----------------------------------------- PUBLIC METHOD  -----------------------------------------
    static public NextRandomAlbum getInstance() {
        return sInstance;
    }

    // Search for next album by using song as the currently played album
    @NonNull
    public Song search(Song song, Context context) {

        // Get all possible album to be play
        ArrayList<Album> albums;
        synchronized (Discography.getInstance()) {
            albums = new ArrayList<>(Discography.getInstance().getAllAlbums());
        }

        isSearchEnded = false;

        // Search
        Album album = searchFunction.foundNextAlbum(song, albums, nextRandomAlbumId, listenHistory, searchHistory, context);

        return addNewRandomAlbum(album, context);
    }

    public Song resetHistories(Context context) {
        searchHistory.clearHistory();
        endSearch();

        listenHistory.fetchHistory();

        nextRandomAlbumId = listenHistory.fetchNextRandomAlbumId();
        return addNewRandomAlbum(Discography.getInstance().getAlbum(nextRandomAlbumId), context);
    }

    public void setHistoriesSize(int size) {
        searchHistory.setHistorySize(size);
        listenHistory.setHistorySize(size);
    }

    // next random album is been loaded into queue, thus old one as been listen too
    public void commit(long albumId) {
        // add id to listen history, this should be the old album not the wanted one
        listenHistory.addIdToHistory(albumId, true);

        endSearch();
    }

    public boolean searchHasEnded() {
        return this.isSearchEnded;
    }

    // called when random album shuffling mode end
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
        initSearch(searchFunction, false);
    }

    public void initSearch(Search searchFunction, boolean synchronizeHistories) {
        this.searchFunction = searchFunction;

        if (synchronizeHistories) {
            searchHistory.setHistory(listenHistory);
        }
    }

    // ----------------------------------------- PRIVATE METHOD -----------------------------------------
    private void endSearch() {
        isSearchEnded = true;
        nextRandomAlbumId = -1;
    }

    @NonNull
    private Song addNewRandomAlbum(long albumId, String albumName, long artistId, @NonNull List<String> artistNames, Context context) {
        return new Song(RANDOM_ALBUM_SONG_ID, context.getResources().getString(R.string.next_album), 0, -1, -1, "",
                -1, -1, albumId, albumName, artistId, artistNames);
    }

    @NonNull
    private Song addEmptyRandomAlbum(Context context) {
        return addNewRandomAlbum(EMPTY_NEXT_RANDOM_ALBUM_ID, "None", -1, MultiValuesTagUtil.split(context.getResources().getString(R.string.no_album_found)), context);
    }

    @NonNull
    private Song addNewRandomAlbum(Album album, Context context) {
        if (album != null) {
            nextRandomAlbumId = album.getId();
            listenHistory.setNextRandomAlbumId(nextRandomAlbumId);

            return addNewRandomAlbum(album.getId(), album.getTitle(), album.getArtistId(), MultiValuesTagUtil.split(album.getArtistName()), context);
        } else {
            nextRandomAlbumId = -1;
            listenHistory.setNextRandomAlbumId(nextRandomAlbumId);

            return addEmptyRandomAlbum(context);
        }
    }
}