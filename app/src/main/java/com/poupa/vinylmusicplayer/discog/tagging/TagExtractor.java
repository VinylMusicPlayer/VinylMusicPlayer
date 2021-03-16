package com.poupa.vinylmusicplayer.discog.tagging;

import androidx.annotation.NonNull;

import com.poupa.vinylmusicplayer.model.Song;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.Tag;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

/**
 * @author SC (soncaokim)
 */

public class TagExtractor {
    private static final BiFunction<Tag, FieldKey, String> safeGetTag = (tags, tag) -> {
        try {return tags.getFirst(tag).trim();}
        catch (KeyNotFoundException ignored) {return "";}
        catch (UnsupportedOperationException ignored){ return "";}
    };
    private static final BiFunction<Tag, FieldKey, Integer> safeGetTagAsInteger = (tags, tag) -> {
        try {return Integer.parseInt(safeGetTag.apply(tags, tag));}
        catch (NumberFormatException ignored) {return 0;}
    };
    private static final BiFunction<Tag, FieldKey, List<String>> safeGetTagAsList = (tags, tag) -> {
        try {return tags.getAll(tag);}
        catch (KeyNotFoundException ignored) {return new ArrayList<>(Arrays.asList(""));}
    };

    public static void extractTags(@NonNull Song song) {
        try {
            // Override with metadata extracted from the file ourselves
            AudioFile file = AudioFileIO.read(new File(song.data));
            Tag tags = file.getTagOrCreateAndSetDefault();

            song.albumName = safeGetTag.apply(tags, FieldKey.ALBUM);
            song.artistNames  = MultiValuesTagUtil.splitIfNeeded(safeGetTagAsList.apply(tags, FieldKey.ARTIST));
            song.albumArtistNames = MultiValuesTagUtil.splitIfNeeded(safeGetTagAsList.apply(tags, FieldKey.ALBUM_ARTIST));
            song.title = safeGetTag.apply(tags, FieldKey.TITLE);
            if (song.title.isEmpty()) {
                // fallback to use the file name
                song.title = file.getFile().getName();
            }

            song.genre = safeGetTag.apply(tags, FieldKey.GENRE);
            song.discNumber = safeGetTagAsInteger.apply(tags, FieldKey.DISC_NO);
            song.trackNumber = safeGetTagAsInteger.apply(tags, FieldKey.TRACK);
            song.year = safeGetTagAsInteger.apply(tags, FieldKey.YEAR);

            ReplayGainTagExtractor.ReplayGainValues rgValues = ReplayGainTagExtractor.setReplayGainValues(file);
            song.replayGainAlbum = rgValues.album;
            song.replayGainTrack = rgValues.track;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
