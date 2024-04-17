package com.poupa.vinylmusicplayer.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.poupa.vinylmusicplayer.util.MusicUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class Album implements Parcelable {
    public static String UNKNOWN_ALBUM_DISPLAY_NAME = "Unknown Album";

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

    @NonNull
    public String getTitle() {
        return getTitle(safeGetFirstSong().albumName);
    }

    @NonNull
    public static String getTitle(@NonNull final String albumName) {
        if (MusicUtil.isAlbumNameUnknown(albumName)) {
            return UNKNOWN_ALBUM_DISPLAY_NAME;
        }
        return albumName;
    }

    @NonNull
    public List<String> getArtistNames() {
        final Song song = safeGetFirstSong();

        if (song.albumArtistNames.isEmpty()) {
            return Collections.unmodifiableList(song.artistNames);
        } else {
            return Collections.unmodifiableList(song.albumArtistNames);
        }
    }

    public int getYear() {
        return safeGetFirstSong().year;
    }

    public long getDateAdded() {
        if (songs.isEmpty()) {return Song.EMPTY_SONG.dateAdded;}

        return Collections.min(
                songs,
                Comparator.comparingLong(song -> song.dateAdded)
        ).dateAdded;
    }

    public long getDateModified() {
        if (songs.isEmpty()) {return Song.EMPTY_SONG.dateModified;}

        return Collections.max(
                songs,
                Comparator.comparingLong(song -> song.dateModified)
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

    @NonNull
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

    public static final Creator<Album> CREATOR = new Creator<>() {
        public Album createFromParcel(Parcel source) {
            return new Album(source);
        }

        public Album[] newArray(int size) {
            return new Album[size];
        }
    };
}
