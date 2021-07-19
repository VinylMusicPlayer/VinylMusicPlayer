package com.poupa.vinylmusicplayer.misc.AlbumShuffling.Search;


import com.poupa.vinylmusicplayer.model.Album;
import com.poupa.vinylmusicplayer.model.Song;


public class ManualAlbumSearch extends ManualSearch {
    @Override
    public boolean searchTypeIsTrue(Song song, Album album) {
        return albumSearch();
    }
}
