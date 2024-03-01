package com.poupa.vinylmusicplayer.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.poupa.vinylmusicplayer.discog.tagging.MultiValuesTagUtil;

import org.jetbrains.annotations.NonNls;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class Song implements Parcelable {
    public static final Song EMPTY_SONG = new Song(-1L, "", -1, -1, -1L, "", -1L, -1L, -1L, "", -1L, new ArrayList<>(List.of("")));

    public final long id;

    public List<String> albumArtistNames = new ArrayList<>(List.of(""));
    public String albumName;
    public long albumId;
    public List<String> artistNames = new ArrayList<>(List.of(""));
    public long artistId; // TODO This field is ambiguous - song's first artist or album first artist?
    @NonNls
    public final String data;
    public final long dateAdded;
    public final long dateModified;
    public int discNumber = 0;
    public final long duration;
    public List<String> genres = new ArrayList<>(List.of(""));
    public float replayGainAlbum = 0.0f;
    public float replayGainTrack = 0.0f;
    public float replayGainPeakAlbum = 1.0f;
    public float replayGainPeakTrack = 1.0f;
    public String title;
    public int trackNumber;
    public int year;

    public Song(long id, String title, int trackNumber, int year, long duration, String data, long dateAdded, long dateModified, long albumId, String albumName, long artistId, @NonNull List<String> artistNames) {
        this.id = id;
        this.albumName = albumName;
        this.albumId = albumId;
        this.artistNames = artistNames;
        this.artistId = artistId;
        this.data = data;
        this.dateAdded = dateAdded;
        this.dateModified = dateModified;
        this.duration = duration;
        this.title = title;
        this.trackNumber = trackNumber;
        this.year = year;
        // Note: Skip following fields since they are not supported by MediaStore:
        // discNumber, genre, albumArtistNames, replayGainTrack, replayGainAlbum
    }

    public Song(@NonNull final Song song) {
        id = song.id;
        albumArtistNames = song.albumArtistNames;
        albumName = song.albumName;
        albumId = song.albumId;
        artistNames = song.artistNames;
        artistId = song.artistId;
        data = song.data;
        dateAdded = song.dateAdded;
        dateModified = song.dateModified;
        discNumber = song.discNumber;
        duration = song.duration;
        genres = song.genres;
        replayGainAlbum = song.replayGainAlbum;
        replayGainTrack = song.replayGainTrack;
        replayGainPeakAlbum = song.replayGainPeakAlbum;
        replayGainPeakTrack = song.replayGainPeakTrack;
        title = song.title;
        trackNumber = song.trackNumber;
        year = song.year;
    }

    public boolean isQuickEqual(@NonNull final Song song) {
        return (id == song.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Song song = (Song) o;

        // Compare numerical fields first
        if (id != song.id) return false;
        if (albumId != song.albumId) return false;
        if (artistId != song.artistId) return false;
        if (dateAdded != song.dateAdded) return false;
        if (dateModified != song.dateModified) return false;
        if (discNumber != song.discNumber) return false;
        if (duration != song.duration) return false;
        if (trackNumber != song.trackNumber) return false;
        if (year != song.year) return false;

        // Note: Skip following fields since floating point comparison is not precise:
        // replayGainTrack, replayGainAlbum

        // Compare simple object fields
        if (!TextUtils.equals(albumName, song.albumName)) return false;
        if (!TextUtils.equals(data, song.data)) return false;
        if (!TextUtils.equals(title, song.title)) return false;

        // Compare structured object fields
        if (!Objects.equals(genres, song.genres)) return false;
        if (!Objects.equals(albumArtistNames, song.albumArtistNames)) return false;
        if (!Objects.equals(artistNames, song.artistNames)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int)id;
        result = 31 * result + albumArtistNames.hashCode();
        result = 31 * result + (albumName != null ? albumName.hashCode() : 0);
        result = 31 * result + (int)albumId;
        result = 31 * result + artistNames.hashCode();
        result = 31 * result + (int)artistId;
        result = 31 * result + (data != null ? data.hashCode() : 0);
        result = 31 * result + (int) (dateAdded ^ (dateAdded >>> 32));
        result = 31 * result + (int) (dateModified ^ (dateModified >>> 32));
        result = 31 * result + discNumber;
        result = 31 * result + (int) (duration ^ (duration >>> 32));
        result = 31 * result + genres.hashCode();
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + trackNumber;
        result = 31 * result + year;

        return result;
    }

    @NonNull
    @Override
    public String toString() {
        final String EOS = "'"; // end of string marker
        return "Song{" +
                "id=" + id +
                ", albumArtistName='" + MultiValuesTagUtil.infoString(albumArtistNames) + EOS +
                ", albumName='" + albumName + EOS +
                ", albumId=" + albumId +
                ", artistNames='" + MultiValuesTagUtil.infoString(artistNames) + EOS +
                ", artistId=" + artistId +
                ", data='" + data + EOS +
                ", dateAdded=" + dateAdded +
                ", dateModified=" + dateModified +
                ", discNumber=" + discNumber +
                ", duration=" + duration +
                ", genre='" + MultiValuesTagUtil.infoString(genres) + EOS +
                ", title='" + title + EOS +
                ", trackNumber=" + trackNumber +
                ", year=" + year +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeStringList(albumArtistNames);
        parcel.writeString(albumName);
        parcel.writeLong(albumId);
        parcel.writeStringList(artistNames);
        parcel.writeLong(artistId);
        parcel.writeString(data);
        parcel.writeLong(dateAdded);
        parcel.writeLong(dateModified);
        parcel.writeInt(discNumber);
        parcel.writeLong(duration);
        parcel.writeStringList(genres);
        parcel.writeString(title);
        parcel.writeInt(trackNumber);
        parcel.writeInt(year);
    }

    protected Song(Parcel in) {
        id = in.readLong();
        in.readStringList(albumArtistNames);
        albumName = in.readString();
        albumId = in.readLong();
        in.readStringList(artistNames);
        artistId = in.readLong();
        data = in.readString();
        dateAdded = in.readLong();
        dateModified = in.readLong();
        discNumber = in.readInt();
        duration = in.readLong();
        in.readStringList(genres);
        title = in.readString();
        trackNumber = in.readInt();
        year = in.readInt();
    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        public Song createFromParcel(Parcel source) {
            return new Song(source);
        }

        public Song[] newArray(int size) {
            return new Song[size];
        }
    };
}
