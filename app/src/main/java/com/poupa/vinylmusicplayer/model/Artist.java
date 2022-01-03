package com.poupa.vinylmusicplayer.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.poupa.vinylmusicplayer.util.MusicUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class Artist implements Parcelable {
    public static final String UNKNOWN_ARTIST_DISPLAY_NAME = "Unknown Artist";
    public static final Artist EMPTY = new Artist(-1, "");

    public final long id;
    public final String name;
    public final ArrayList<Album> albums;

    public Artist(long id, String name) {
        this.id = id;
        this.name = name;
        this.albums = new ArrayList<>();
    }

    public long getId() {
        return id;
    }

    public String getName() {
        if (MusicUtil.isArtistNameUnknown(name)) {
            return Artist.UNKNOWN_ARTIST_DISPLAY_NAME;
        }
        return name;
    }

    public int getSongCount() {
        int songCount = 0;
        for (Album album : albums) {
            songCount += album.getSongCount();
        }
        return songCount;
    }

    public int getAlbumCount() {
        return albums.size();
    }

    public ArrayList<Song> getSongs() {
        ArrayList<Song> songs = new ArrayList<>();
        for (Album album : albums) {
            songs.addAll(album.songs);
        }
        return songs;
    }

    public long getDateModified() {
        if (albums.isEmpty()) {return Song.EMPTY_SONG.dateModified;}

        return Collections.max(
                albums,
                Comparator.comparingLong(Album::getDateModified)
        ).getDateModified();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Artist artist = (Artist) o;

        if (id != artist.id) return false;
        if (!TextUtils.equals(name, artist.name)) return false;
        return Objects.equals(albums, artist.albums);
    }

    @Override
    public int hashCode() {
        int result = (int)id;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (albums != null ? albums.hashCode() : 0);

        return result;
    }

    @Override
    public String toString() {
        return "Artist{" +
                "albums=" + albums +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.name);
        dest.writeTypedList(this.albums);
    }

    protected Artist(Parcel in) {
        this.id = in.readLong();
        this.name = in.readString();
        this.albums = in.createTypedArrayList(Album.CREATOR);
    }

    public static final Parcelable.Creator<Artist> CREATOR = new Parcelable.Creator<Artist>() {
        @Override
        public Artist createFromParcel(Parcel source) {
            return new Artist(source);
        }

        @Override
        public Artist[] newArray(int size) {
            return new Artist[size];
        }
    };
}
