package com.poupa.vinylmusicplayer.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.poupa.vinylmusicplayer.discog.Discography;
import com.poupa.vinylmusicplayer.util.MusicUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class Album implements Parcelable {
    public static final String UNKNOWN_ALBUM_DISPLAY_NAME = "Unknown Album";

    public final ArrayList<Song> songs;

    public Album(ArrayList<Song> songs) {
        this.songs = songs;
    }

    public Album() {
        this.songs = new ArrayList<>();
    }

    public long getId() {
        return safeGetFirstSong().albumId;
    }

    public String getTitle() {
        String name = safeGetFirstSong().albumName;
        if (MusicUtil.isAlbumNameUnknown(name)) {
            return UNKNOWN_ALBUM_DISPLAY_NAME;
        }
        return name;
    }

    public long getArtistId() {
        return getArtist().id;
    }

    public String getArtistName() {
        return getArtist().name;
    }

    @NonNull
    private Artist getArtist() {
        final Song song = safeGetFirstSong();

        // Try getting the album artist
        final String name = song.albumArtistNames.get(0);
        if (!MusicUtil.isArtistNameUnknown(name)) {
            final Artist artist = Discography.getInstance().getArtistByName(name);
            if (artist != null) return artist;
        }

        // Fallback: use the first song's first artist
        final Artist artist = Discography.getInstance().getArtist(song.artistId);
        if (artist != null) return artist;

        // Give up
        return Artist.EMPTY;
    }

    public int getYear() {
        return safeGetFirstSong().year;
    }

    public long getDateAdded() {
        if (songs.isEmpty()) {return Song.EMPTY_SONG.dateModified;}

        return Collections.min(
                songs,
                Comparator.comparingLong(s -> s.dateAdded)
        ).dateAdded;
    }

    public long getDateModified() {
        if (songs.isEmpty()) {return Song.EMPTY_SONG.dateModified;}

        return Collections.max(
                songs,
                Comparator.comparingLong(s -> s.dateModified)
        ).dateModified;
    }

    public int getSongCount() {
        return songs.size();
    }

    @NonNull
    public Song safeGetFirstSong() {
        return songs.isEmpty() ? Song.EMPTY_SONG : songs.get(0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Album that = (Album) o;

        return Objects.equals(songs, that.songs);

    }

    @Override
    public int hashCode() {
        return songs != null ? songs.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Album{" +
                "songs=" + songs +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(songs);
    }

    protected Album(Parcel in) {
        this.songs = in.createTypedArrayList(Song.CREATOR);
    }

    public static final Creator<Album> CREATOR = new Creator<Album>() {
        public Album createFromParcel(Parcel source) {
            return new Album(source);
        }

        public Album[] newArray(int size) {
            return new Album[size];
        }
    };
}
