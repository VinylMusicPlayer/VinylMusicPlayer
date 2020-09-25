package com.poupa.vinylmusicplayer.loader;

import android.content.Context;
import android.provider.MediaStore.Audio.AudioColumns;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.poupa.vinylmusicplayer.model.Album;
import com.poupa.vinylmusicplayer.model.Artist;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;

import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ArtistLoader {
    public static String getSongLoaderSortOrder(Context context) {
        return PreferenceUtil.getInstance().getArtistSortOrder() + ", " + PreferenceUtil.getInstance().getArtistAlbumSortOrder() + ", " + PreferenceUtil.getInstance().getAlbumSongSortOrder();
    }

    @NonNull
    public static ArrayList<Artist> getAllArtists(@NonNull final Context context) {
        ArrayList<Song> songs = SongLoader.getSongs(SongLoader.makeSongCursor(
                context,
                null,
                null,
                getSongLoaderSortOrder(context))
        );
        return splitIntoArtists(songs);
    }

    @NonNull
    public static ArrayList<Artist> getArtists(@NonNull final Context context, String query) {
        ArrayList<Song> songs = SongLoader.getSongs(SongLoader.makeSongCursor(
                context,
                AudioColumns.ARTIST + " LIKE ?",
                new String[]{"%" + query + "%"},
                getSongLoaderSortOrder(context))
        );
        return splitIntoArtists(songs);
    }

    @NonNull
    public static Artist getArtist(@NonNull final Context context, long artistId) {
        ArrayList<Song> songs = SongLoader.getSongs(SongLoader.makeSongCursor(
                context,
                AudioColumns.ARTIST_ID + "=?",
                new String[]{String.valueOf(artistId)},
                getSongLoaderSortOrder(context))
        );
        return new Artist(AlbumLoader.splitIntoAlbums(songs));
    }

    @NonNull
    public static ArrayList<Artist> splitIntoArtists(@Nullable final ArrayList<Song> songs) {
        ArrayList<Artist> artists = new ArrayList<>();
        if (songs != null) {
            for (Song song : songs) {
                Artist artist = getOrCreateArtist(artists, song.artistId);
                Album album = AlbumLoader.getOrCreateAlbum(artist.albums, song.albumId);
                album.songs.add(song);
            }
        }
        return artists;
    }

    private static Artist getOrCreateArtist(ArrayList<Artist> artists, long artistId) {
        for (Artist artist : artists) {
            if (!artist.albums.isEmpty() && !artist.albums.get(0).songs.isEmpty() && artist.albums.get(0).songs.get(0).artistId == artistId) {
                return artist;
            }
        }
        Artist artist = new Artist();
        artists.add(artist);
        return artist;
    }
}
