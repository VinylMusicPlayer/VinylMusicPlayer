package com.poupa.vinylmusicplayer.sort;

import android.view.SubMenu;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.util.ComparatorUtil;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @author SC (soncaokim)
 */
public class FileSortOrder {
    private static final Comparator<File> BY_NAME = (f1, f2) -> {
        if (f1.isDirectory() && !f2.isDirectory()) {
            return -1;
        } else if (!f1.isDirectory() && f2.isDirectory()) {
            return 1;
        } else {
            return f1.getAbsolutePath().compareToIgnoreCase(f2.getAbsolutePath());
        }
    };
    private static final Comparator<File> BY_DATE_MODIFIED = Comparator.comparingLong(File::lastModified);

    private static final String SORT_ORDER_NAME = "sort_order_name";
    private static final String SORT_ORDER_NAME_REVERSE = SORT_ORDER_NAME + "_reverse";
    private static final String SORT_ORDER_DATE_MODIFIED = "sort_order_date_modified";
    private static final String SORT_ORDER_DATE_MODIFIED_REVERSE = SORT_ORDER_DATE_MODIFIED + "_reverse";

    private static final List<SortOrder<File>> SUPPORTED_ORDERS = Arrays.asList(
            Utils.build(
                    SORT_ORDER_NAME,
                    file -> Utils.getSectionName(file.getName()),
                    BY_NAME,
                    R.id.action_file_sort_order_name,
                    R.string.sort_order_name
            ),
            Utils.build(
                    SORT_ORDER_NAME_REVERSE,
                    file -> Utils.getSectionName(file.getName()),
                    ComparatorUtil.reverse(BY_NAME),
                    R.id.action_file_sort_order_name_reverse,
                    R.string.sort_order_name_reverse
            ),
            Utils.build(
                    SORT_ORDER_DATE_MODIFIED,
                    file -> Utils.getSectionName(file.lastModified() / 1000),
                    BY_DATE_MODIFIED,
                    R.id.action_file_sort_order_date_modified,
                    R.string.sort_order_date_modified
            ),
            Utils.build(
                    SORT_ORDER_DATE_MODIFIED_REVERSE,
                    file -> Utils.getSectionName(file.lastModified() / 1000),
                    ComparatorUtil.reverse(BY_DATE_MODIFIED),
                    R.id.action_file_sort_order_date_modified_reverse,
                    R.string.sort_order_date_modified_reverse
            )
    );

    @NonNull
    public static
    SortOrder<File> fromPreference(@NonNull final String preferenceValue) {
        SortOrder<File> match = Utils.collectionSearch(SUPPORTED_ORDERS, preferenceValue, x -> x.preferenceValue);
        if (match == null) {
            match = SUPPORTED_ORDERS.get(0);
        }
        return match;
    }

    @Nullable
    public static
    SortOrder<File> fromMenuResourceId(@IdRes int id) {
        // Attn: Dont provide fallback default value
        // this function can be called with an alien menu res ID, and in such case it should return null value
        return Utils.collectionSearch(SUPPORTED_ORDERS, id, x -> x.menuResourceId);
    }

    public static void buildMenu(@NonNull final SubMenu sortOrderMenu, final String preferenceValue) {
        Utils.buildMenu(SUPPORTED_ORDERS, sortOrderMenu, preferenceValue);
    }
}
