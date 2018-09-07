package com.poupa.vinylmusicplayer.loader;

import com.poupa.vinylmusicplayer.model.Song;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.TagField;

public class ReplaygainTagExtractor {

  public static void setReplaygainValues(Song song) {
    song.replaygainTrack = 0.0f;
    song.replaygainAlbum = 0.0f;

    try {
      AudioFile file = AudioFileIO.read(new File(song.data));
      Tag tag = file.getTag();
      Map<String, Float> tags = null;

      if (tag.hasField("TXXX")) {
        tags = formatFrame(tag.getFields("TXXX"));
      } else if (tag.hasField("RGAD")) {                  // may support legacy metadata formats: RGAD, RVA2
        tags = formatFrame(tag.getFields("RGAD"));
      } else if (tag.hasField("RVA2")) {
        tags = formatFrame(tag.getFields("RVA2"));
      }

      if (tags != null) {
        if(tags.containsKey("REPLAYGAIN_TRACK_GAIN")) {
          song.replaygainTrack = tags.get("REPLAYGAIN_TRACK_GAIN");
        } else if (tags.containsKey("TRACK")) {               // may support RVA2 key string ?
          song.replaygainTrack = tags.get("TRACK");
        }
        if(tags.containsKey("REPLAYGAIN_ALBUM_GAIN")) {
          song.replaygainAlbum = tags.get("REPLAYGAIN_ALBUM_GAIN");
        } else if (tags.containsKey("ALBUM")) {               // may support RVA2 key string ?
          song.replaygainTrack = tags.get("ALBUM");
        }
      }
    } catch (CannotReadException | IOException | ReadOnlyFileException | TagException | InvalidAudioFrameException e) {
      e.printStackTrace();
    }
  }

  private static Map<String, Float> formatFrame(List<TagField> fields) {
    Map<String, Float> tags = new HashMap<>();

    for (TagField field : fields) {
      String data[] = field.toString().split(";");
      tags.put(data[0].substring(13, data[0].length() - 1).toUpperCase(),  Float.parseFloat(data[1].replaceAll("[^0-9.-]","")));
    }

    return tags;
  }
}
