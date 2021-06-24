package com.poupa.vinylmusicplayer.misc.RandomAlbum;


import com.poupa.vinylmusicplayer.model.Album;
import com.poupa.vinylmusicplayer.model.Song;


public class ManualGenreSearch extends ManualSearch {
    @Override
    public boolean searchTypeIsTrue(Song song, Album album) {
        return genreSearch(song, album);
    }
}
