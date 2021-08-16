package com.poupa.vinylmusicplayer.misc.queue.AlbumShuffling;


import java.util.ArrayList;
import java.util.Random;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.discog.Discography;
import com.poupa.vinylmusicplayer.misc.queue.DynamicElement;
import com.poupa.vinylmusicplayer.misc.queue.DynamicQueueLoader;
import com.poupa.vinylmusicplayer.model.Album;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.util.MusicUtil;


public class AlbumShufflingQueueLoader implements DynamicQueueLoader {
    public static final String SEARCH_TYPE = "search_type";

    public static final int RANDOM_SEARCH = 1;
    public static final int ARTIST_SEARCH = 2;
    public static final int GENRE_SEARCH = 3;

    private final DB database;
    private Album nextAlbum;
    private Song songUsedForSearching;

    public AlbumShufflingQueueLoader() {
        this.nextAlbum = new Album();
        this.database = new DB();

        this.songUsedForSearching = Song.EMPTY_SONG;
    }

    public boolean restoreQueue(Context context, Song song) {
        this.nextAlbum = Discography.getInstance().getAlbum(database.fetchNextRandomAlbumId());
        this.songUsedForSearching = song;

        return true;
    }

    public void setNextDynamicQueue(Context context, Song song, boolean force) {
        Bundle bundle = new Bundle();
        bundle.putInt(AlbumShufflingQueueLoader.SEARCH_TYPE, AlbumShufflingQueueLoader.RANDOM_SEARCH);
        setNextDynamicQueue(bundle, context, song, force);
    }

    public void setNextDynamicQueue(Bundle criteria, Context context, Song song, boolean force) {
        if (song != null && (force || (song.id != songUsedForSearching.id))) {
            this.songUsedForSearching = song;

            int searchType = criteria.getInt(SEARCH_TYPE);

            ArrayList<Album> albums;
            synchronized (Discography.getInstance()) {
                albums = new ArrayList<>(Discography.getInstance().getAllAlbums());
            }

            ArrayList<Album> subList = new ArrayList<>();
            boolean isAlbumInCriteria = false;
            for (Album album : albums) {
                if (song.albumId != album.getId()) {
                    switch (searchType) {
                        case RANDOM_SEARCH:
                            isAlbumInCriteria = true;
                            break;
                        case ARTIST_SEARCH:
                            isAlbumInCriteria = album.getArtistId() == song.artistId;
                            break;
                        case GENRE_SEARCH:
                            isAlbumInCriteria = album.songs != null && album.songs.size() > 0 &&
                                    song.genre.equals(album.songs.get(0).genre);
                            break;
                    }

                    if (isAlbumInCriteria) {
                        subList.add(album);
                    }
                }
            }

            if (subList.size() > 0) {
                Random rand = new Random();
                this.nextAlbum = subList.get(rand.nextInt(subList.size()));
                this.database.setNextRandomAlbumId(nextAlbum.getId());
            } else {
                if (context != null) {
                    Toast.makeText(context, context.getResources().getString(R.string.no_other_album_found), Toast.LENGTH_SHORT).show();
                } else {
                    this.nextAlbum = null;
                    this.database.setNextRandomAlbumId(-1);
                }
            }
        }
    }

    public static ArrayList<Song> getNextRandomQueue() {
        ArrayList<Album> albums;
        synchronized (Discography.getInstance()) {
            albums = new ArrayList<>(Discography.getInstance().getAllAlbums());
        }
        Random rand = new Random();
        Album album = albums.get(rand.nextInt(albums.size()));

        return album.songs;
    }

    @NonNull
    public DynamicElement getDynamicElement(Context context) {
        if (nextAlbum != null)
            return new DynamicElement(context.getResources().getString(R.string.next_album),
                    MusicUtil.buildInfoString(this.nextAlbum.getArtistName(), this.nextAlbum.getTitle()),
                    "-");
        return getEmptyDynamicElement(context);
    }

    public boolean isNextQueueEmpty() {
        return  (nextAlbum == null);
    }

    private DynamicElement getEmptyDynamicElement(Context context) {
        return new DynamicElement(context.getResources().getString(R.string.next_album), context.getResources().getString(R.string.no_album_found), "-");
    }

    public ArrayList<Song> getNextQueue() {
        return nextAlbum.songs;
    }
}
