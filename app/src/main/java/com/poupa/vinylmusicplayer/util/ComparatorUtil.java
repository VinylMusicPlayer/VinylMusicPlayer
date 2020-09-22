package com.poupa.vinylmusicplayer.util;

import java.util.Comparator;

/**
 * @author SC (soncaokim)
 */

public class ComparatorUtil {
    public static <E> Comparator<E> reverse(Comparator<E> c) {
        return (a1, a2) -> c.compare(a2, a1);
    }

    public static <E> Comparator<E> chain(Comparator<E> c1, Comparator<E> c2) {
        return (a1, a2) -> {
            final int diff = c1.compare(a1, a2);
            return (diff != 0) ? diff : c2.compare(a1, a2);
        };
    }

    public static int compareLongInts(long v1, long v2) {
        long diff = v1 - v2;

        if (diff < Integer.MIN_VALUE) {diff = Integer.MIN_VALUE;}
        if (diff > Integer.MAX_VALUE) {diff = Integer.MAX_VALUE;}
        return (int)diff;
    }
}
