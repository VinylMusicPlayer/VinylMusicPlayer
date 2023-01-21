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

    public static <E> Comparator<E> chain(Comparator<E> c1, Comparator<E> c2, Comparator<E> c3) {
        return chain(c1, chain(c2, c3));
    }
}
