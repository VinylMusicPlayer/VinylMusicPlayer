package com.poupa.vinylmusicplayer.helper;

import android.content.Context;

import com.poupa.vinylmusicplayer.discog.tagging.MultiValuesTagUtil;
import com.poupa.vinylmusicplayer.model.Playlist;
import com.poupa.vinylmusicplayer.model.Song;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class M3UWriter implements M3UConstants {

    public static File write(Context context, File dir, Playlist playlist) throws IOException {
        if (!dir.exists()) { //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
        }
        File file = new File(dir, playlist.name.concat("." + EXTENSION));

        ArrayList<Song> songs = playlist.getSongs(context);

        if (songs.size() > 0) {
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));

            bw.write(HEADER);
            for (Song song : songs) {
                bw.newLine();
                bw.write(ENTRY + song.duration + DURATION_SEPARATOR + MultiValuesTagUtil.infoString(song.artistNames) + " - " + song.title);
                bw.newLine();
                bw.write(song.data);
            }

            bw.close();
        }

        return file;
    }
}
