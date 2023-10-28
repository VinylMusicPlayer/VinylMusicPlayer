package com.poupa.vinylmusicplayer.service;

import androidx.annotation.NonNull;

/**
 * Created by Beesham Sarendranauth (Beesham)
 */
public class BrowsableMediaIDHelper {
    // Media IDs used on browsable items of MediaBrowser
    static final String MEDIA_ID_ROOT = "__ROOT__";
    static final String MEDIA_ID_MUSICS_BY_LAST_ADDED = "__BY_LAST_ADDED__";
    static final String MEDIA_ID_MUSICS_BY_HISTORY = "__BY_HISTORY__";
    static final String MEDIA_ID_MUSICS_BY_NOT_RECENTLY_PLAYED = "__BY_NOT_RECENTLY_PLAYED__";
    static final String MEDIA_ID_MUSICS_BY_TOP_TRACKS = "__BY_TOP_TRACKS__";
    static final String MEDIA_ID_MUSICS_BY_PLAYLIST = "__BY_PLAYLIST__";
    static final String MEDIA_ID_MUSICS_BY_ALBUM = "__BY_ALBUM__";
    static final String MEDIA_ID_MUSICS_BY_ARTIST = "__BY_ARTIST__";
    static final String MEDIA_ID_MUSICS_BY_SHUFFLE = "__BY_SHUFFLE__";
    static final String MEDIA_ID_MUSICS_BY_QUEUE = "__BY_QUEUE__";

    private static final String CATEGORY_SEPARATOR = "__/__";
    private static final String LEAF_SEPARATOR = "__|__";

    /**
     * Create a String value that represents a playable or a browsable media.
     *
     * Encode the media browsable categories, if any, and the unique music ID, if any,
     * into a single String mediaID.
     *
     * MediaIDs are of the form {categoryType}__/__{categoryValue}__|__{musicUniqueId}, to make it
     * easy to find the category (like genre) that a music was selected from, so we
     * can correctly build the playing queue. This is specially useful when
     * one music can appear in more than one list, like "by genre -> genre_1"
     * and "by artist -> artist_1".
     *
     * @param mediaID    Unique ID for playable items, or null for browseable items.
     * @param categories Hierarchy of categories representing this item's browsing parents.
     * @return A hierarchy-aware media ID.
     */
    static String createMediaID(String mediaID, String... categories) {
        StringBuilder sb = new StringBuilder();
        if (categories != null) {
            for (int i = 0; i < categories.length; i++) {
                if (!isValidCategory(categories[i])) {
                    throw new IllegalArgumentException("Invalid category: " + categories[i]);
                }
                sb.append(categories[i]);
                if (i < categories.length - 1) {
                    sb.append(CATEGORY_SEPARATOR);
                }
            }
        }
        if (mediaID != null) {
            sb.append(LEAF_SEPARATOR).append(mediaID);
        }
        return sb.toString();
    }
    static String extractCategory(@NonNull String mediaID) {
        int pos = mediaID.indexOf(LEAF_SEPARATOR);
        if (pos >= 0) {
            return mediaID.substring(0, pos);
        }
        return mediaID;
    }

    static String extractSubCategoryFromCategory(@NonNull String category) {
        int pos = category.indexOf(CATEGORY_SEPARATOR);
        if (pos >= 0) {
            return category.substring(pos + CATEGORY_SEPARATOR.length());
        }
        return null;
    }

    static String extractMusicID(@NonNull String mediaID) {
        int pos = mediaID.indexOf(LEAF_SEPARATOR);
        if (pos >= 0) {
            return mediaID.substring(pos + LEAF_SEPARATOR.length());
        }
        return null;
    }

    private static boolean isValidCategory(String category) {
        return category == null ||
                (!category.contains(CATEGORY_SEPARATOR) && !category.contains(LEAF_SEPARATOR));
    }
}
