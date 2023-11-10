package com.poupa.vinylmusicplayer.discog.tagging;

import androidx.annotation.NonNull;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.flac.FlacTag;
import org.jaudiotagger.tag.mp4.Mp4Tag;
import org.jaudiotagger.tag.vorbiscomment.VorbisCommentTag;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

public class ReplayGainTagExtractor {
  // Normalize all tags using the Vorbis ones
  private static final String REPLAYGAIN_TRACK_GAIN = "REPLAYGAIN_TRACK_GAIN";
  private static final String REPLAYGAIN_ALBUM_GAIN = "REPLAYGAIN_ALBUM_GAIN";
  private static final String REPLAYGAIN_TRACK_PEAK = "REPLAYGAIN_TRACK_PEAK";
  private static final String REPLAYGAIN_ALBUM_PEAK = "REPLAYGAIN_ALBUM_PEAK";

  public static class ReplayGainValues {float track = 0; float album = 0; float peakTrack = 1.0f; float peakAlbum = 1.0f;}

  static ReplayGainValues setReplayGainValues(@NonNull final AudioFile file) {
    Map<String, Float> tags = null;

    try {
      Tag tag = file.getTag();

      if (tag instanceof VorbisCommentTag || tag instanceof FlacTag) {
        tags = parseTags(tag);
      } else if (tag instanceof Mp4Tag) {
        tags = parseMp4Tags(tag);
      } else {
        tags = parseId3Tags(tag, file);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    ReplayGainValues result = new ReplayGainValues();
    if (tags != null && !tags.isEmpty()) {
      if (tags.containsKey(REPLAYGAIN_TRACK_GAIN)) {
        result.track = tags.get(REPLAYGAIN_TRACK_GAIN);
      }
      if (tags.containsKey(REPLAYGAIN_TRACK_PEAK)) {
        result.peakTrack = tags.get(REPLAYGAIN_TRACK_PEAK);
      }
      if (tags.containsKey(REPLAYGAIN_ALBUM_GAIN)) {
        result.album = tags.get(REPLAYGAIN_ALBUM_GAIN);
      }
      if (tags.containsKey(REPLAYGAIN_ALBUM_PEAK)) {
        result.peakAlbum = tags.get(REPLAYGAIN_ALBUM_PEAK);
      }
    }
    return result;
  }

  private static Map<String, Float> parseId3Tags(Tag tag, @NonNull final AudioFile file) throws Exception {
    String id = null;

    if (tag.hasField("TXXX")) {
      id = "TXXX";
    } else if (tag.hasField("RGAD")) {    // may support legacy metadata formats: RGAD, RVA2
      id = "RGAD";
    } else if (tag.hasField("RVA2")) {
      id = "RVA2";
    }

    if (id == null) return parseLameHeader(file);

    Map<String, Float> tags = new HashMap<>();

    for (TagField field : tag.getFields(id)) {
      String[] data = field.toString().split(";");

      data[0] = data[0].substring(13, data[0].length() - 1).toUpperCase();

      if (data[0].equals("TRACK")) {data[0] = REPLAYGAIN_TRACK_GAIN;}
      else if (data[0].equals("ALBUM")) {data[0] = REPLAYGAIN_ALBUM_GAIN;}

      tags.put(data[0], parseFloat(data[1]));
    }

    return tags;
  }

  private static Map<String, Float> parseTags(Tag tag) {
    Map<String, Float> tags = new HashMap<>();

    if (tag.hasField(REPLAYGAIN_TRACK_GAIN)) {
      tags.put(REPLAYGAIN_TRACK_GAIN, parseFloat(tag.getFirst(REPLAYGAIN_TRACK_GAIN)));
    }
    if (tag.hasField(REPLAYGAIN_TRACK_PEAK)) {
      tags.put(REPLAYGAIN_TRACK_PEAK, parseFloat(tag.getFirst(REPLAYGAIN_TRACK_PEAK)));
    }
    if (tag.hasField(REPLAYGAIN_ALBUM_GAIN)) {
      tags.put(REPLAYGAIN_ALBUM_GAIN, parseFloat(tag.getFirst(REPLAYGAIN_ALBUM_GAIN)));
    }
    if (tag.hasField(REPLAYGAIN_ALBUM_PEAK)) {
      tags.put(REPLAYGAIN_ALBUM_PEAK, parseFloat(tag.getFirst(REPLAYGAIN_ALBUM_PEAK)));
    }

    return tags;
  }

  private static Map<String, Float> parseMp4Tags(Tag tag) {
    Map<String, Float> tags = parseTags(tag);

    final String ITUNES_PREFIX = "----:com.apple.iTunes:";
    if (!tags.containsKey(REPLAYGAIN_TRACK_GAIN) && tag.hasField(ITUNES_PREFIX + REPLAYGAIN_TRACK_GAIN)) {
      tags.put(REPLAYGAIN_TRACK_GAIN, parseFloat(tag.getFirst(ITUNES_PREFIX + REPLAYGAIN_TRACK_GAIN)));
    }
    if (!tags.containsKey(REPLAYGAIN_TRACK_PEAK) && tag.hasField(ITUNES_PREFIX + REPLAYGAIN_TRACK_PEAK)) {
      tags.put(REPLAYGAIN_TRACK_PEAK, parseFloat(tag.getFirst(ITUNES_PREFIX + REPLAYGAIN_TRACK_PEAK)));
    }

    if (!tags.containsKey(REPLAYGAIN_ALBUM_GAIN) && tag.hasField(ITUNES_PREFIX + REPLAYGAIN_ALBUM_GAIN)) {
      tags.put(REPLAYGAIN_ALBUM_GAIN, parseFloat(tag.getFirst(ITUNES_PREFIX + REPLAYGAIN_ALBUM_GAIN)));
    }
    if (!tags.containsKey(REPLAYGAIN_ALBUM_PEAK) && tag.hasField(ITUNES_PREFIX + REPLAYGAIN_ALBUM_PEAK)) {
      tags.put(REPLAYGAIN_ALBUM_PEAK, parseFloat(tag.getFirst(ITUNES_PREFIX + REPLAYGAIN_ALBUM_PEAK)));
    }

    return tags;
  }

  private static Map<String, Float> parseLameHeader(@NonNull final AudioFile file) throws IOException {
    // Method taken from adrian-bl/bastp library
    // Peak values are as per http://gabriel.mp3-tech.org/mp3infotag.html
    Map<String, Float> tags = new HashMap<>();
    RandomAccessFile s = new RandomAccessFile(file.getFile(), "r");
    byte[] chunk = new byte[12];

    s.seek(0x24);
    s.read(chunk);

    String lameMark = new String(chunk, 0, 4, "ISO-8859-1");

    if (lameMark.equals("Info") || lameMark.equals("Xing")) {
      s.seek(0xA7);
      s.read(chunk);

      int rawPeak =  b2be32(chunk);
      if (rawPeak != 0) {
        float peak = Float.intBitsToFloat(rawPeak);
        if (!Float.isNaN(peak)) {
          tags.put(REPLAYGAIN_TRACK_PEAK, peak);
          tags.put(REPLAYGAIN_ALBUM_PEAK, peak);
        }
      }

      s.read(chunk);

      int raw = b2be32(chunk);
      int gtrk_raw = raw >> 16;     /* first 16 bits are the raw track gain value */
      int galb_raw = raw & 0xFFFF;  /* the rest is for the album gain value       */

      float gtrk_val = (float) (gtrk_raw & 0x01FF) / 10;
      float galb_val = (float) (galb_raw & 0x01FF) / 10;

      gtrk_val = ((gtrk_raw & 0x0200) != 0 ? -1 * gtrk_val : gtrk_val);
      galb_val = ((galb_raw & 0x0200) != 0 ? -1 * galb_val : galb_val);

      if ((gtrk_raw & 0xE000) == 0x2000) {
        tags.put(REPLAYGAIN_TRACK_GAIN, gtrk_val);
      }
      if ((gtrk_raw & 0xE000) == 0x4000) {
        tags.put(REPLAYGAIN_ALBUM_GAIN, galb_val);
      }
    }

    return tags;
  }

  private static int b2le32(byte[] b) {
    int r = 0;
    for(int i=0; i<4; i++) {
      r |= ( b2u(b[i]) << (8*i) );
    }
    return r;
  }

  private static int b2be32(byte[] b) {
    return swap32(b2le32(b));
  }

  private static int swap32(int i) {
    return((i&0xff)<<24)+((i&0xff00)<<8)+((i&0xff0000)>>8)+((i>>24)&0xff);
  }

  private static int b2u(byte x) {
    return (x & 0xFF);
  }

  private static float parseFloat(String s) {
    float result = 0.0f;
    try {
      s = s.replaceAll("[^0-9.-]","");
      result = Float.parseFloat(s);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return result;
  }
}
