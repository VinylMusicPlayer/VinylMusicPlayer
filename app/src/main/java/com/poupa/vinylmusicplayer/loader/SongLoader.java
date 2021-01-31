package com.poupa.vinylmusicplayer.loader;

import androidx.annotation.NonNull;

import com.poupa.vinylmusicplayer.discog.Discography;
import com.poupa.vinylmusicplayer.discog.tagging.MultiValuesTagUtil;
import com.poupa.vinylmusicplayer.helper.SortOrder;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.util.ComparatorUtil;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;
import com.poupa.vinylmusicplayer.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * @author Karim Abou Zeid (kabouzeid)
 * @author SC (soncaokim)
 */
public class SongLoader {
    public static final Comparator<Song> BY_TITLE = (s1, s2) -> StringUtil.compareIgnoreAccent(s1.title, s2.title);
    public static final Comparator<Song> BY_ARTIST = (s1, s2) -> StringUtil.compareIgnoreAccent(
            MultiValuesTagUtil.infoString(s1.artistNames),
            MultiValuesTagUtil.infoString(s2.artistNames));
    public static final Comparator<Song> BY_ALBUM = (s1, s2) -> StringUtil.compareIgnoreAccent(s1.albumName, s2.albumName);
    public static final Comparator<Song> BY_YEAR_DESC = (s1, s2) -> s2.year - s1.year;
    public static final Comparator<Song> BY_DATE_ADDED = (s1, s2) -> ComparatorUtil.compareLongInts(s1.dateAdded, s2.dateAdded);
    public static final Comparator<Song> BY_DATE_ADDED_DESC = ComparatorUtil.reverse(BY_DATE_ADDED);
    public static final Comparator<Song> BY_DISC_TRACK = (s1, s2) -> (s1.discNumber != s2.discNumber)
            ? (s1.discNumber - s2.discNumber)
            : (s1.trackNumber - s2.trackNumber);

    private final static Discography discography = Discography.getInstance();

    @NonNull
    public static ArrayList<Song> getSongs(@NonNull final String query) {
        final String strippedQuery = StringUtil.stripAccent(query.toLowerCase());

        ArrayList<Song> songs = new ArrayList<>();
        for (Song song : Discography.getInstance().getAllSongs()) {
            final String strippedTitle = StringUtil.stripAccent(song.title.toLowerCase());
            if (strippedTitle.contains(strippedQuery)) {
                songs.add(song);
            }
        }
        Collections.sort(songs, getSortOrder());
        return songs;
    }

    @NonNull
    public static Comparator<Song> getSortOrder() {
        switch (PreferenceUtil.getInstance().getSongSortOrder()) {
            case SortOrder.SongSortOrder.SONG_Z_A:
                return ComparatorUtil.chain(ComparatorUtil.reverse(BY_TITLE), ComparatorUtil.reverse(BY_ARTIST));
            case SortOrder.SongSortOrder.SONG_ARTIST:
                return ComparatorUtil.chain(BY_ARTIST, BY_ALBUM);
            case SortOrder.SongSortOrder.SONG_ALBUM:
                return ComparatorUtil.chain(BY_ALBUM, BY_ARTIST);
            case SortOrder.SongSortOrder.SONG_YEAR_REVERSE:
                return ComparatorUtil.chain(BY_YEAR_DESC, BY_ARTIST);
            case SortOrder.SongSortOrder.SONG_DATE_ADDED_REVERSE:
                return ComparatorUtil.chain(BY_DATE_ADDED_DESC, BY_ARTIST);

            case SortOrder.SongSortOrder.SONG_A_Z:
            default:
                return ComparatorUtil.chain(BY_TITLE, BY_ARTIST);
        }
    }
}
