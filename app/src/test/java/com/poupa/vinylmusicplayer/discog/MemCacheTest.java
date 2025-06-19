package com.poupa.vinylmusicplayer.discog;

import com.poupa.vinylmusicplayer.model.Song;

import org.junit.Test;

import static org.junit.Assert.*;
import java.util.List;
import java.util.Set;

public class MemCacheTest {

    @Test
    public void addingSongWithNoArtist_albumStillShowsUp(){
        MemCache memCache = new MemCache();

        Song song1 = new Song(1, "NoArtists",   0, 0, 0, "---", 0, 0, 101, "Nice Songs 1", List.of());
        Song song2 = new Song(1, "HasArtist",   0, 0, 0, "---", 0, 0, 102, "Nice Songs 2", List.of("A"));
        Song song3 = new Song(1, "ManyArtists", 0, 0, 0, "---", 0, 0, 103, "Nice Songs 3", List.of("B","C"));

        memCache.addSong(song1);
        memCache.addSong(song2);
        memCache.addSong(song3);

        assertEquals(3, memCache.albumsByName.size());
        assertEquals(Set.of(101L),memCache.albumsByName.get("Nice Songs 1"));
        assertEquals(Set.of(102L),memCache.albumsByName.get("Nice Songs 2"));
        assertEquals(Set.of(103L),memCache.albumsByName.get("Nice Songs 3"));
        assertNull(memCache.albumsByName.get("D"));


        assertEquals(3, memCache.albumsByAlbumIdAndArtistId.size());
        assertNotNull(memCache.albumsByAlbumIdAndArtistId.get( 101L ));
        assertNotNull(memCache.albumsByAlbumIdAndArtistId.get( 102L ));
        assertNotNull(memCache.albumsByAlbumIdAndArtistId.get( 103L ));
        assertNull(memCache.albumsByName.get("D"));
    }
}
