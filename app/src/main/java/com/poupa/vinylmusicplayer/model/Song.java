package com.poupa.vinylmusicplayer.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.poupa.vinylmusicplayer.loader.ReplayGainTagExtractor;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class Song implements Parcelable {
    public static final Song EMPTY_SONG = new Song(-1, "", -1, -1, -1, "", -1, -1, -1, "", -1, "");

    public final long id;
    public String title;
    public int trackNumber;
    public int discNumber = 0;
    public int year;
    public final long duration;
    public final String data;
    public final long dateAdded;
    public final long dateModified;
    public long albumId;
    public String albumName;
    public long artistId;
    public String artistName;
    public String albumArtistName;
    public String genre;

    private float replayGainTrack = Float.NaN;
    private float replayGainAlbum = Float.NaN;

    public Song(long id, String title, int trackNumber, int year, long duration, String data, long dateAdded, long dateModified, long albumId, String albumName, long artistId, String artistName) {
        this.id = id;
        this.title = title;
        this.trackNumber = trackNumber;
        this.year = year;
        this.duration = duration;
        this.data = data;
        this.dateAdded = dateAdded;
        this.dateModified = dateModified;
        this.albumId = albumId;
        this.albumName = albumName;
        this.artistId = artistId;
        this.artistName = artistName;
        // Note: Ignore since it's not supported by MediaStore: discNumber, genre
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

        if (id != song.id) return false;
        if (trackNumber != song.trackNumber) return false;
        // Note: Ignore since it's not supported by MediaStore: if (discNumber != song.discNumber) return false;
        if (year != song.year) return false;
        if (duration != song.duration) return false;
        if (dateAdded != song.dateAdded) return false;
        if (dateModified != song.dateModified) return false;
        if (albumId != song.albumId) return false;
        if (artistId != song.artistId) return false;
        if (!TextUtils.equals(title, song.title)) return false;
        if (!TextUtils.equals(data, song.data)) return false;
        if (!TextUtils.equals(albumName, song.albumName)) return false;
        if (!TextUtils.equals(artistName, song.artistName)) return false;
        // Note: Ignore since it's not supported by MediaStore: if (!TextUtils.equals(genre, song.genre)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int)id;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + trackNumber;
        // Note: Ignore since it's not supported by MediaStore: result = 31 * result + discNumber;
        result = 31 * result + year;
        result = 31 * result + (int) (duration ^ (duration >>> 32));
        result = 31 * result + (data != null ? data.hashCode() : 0);
        result = 31 * result + (int) (dateAdded ^ (dateAdded >>> 32));
        result = 31 * result + (int) (dateModified ^ (dateModified >>> 32));
        result = 31 * result + (int)albumId;
        result = 31 * result + (albumName != null ? albumName.hashCode() : 0);
        result = 31 * result + (int)artistId;
        result = 31 * result + (artistName != null ? artistName.hashCode() : 0);
        // Note: Ignore since it's not supported by MediaStore: result = 31 * result + (genre != null ? genre.hashCode() : 0);

        return result;
    }

    @Override
    public String toString() {
        return "Song{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", trackNumber=" + trackNumber +
                ", discNumber=" + discNumber +
                ", year=" + year +
                ", duration=" + duration +
                ", data='" + data + '\'' +
                ", dateAdded=" + dateAdded +
                ", dateModified=" + dateModified +
                ", albumId=" + albumId +
                ", albumName='" + albumName + '\'' +
                ", artistId=" + artistId +
                ", artistName='" + artistName + '\'' +
                ", genre='" + genre + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.title);
        dest.writeInt(this.trackNumber);
        // Note: Ignore since it's not supported by MediaStore: dest.writeInt(this.discNumber);
        dest.writeInt(this.year);
        dest.writeLong(this.duration);
        dest.writeString(this.data);
        dest.writeLong(this.dateAdded);
        dest.writeLong(this.dateModified);
        dest.writeLong(this.albumId);
        dest.writeString(this.albumName);
        dest.writeLong(this.artistId);
        dest.writeString(this.artistName);
        // Note: Ignore since it's not supported by MediaStore: dest.writeString(this.genre);
    }

    protected Song(Parcel in) {
        this.id = in.readLong();
        this.title = in.readString();
        this.trackNumber = in.readInt();
        // Note: Ignore since it's not supported by MediaStore: this.discNumber = in.readInt();
        this.year = in.readInt();
        this.duration = in.readLong();
        this.data = in.readString();
        this.dateAdded = in.readLong();
        this.dateModified = in.readLong();
        this.albumId = in.readLong();
        this.albumName = in.readString();
        this.artistId = in.readLong();
        this.artistName = in.readString();
        // Note: Ignore since it's not supported by MediaStore: this.genre = in.readString();
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
