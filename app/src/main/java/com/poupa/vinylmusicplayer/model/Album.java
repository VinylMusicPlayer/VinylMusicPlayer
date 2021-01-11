package com.poupa.vinylmusicplayer.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.poupa.vinylmusicplayer.util.MusicUtil;

import java.util.ArrayList;

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
        // TODO Return the albumArtist instead
        return safeGetFirstSong().artistId;
    }

    public String getArtistName() {
        final Song song = safeGetFirstSong();
        String name = song.albumArtistName;
        if (TextUtils.isEmpty(name)) {
            name = MusicUtil.artistNamesMerge(song);
        }
        if (MusicUtil.isArtistNameUnknown(name)) {
            return Artist.UNKNOWN_ARTIST_DISPLAY_NAME;
        }
        return name;
    }

    public int getYear() {
        return safeGetFirstSong().year;
    }

    public long getDateAdded() {
        return safeGetFirstSong().dateAdded;
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

        return songs != null ? songs.equals(that.songs) : that.songs == null;

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
