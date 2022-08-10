package com.poupa.vinylmusicplayer.misc.queue.DynamicElement.AlbumShuffling;


import java.util.ArrayList;
import java.util.Random;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.discog.Discography;
import com.poupa.vinylmusicplayer.misc.queue.DynamicElement.AbstractShuffling.AbstractQueueLoader;
import com.poupa.vinylmusicplayer.misc.queue.DynamicElement.DynamicElement;
import com.poupa.vinylmusicplayer.misc.queue.DynamicElement.DynamicQueueItemAdapter;
import com.poupa.vinylmusicplayer.misc.queue.DynamicElement.DynamicQueueLoader;
import com.poupa.vinylmusicplayer.model.Album;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.util.MusicUtil;


/** Album shuffling implementation of {@link DynamicQueueLoader} */
public class AlbumShufflingQueueLoader extends AbstractQueueLoader {
    public static final String SEARCH_TYPE = "search_type";

    public static final int RANDOM_SEARCH = 1;
    public static final int ARTIST_SEARCH = 2;
    public static final int GENRE_SEARCH = 3;

    private final DB database;
    private Album nextAlbum;

    public AlbumShufflingQueueLoader() {
        super();

        this.nextAlbum = new Album();
        this.database = new DB();
    }

    @Override
    public boolean restoreQueue(Context context, Song song) {
        this.nextAlbum = Discography.getInstance().getAlbum(database.fetchNextRandomAlbumId());

        return super.restoreQueue(song);
    }

    public DynamicQueueItemAdapter getAdapter() {
        return new AlbumShufflingQueueItemAdapter();
    }

    // For album shuffling V2: use bottom sheet criteria for automatic search instead of the actual random manual search
    @Override
    public boolean setNextDynamicQueue(Context context, Song song, boolean force) {
        Bundle bundle = new Bundle();
        bundle.putInt(AlbumShufflingQueueLoader.SEARCH_TYPE, AlbumShufflingQueueLoader.RANDOM_SEARCH);

        return setNextDynamicQueue(bundle, context, song, force);
    }

    @Override
    public boolean setNextDynamicQueue(Bundle criteria, Context context, Song song, boolean force) {
        if (!super.setNextDynamicQueue(criteria, context, song, force))
            return false;

        //Random search basic form, will be updated for v2
        int searchType = criteria.getInt(SEARCH_TYPE);

        ArrayList<Album> albums;
        synchronized (Discography.getInstance()) {
            albums = new ArrayList<>(Discography.getInstance().getAllAlbums());
        }

        ArrayList<Album> subList = new ArrayList<>();
        boolean isAlbumInCriteria = false;
        for (Album album : albums) {
            if (song.albumId != album.getId() && (nextAlbum == null || nextAlbum.getId() != album.getId())) {
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

        return true;
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

    public boolean isNextQueueEmpty() {
        return  (nextAlbum == null);
    }

    public ArrayList<Song> getNextQueue() {
        if (isNextQueueEmpty())
            return null;

        return nextAlbum.songs;
    }

    @Override
    protected DynamicElement createEmptyDynamicElement(Context context) {
        return new DynamicElement(context.getResources().getString(R.string.next_album),
                context.getResources().getString(R.string.no_album_found),
                R.drawable.ic_shuffle_album_white_24dp); //"-");
    }

    @Override
    protected DynamicElement createNewDynamicElement(Context context) {
        return new DynamicElement(context.getResources().getString(R.string.next_album),
                MusicUtil.buildInfoString(this.nextAlbum.getArtistName(), this.nextAlbum.getTitle()),
                R.drawable.ic_shuffle_album_white_24dp); //"-");
    }

    @Override
    protected boolean isSongDifferentEnough(@NonNull Song song) {
        return (song.albumId != songUsedForSearching.albumId);
    }
}
