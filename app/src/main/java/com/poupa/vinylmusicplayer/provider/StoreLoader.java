package com.poupa.vinylmusicplayer.provider;

import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

/**
 * @author SC (soncaokim)
 */

public class StoreLoader {
    @NonNull
    public static ArrayList<Long> getIdsFromCursor(@Nullable Cursor cursor, @NonNull final String columnName) {
        ArrayList<Long> ids = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()) {
            int idColumn = cursor.getColumnIndex(columnName);
            do {
                ids.add(cursor.getLong(idColumn));
            } while (cursor.moveToNext());
        }

        return ids;
    }
}
