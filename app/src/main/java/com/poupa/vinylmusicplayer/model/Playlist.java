package com.poupa.vinylmusicplayer.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.poupa.vinylmusicplayer.loader.PlaylistSongLoader;
import com.poupa.vinylmusicplayer.util.MusicUtil;

import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class Playlist implements Parcelable {
    public final long id;
    public final String name;

    public Playlist(final long id, final String name) {
        this.id = id;
        this.name = name;
    }

    public Playlist() {
        this.id = -1;
        this.name = "";
    }

    @NonNull
    public String getInfoString(@NonNull Context context) {
        int songCount = getSongs(context).size();
        return MusicUtil.getSongCountString(context, songCount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Playlist playlist = (Playlist) o;

        if (id != playlist.id) return false;
        return name != null ? name.equals(playlist.name) : playlist.name == null;

    }

    @Override
    public int hashCode() {
        int result = (int)id;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Playlist{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    @NonNull
    public ArrayList<Song> getSongs(Context context) {
        // this default implementation covers static playlists
        return PlaylistSongLoader.getPlaylistSongList(context, id);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.name);
    }

    protected Playlist(Parcel in) {
        this.id = in.readLong();
        this.name = in.readString();
    }

    public static final Creator<Playlist> CREATOR = new Creator<Playlist>() {
        public Playlist createFromParcel(Parcel source) {
            return new Playlist(source);
        }

        public Playlist[] newArray(int size) {
            return new Playlist[size];
        }
    };
}
