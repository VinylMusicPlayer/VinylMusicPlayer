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

import static android.view.Menu.NONE;

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
    public @IdRes int menuResourceId;

    /** the menu string ID that is assigned to this sort order */
    public int menuTextId;
}

class Utils {
    static <T> SortOrder<T> build(
            @NonNull String preferenceValue,
            @NonNull Function<T, String> sectionNameBuilder,
            @NonNull Comparator<T> comparatorFunc,
            @IdRes int menuResourceId,
            @StringRes int menuTextId) {
        SortOrder<T> result = new SortOrder<>();
        result.preferenceValue = preferenceValue;
        result.sectionNameBuilder = sectionNameBuilder;
        result.comparator = comparatorFunc;
        result.menuResourceId = menuResourceId;
        result.menuTextId = menuTextId;
        return result;
    }

    static @NonNull
    String getSectionName(long seconds) {
        final Date date = new Date(1000 * seconds);
        final long millisOneDay = 1000 * 60 * 60 * 24;
        final long daysSinceToday = ((new Date()).getTime() - date.getTime()) / millisOneDay;
        String format = "yyyy";
        if (daysSinceToday >= 0) {
            if (daysSinceToday < 7) {format = "EEE";}
            else if (daysSinceToday < 365) {format = "MMM";}
        }
        return android.text.format.DateFormat.format(format, date).toString();
    }

    static @NonNull
    String getSectionName(@NonNull String name) {
        // TODO This is too much, can be simplified as: charAt[0].toUpper()
        return MusicUtil.getSectionName(name);
    }

    static <T, U> T collectionSearch(@NonNull List<T> collection, U searchValue, @NonNull Function<T, U> valueExtractor) {
        T match = null;
        for (T item : collection) {
            if (valueExtractor.apply(item).equals(searchValue)) {
                match = item;
                break;
            }
        }
        return match;
    }

    static <T> void buildMenu(@NonNull List<SortOrder<T>> collection, @NonNull SubMenu sortOrderMenu, String currentPreferenceValue) {
        for (SortOrder<T> item : collection) {
            sortOrderMenu
                    .add(0, item.menuResourceId, NONE, item.menuTextId)
                    .setChecked(currentPreferenceValue.equals(item.preferenceValue));
        }
    }
}
