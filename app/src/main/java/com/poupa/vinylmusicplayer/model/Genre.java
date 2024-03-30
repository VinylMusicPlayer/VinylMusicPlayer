package com.poupa.vinylmusicplayer.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.poupa.vinylmusicplayer.util.MusicUtil;

public class Genre implements Parcelable {
    public static String UNKNOWN_GENRE_DISPLAY_NAME = "Unknown Genre";

    public final long id;
    public final String name;

    public int songCount;

    public Genre(final long id, final String name, final int songCount) {
        this.id = id;
        this.name = name;
        this.songCount = songCount;
    }

    public String getName() {
        if (MusicUtil.isGenreNameUnknown(name)) {
            return UNKNOWN_GENRE_DISPLAY_NAME;
        }
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Genre genre = (Genre) o;

        if (id != genre.id) return false;

        if (!name.equals(genre.name)) return false;
        return songCount == genre.songCount;
    }

    @Override
    public int hashCode() {
        long result = id;
        result = 31 * result + name.hashCode();
        result = 31 * result + songCount;
        return (int)result;
    }

    @NonNull
    @Override
    public String toString() {
        return "Genre{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", songCount=" + songCount + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong((int)this.id);
        dest.writeString(this.name);
        dest.writeInt(this.songCount);
    }

    protected Genre(Parcel in) {
        this.id = in.readLong();
        this.name = in.readString();
        this.songCount = in.readInt();
    }

    public static final Creator<Genre> CREATOR = new Creator<Genre>() {
        public Genre createFromParcel(Parcel source) {
            return new Genre(source);
        }

        public Genre[] newArray(int size) {
            return new Genre[size];
        }
    };
}
