package com.poupa.vinylmusicplayer.loader;

import com.poupa.vinylmusicplayer.model.Song;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.flac.FlacTag;
import org.jaudiotagger.tag.vorbiscomment.VorbisCommentTag;

public class ReplaygainTagExtractor {

  public static void setReplaygainValues(Song song) {
    song.replaygainTrack = 0.0f;
    song.replaygainAlbum = 0.0f;

    Map<String, Float> tags = null;

    try {
      AudioFile file = AudioFileIO.read(new File(song.data));
      Tag tag = file.getTag();

      if (tag instanceof VorbisCommentTag) {
        tags = parseVorbisTags((VorbisCommentTag) tag);
      } else if (tag instanceof FlacTag) {
        tags = parseVorbisTags(((FlacTag) tag).getVorbisCommentTag());
      } else {
        tags = parseId3Tags(tag);
      }

    } catch (CannotReadException | IOException | ReadOnlyFileException | TagException | InvalidAudioFrameException e) {
      e.printStackTrace();
    }

    if (tags != null && !tags.isEmpty()) {
      if(tags.containsKey("REPLAYGAIN_TRACK_GAIN")) {
        song.replaygainTrack = tags.get("REPLAYGAIN_TRACK_GAIN");
      }
      if(tags.containsKey("REPLAYGAIN_ALBUM_GAIN")) {
        song.replaygainAlbum = tags.get("REPLAYGAIN_ALBUM_GAIN");
      }
    }
  }

  private static Map<String, Float> parseId3Tags(Tag tag) {
    Map<String, Float> tags = new HashMap<>();
    String id = null;

    if (tag.hasField("TXXX")) {
      id = "TXXX";
    } else if (tag.hasField("RGAD")) {    // may support legacy metadata formats: RGAD, RVA2
      id = "RGAD";
    } else if (tag.hasField("RVA2")) {
      id = "RVA2";
    }

    if (id == null) return null;

    for (TagField field : tag.getFields(id)) {
      String[] data = field.toString().split(";");

      data[0] = data[0].substring(13, data[0].length() - 1).toUpperCase();
      if (data[0].equals("TRACK")) data[0] = "REPLAYGAIN_TRACK_GAIN";
      else if (data[0].equals("ALBUM")) data[0] = "REPLAYGAIN_ALBUM_GAIN";

      tags.put(data[0],  Float.parseFloat(data[1].replaceAll("[^0-9.-]","")));
    }

    return tags;
  }

  private static Map<String, Float> parseVorbisTags(VorbisCommentTag tag) {
    Map<String, Float> tags = new HashMap<>();

    if (tag.hasField("REPLAYGAIN_TRACK_GAIN")) tags.put("REPLAYGAIN_TRACK_GAIN", Float.parseFloat(tag.getFirst("REPLAYGAIN_TRACK_GAIN").replaceAll("[^0-9.-]","")));
    if (tag.hasField("REPLAYGAIN_ALBUM_GAIN")) tags.put("REPLAYGAIN_ALBUM_GAIN", Float.parseFloat(tag.getFirst("REPLAYGAIN_ALBUM_GAIN").replaceAll("[^0-9.-]","")));

    return tags;
  }



}
