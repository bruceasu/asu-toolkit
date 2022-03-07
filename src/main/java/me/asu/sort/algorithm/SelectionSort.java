package me.asu.sort.algorithm;

import java.util.Comparator;

/**
 * @version 1.0
 * @since 2004-10-21
 */
public class SelectionSort extends AbstractSort {

    @Override
    public String algorithm() {
        return "selection";
    }

    @SuppressWarnings("unchecked")
    public <T> void sort(T[] data) {
        for (int i = 0; i < data.length; i++) {
            int lowIndex = i;
            for (int j = data.length - 1; j > i; j--) {
                if (((Comparable) data[j]).compareTo(data[lowIndex]) < 0) {
                    lowIndex = j;
                }
            }
            swap(data, i, lowIndex);
        }
    }

    public <T> void sort(T[] data, Comparator<? super T> c) {
        for (int i = 0; i < data.length; i++) {
            int lowIndex = i;
            for (int j = data.length - 1; j > i; j--) {
                if (c.compare(data[j], data[lowIndex]) < 0) {
                    lowIndex = j;
                }
            }
            swap(data, i, lowIndex);
        }
    }
}