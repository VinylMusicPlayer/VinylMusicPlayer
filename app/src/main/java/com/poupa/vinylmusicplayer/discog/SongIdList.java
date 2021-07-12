package com.poupa.vinylmusicplayer.discog;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;

import com.poupa.vinylmusicplayer.App;
import com.poupa.vinylmusicplayer.model.Song;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Ordered list of songs, backed by a persistent storage
 * Can be used for playlist, play queue
 *
 * @author SC (soncaokim)
 */
class SongIdList {
     final String name;
     final List<Long> songIds = new ArrayList<>();

     private final static String SEPARATOR = ",";
     private final static String PREF_NAME_PREFIX = "SONG_IDS_";

     static List<String> loadAll() {
          ArrayList<String> result = new ArrayList<>();

          SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(App.getStaticContext());
          for (String prefName : preferences.getAll().keySet()) {
               if (prefName.startsWith(PREF_NAME_PREFIX)) {
                    result.add(prefName.substring(PREF_NAME_PREFIX.length()));
               }
          }

          return result;
     }

     SongIdList(@NonNull String name) {
          this.name = name;
          load();
     }

     void load() {
          SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(App.getStaticContext());
          String values = preferences.getString(PREF_NAME_PREFIX + name, "");

          songIds.clear();
          for (String id : values.split(SEPARATOR)) {songIds.add(Long.valueOf(id));}
     }

     void save() {
          StringBuilder values = new StringBuilder();
          for (Long id : songIds) {
               if (values.length() > 0) {values.append(SEPARATOR);}
               values.append(id);
          }

          SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(App.getStaticContext());
          preferences.edit().putString(PREF_NAME_PREFIX + name, values.toString()).apply();
     }

     @NonNull
     List<Song> asSongs(@NonNull final Map<Long, Song> availableSongsById) {
          ArrayList<Song> result = new ArrayList<>();
          ArrayList<Long> orphanIds = new ArrayList<>();

          for (Long id : songIds) {
               final Song matchingSong = availableSongsById.get(id);
               if (matchingSong != null) {
                    result.add(matchingSong);
               } else {
                    orphanIds.add(id);
               }
          }

          if (!orphanIds.isEmpty()) {
               songIds.removeAll(orphanIds);
               save();
          }
          return result;
     }
}