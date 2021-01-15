package com.poupa.vinylmusicplayer.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.poupa.vinylmusicplayer.loader.ReplayGainTagExtractor;
import com.poupa.vinylmusicplayer.util.MusicUtil;

import java.util.Arrays;
import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class Song implements Parcelable {
    public static final Song EMPTY_SONG = new Song(-1, "", -1, -1, -1, "", -1, -1, -1, "", -1, Arrays.asList(""));
    //public static final int TRACK_ARTIST_MAIN = 0;

    public final long id;

    public List<String> albumArtistNames = Arrays.asList("");
    public String albumName;
    public long albumId;
    public List<String> artistNames = Arrays.asList("");
    public long artistId;
    public final String data;
    public final long dateAdded;
    public final long dateModified;
    public int discNumber = 0;
    public final long duration;
    public String genre;
    private float replayGainAlbum = Float.NaN;
    private float replayGainTrack = Float.NaN;
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

    public Song(final @NonNull Song song) {
        this.id = song.id;
        this.albumArtistNames = song.albumArtistNames;
        this.albumName = song.albumName;
        this.albumId = song.albumId;
        this.artistNames = song.artistNames;
        this.artistId = song.artistId;
        this.data = song.data;
        this.dateAdded = song.dateAdded;
        this.dateModified = song.dateModified;
        this.discNumber = song.discNumber;
        this.duration = song.duration;
        this.genre = song.genre;
        this.replayGainAlbum = song.replayGainAlbum;
        this.replayGainTrack = song.replayGainTrack;
        this.title = song.title;
        this.trackNumber = song.trackNumber;
        this.year = song.year;
    }

    public void setReplayGainValues(float track, float album) {
        replayGainTrack = track;
        replayGainAlbum = album;
    }

    public float getReplayGainTrack() {
        // Since the extraction of RG tags incurs I/O, only extract the replay gain values if needed
        if (Float.isNaN(replayGainTrack)) {
            ReplayGainTagExtractor.setReplayGainValues(this);
        }
        return replayGainTrack;
    }

    public float getReplayGainAlbum() {
        // Since the extraction of RG tags incurs I/O, only extract the replay gain values if needed
        if (Float.isNaN(replayGainAlbum)) {
            ReplayGainTagExtractor.setReplayGainValues(this);
        }
        return replayGainAlbum;
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

        // Compare object fields
        if (albumArtistNames.size() != song.albumArtistNames.size()) return false;
        for (int i=0; i<albumArtistNames.size(); ++i) {
            if (!TextUtils.equals(albumArtistNames.get(i), song.albumArtistNames.get(i))) return false;
        }
        if (!TextUtils.equals(albumName, song.albumName)) return false;
        if (artistNames.size() != song.artistNames.size()) return false;
        for (int i=0; i<artistNames.size(); ++i) {
            if (!TextUtils.equals(artistNames.get(i), song.artistNames.get(i))) return false;
        }
        if (!TextUtils.equals(data, song.data)) return false;
        if (!TextUtils.equals(genre, song.genre)) return false;
        if (!TextUtils.equals(title, song.title)) return false;

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
        result = 31 * result + (genre != null ? genre.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + trackNumber;
        result = 31 * result + year;

        return result;
    }

    @Override
    public String toString() {
        return "Song{" +
                "id=" + id +
                ", albumArtistName='" + MusicUtil.artistNamesMerge(albumArtistNames) + '\'' +
                ", albumName='" + albumName + '\'' +
                ", albumId=" + albumId +
                ", artistNames='" + MusicUtil.artistNamesMerge(artistNames) + '\'' +
                ", artistId=" + artistId +
                ", data='" + data + '\'' +
                ", dateAdded=" + dateAdded +
                ", dateModified=" + dateModified +
                ", discNumber=" + discNumber +
                ", duration=" + duration +
                ", genre='" + genre + '\'' +
                ", title='" + title + '\'' +
                ", trackNumber=" + trackNumber +
                ", year=" + year +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeStringList(this.albumArtistNames);
        dest.writeString(this.albumName);
        dest.writeLong(this.albumId);
        dest.writeStringList(this.artistNames);
        dest.writeLong(this.artistId);
        dest.writeString(this.data);
        dest.writeLong(this.dateAdded);
        dest.writeLong(this.dateModified);
        dest.writeInt(this.discNumber);
        dest.writeLong(this.duration);
        dest.writeString(this.genre);
        dest.writeString(this.title);
        dest.writeInt(this.trackNumber);
        dest.writeInt(this.year);
    }

    protected Song(Parcel in) {
        this.id = in.readLong();
        in.readStringList(this.albumArtistNames);
        this.albumName = in.readString();
        this.albumId = in.readLong();
        in.readStringList(this.artistNames);
        this.artistId = in.readLong();
        this.data = in.readString();
        this.dateAdded = in.readLong();
        this.dateModified = in.readLong();
        this.discNumber = in.readInt();
        this.duration = in.readLong();
        this.genre = in.readString();
        this.title = in.readString();
        this.trackNumber = in.readInt();
        this.year = in.readInt();
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
