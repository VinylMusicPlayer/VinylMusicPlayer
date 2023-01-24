package com.poupa.vinylmusicplayer.sort;

import android.view.SubMenu;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.poupa.vinylmusicplayer.util.MusicUtil;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

import android.view.Menu;

/**
 * @author SC (soncaokim)
 */
public class SortOrder<T> {
    /** The value saved in the preferences for this sort order */
    public String preferenceValue;

    /** The representative text for this item */
    public Function<T, String> sectionNameBuilder;

    /** The comparator used for this sort order*/
    public Comparator<T> comparator;

    /** the menu resource ID that is assigned to this sort order */
    @IdRes int menuResourceId;

    /** the menu string ID that is assigned to this sort order */
    int menuTextId;
}

class Utils {
    @NonNull
    static <T> SortOrder<T> build(
            @NonNull final String preferenceValue,
            @NonNull final Function<T, String> sectionNameBuilder,
            @NonNull final Comparator<T> comparatorFunc,
            @IdRes final int menuResourceId,
            @StringRes final int menuTextId) {
        final SortOrder<T> result = new SortOrder<>();
        result.preferenceValue = preferenceValue;
        result.sectionNameBuilder = sectionNameBuilder;
        result.comparator = comparatorFunc;
        result.menuResourceId = menuResourceId;
        result.menuTextId = menuTextId;
        return result;
    }

    @NonNull
    static String getSectionName(long seconds) {
        final Date date = new Date(1000L * seconds);
        final long millisOneDay = 1000L * 60L * 60L * 24L;
        final long daysSinceToday = ((new Date()).getTime() - date.getTime()) / millisOneDay;
        String format = "yyyy";
        if (daysSinceToday >= 0) {
            if (daysSinceToday < 7) {format = "EEE";}
            else if (daysSinceToday < 365) {format = "MMM";}
        }
        return android.text.format.DateFormat.format(format, date).toString();
    }

    @NonNull
    static String getSectionName(@NonNull final String name) {
        return String.valueOf(name.charAt(0)).toUpperCase();
    }

    static <T, U> T collectionSearch(@NonNull final Iterable<? extends T> collection, final U searchValue, @NonNull final Function<T, U> valueExtractor) {
        T match = null;
        for (final T item : collection) {
            if (valueExtractor.apply(item).equals(searchValue)) {
                match = item;
                break;
            }
        }
        return match;
    }

    static <T> void buildMenu(@NonNull final Iterable<? extends SortOrder<T>> collection, @NonNull final SubMenu sortOrderMenu, final String preferenceValue) {
        for (final SortOrder<T> item : collection) {
            sortOrderMenu
                    .add(0, item.menuResourceId, Menu.NONE, item.menuTextId)
                    .setChecked(preferenceValue.equals(item.preferenceValue));
        }
    }
}
