/*
 * Copyright (C) 2012 Andrew Neal Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.poupa.vinylmusicplayer.helper;

import android.provider.MediaStore;

/**
 * Holds all of the sort orders for each list type.
 *
 * @author Andrew Neal (andrewdneal@gmail.com)
 */
public final class SortOrder {

    /**
     * This class is never instantiated
     */
    public SortOrder() {
    }

    /**
     * Artist sort order entries.
     */
    public interface ArtistSortOrder {
        /* Artist sort order A-Z */
        String ARTIST_A_Z = MediaStore.Audio.Artists.DEFAULT_SORT_ORDER;

        /* Artist sort order Z-A */
        String ARTIST_Z_A = ARTIST_A_Z + " DESC";

        String ARTIST_DATE_MODIFIED_REVERSE = "date-modified-reverse";
    }

    /**
     * Album sort order entries.
     */
    public interface AlbumSortOrder {
        /* Album sort order A-Z */
        String ALBUM_A_Z = MediaStore.Audio.Albums.DEFAULT_SORT_ORDER;

        /* Album sort order Z-A */
        String ALBUM_Z_A = ALBUM_A_Z + " DESC";

        /* Album sort order artist */
        String ALBUM_ARTIST = MediaStore.Audio.Artists.DEFAULT_SORT_ORDER
                + ", " + MediaStore.Audio.Albums.DEFAULT_SORT_ORDER;

        /* Album sort order year */
        String ALBUM_YEAR_REVERSE = MediaStore.Audio.Media.YEAR + " DESC";

        /* Album date added */
        String ALBUM_DATE_ADDED_REVERSE = MediaStore.Audio.Media.DATE_ADDED + " DESC";

        String ALBUM_DATE_MODIFIED_REVERSE = "date-modified-reverse";
    }

    /**
     * Song sort order entries.
     */
    public interface SongSortOrder {
        /* Song sort order A-Z */
        String SONG_A_Z = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;

        /* Song sort order Z-A */
        String SONG_Z_A = SONG_A_Z + " DESC";

        /* Song sort order artist */
        String SONG_ARTIST = MediaStore.Audio.Artists.DEFAULT_SORT_ORDER;

        /* Song sort order album */
        String SONG_ALBUM = MediaStore.Audio.Albums.DEFAULT_SORT_ORDER;

        /* Song sort order year */
        String SONG_YEAR_REVERSE = MediaStore.Audio.Media.YEAR + " DESC";

        /* Song sort order date */
        String SONG_DATE_ADDED_REVERSE = MediaStore.Audio.Media.DATE_ADDED + " DESC";
    }
}
