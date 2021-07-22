package com.poupa.vinylmusicplayer.discog.tagging;

import androidx.annotation.NonNull;

import com.poupa.vinylmusicplayer.model.Song;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.Tag;

import java.io.File;
import java.util.List;

/**
 * @author SC (soncaokim)
 */

public class TagExtractor {
    @FunctionalInterface
    private interface Func3Args<T1, T2, T3, R> {
        R apply(T1 arg1,T2 arg2,T3 arg3);
    }
    private static final Func3Args<Tag, FieldKey, String, String> safeGetTag = (tags, tag, defaultValue) -> {
        try {
            final String value = tags.getFirst(tag).trim();
            return (value.isEmpty()) ? defaultValue : value;
        }
        catch (KeyNotFoundException ignored) {return defaultValue;}
        catch (UnsupportedOperationException ignored){ return defaultValue;}
    };
    private static final Func3Args<Tag, FieldKey, Integer, Integer> safeGetTagAsInteger = (tags, tag, defaultValue) -> {
        try {return Integer.parseInt(safeGetTag.apply(tags, tag, String.valueOf(defaultValue)));}
        catch (NumberFormatException ignored) {return defaultValue;}
    };
    private static final Func3Args<Tag, FieldKey, Integer, Integer> safeGetTagAsReleaseYear = (tags, tag, defaultValue) -> {
        try {
            String tagValueAsText = safeGetTag.apply(tags, tag, "");
            final int MINIMAL_LENGHT = 4; // yyyy
            if (tagValueAsText.length() < MINIMAL_LENGHT) {
                return defaultValue;
            }
            return Integer.parseInt(tagValueAsText.substring(0, MINIMAL_LENGHT));
        } catch (NumberFormatException ignored) {return defaultValue;}
    };
    private static final Func3Args<Tag, FieldKey, List<String>, List<String>> safeGetTagAsList = (tags, tag, defaultValue) -> {
        try {return MultiValuesTagUtil.splitIfNeeded(tags.getAll(tag));}
        catch (KeyNotFoundException ignored) {return defaultValue;}
    };

    public static void extractTags(@NonNull Song song) {
        try {
            // Override with metadata extracted from the file ourselves
            AudioFile file = AudioFileIO.read(new File(song.data));
            Tag tags = file.getTagOrCreateAndSetDefault();
            if (tags.isEmpty() && (file instanceof MP3File)) {
                tags = ((MP3File) file).getID3v1Tag();
            }

            song.albumName = safeGetTag.apply(tags, FieldKey.ALBUM, song.albumName);
            song.artistNames  = safeGetTagAsList.apply(tags, FieldKey.ARTIST, song.artistNames);
            song.albumArtistNames = safeGetTagAsList.apply(tags, FieldKey.ALBUM_ARTIST, song.albumArtistNames);
            song.title = safeGetTag.apply(tags, FieldKey.TITLE, song.title);
            if (song.title.isEmpty()) {
                // fallback to use the file name
                song.title = file.getFile().getName();
            }

            song.genre = safeGetTag.apply(tags, FieldKey.GENRE, song.genre);
            song.discNumber = safeGetTagAsInteger.apply(tags, FieldKey.DISC_NO, song.discNumber);
            song.trackNumber = safeGetTagAsInteger.apply(tags, FieldKey.TRACK, song.trackNumber);
            song.year = safeGetTagAsReleaseYear.apply(tags, FieldKey.YEAR, song.year);

            ReplayGainTagExtractor.ReplayGainValues rgValues = ReplayGainTagExtractor.setReplayGainValues(file);
            song.replayGainAlbum = rgValues.album;
            song.replayGainTrack = rgValues.track;
        } catch (@NonNull Exception | NoSuchMethodError | VerifyError e) {
            e.printStackTrace();
        }
    }
}
