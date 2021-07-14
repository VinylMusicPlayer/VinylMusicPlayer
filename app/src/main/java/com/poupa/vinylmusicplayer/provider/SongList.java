package com.poupa.vinylmusicplayer.provider;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.poupa.vinylmusicplayer.App;
import com.poupa.vinylmusicplayer.discog.Discography;
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
abstract class SongList {
     String name;
     final List<Long> songIds = new ArrayList<>();

     SongList(@NonNull String name) {
          this.name = name;
          load();
     }

     abstract void load();
     abstract void save(@Nullable String newName);

     @NonNull
     public ArrayList<Song> asSongs() {
          ArrayList<Song> result = new ArrayList<>();
          ArrayList<Long> orphanIds = new ArrayList<>();

          // TODO Do this cleaning and mapping once
          final Map<Long, Song> availableSongsById = Discography.getInstance().getAllSongsById();
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
               save(null);
          }
          return result;
     }
}

abstract class MutableSongList extends SongList {
     MutableSongList(@NonNull String name) {
          super(name);
     }

     void removeSong(long id) {
          songIds.remove(id);
          save(null);
          // TODO Call observers
     }

     void addSong(long id) {
          songIds.add(id);
          save(null);
          // TODO Call observers
     }

     void addSongs(@NonNull List<Song> songs) {
          for (Song song : songs) {
               songIds.add(song.id);
          }
          save(null);
          // TODO Call observers
     }

     void clear() {
          songIds.clear();
          save(null);
     }

     void rename(@NonNull final String newName) {
          save(newName);
     }
}

class PreferencesBackedSongList extends MutableSongList {
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

          // TODO Sort the result
          return result;
     }

     PreferencesBackedSongList(@NonNull String name) {
          super(name);
     }

     @Override
     void load() {
          SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(App.getStaticContext());
          String values = preferences.getString(PREF_NAME_PREFIX + name, "");

          songIds.clear();
          for (String id : values.split(SEPARATOR)) {songIds.add(Long.valueOf(id));}
     }

     @Override
     void save(@Nullable String newName) {
          StringBuilder values = new StringBuilder();
          for (Long id : songIds) {
               if (values.length() > 0) {values.append(SEPARATOR);}
               values.append(id);
          }

          SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(App.getStaticContext());
          if (newName != null) {
               preferences.edit().remove(PREF_NAME_PREFIX + name).apply();
               name = newName;
          }
          preferences.edit().putString(PREF_NAME_PREFIX + name, values.toString()).apply();
     }
}

// TODO SongListFromPlayHistory
