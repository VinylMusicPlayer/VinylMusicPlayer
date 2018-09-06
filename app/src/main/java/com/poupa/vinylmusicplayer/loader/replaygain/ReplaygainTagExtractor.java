package com.poupa.vinylmusicplayer.loader.replaygain;

import com.poupa.vinylmusicplayer.model.Song;
import java.util.ArrayList;
import java.util.HashMap;

public class ReplaygainTagExtractor {
  public static void setReplaygainValues(Song song) {
    HashMap tags = new Bastp().getTags(song.data);

    System.out.println("------------------------ replaygain "+ song.data);

    song.replaygainTrack = 0;
    song.replaygainAlbum = 0;

    if(tags.containsKey("REPLAYGAIN_TRACK_GAIN"))
      song.replaygainTrack = getFloatFromString((String)((ArrayList)tags.get("REPLAYGAIN_TRACK_GAIN")).get(0));
    if(tags.containsKey("REPLAYGAIN_ALBUM_GAIN"))
      song.replaygainAlbum = getFloatFromString((String)((ArrayList)tags.get("REPLAYGAIN_ALBUM_GAIN")).get(0));

    // likely OPUS
    if(tags.containsKey("R128_TRACK_GAIN"))
      song.replaygainTrack = 5.0f + getFloatFromString((String)((ArrayList)tags.get("R128_TRACK_GAIN")).get(0)) / 256.0f;
    else if(tags.containsKey("R128_BASTP_BASE_GAIN"))
      song.replaygainTrack = 0.0f + getFloatFromString((String)((ArrayList)tags.get("R128_BASTP_BASE_GAIN")).get(0)) / 256.0f;
    if(tags.containsKey("R128_ALBUM_GAIN"))
      song.replaygainAlbum = 5.0f + getFloatFromString((String)((ArrayList)tags.get("R128_ALBUM_GAIN")).get(0)) / 256.0f;

  }


  private static float getFloatFromString(String rg_raw) {
    float rg_float = 0f;
    try {
      String nums = rg_raw.replaceAll("[^0-9.-]","");
      rg_float = Float.parseFloat(nums);
    } catch(Exception ignored) {}
    return rg_float;
  }
}
