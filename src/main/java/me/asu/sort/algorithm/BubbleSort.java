package me.asu.sort.algorithm;

import java.util.Comparator;

/**
 * 
 * @version 1.0
 * @since 2004-10-21
 */
public class BubbleSort extends AbstractSort {

    @Override
    public String algorithm() {
        return "bubble";
    }

    @SuppressWarnings("unchecked")
    public <T> void sort(T[] data) {
        for (int i = 0; i < data.length; i++) {
            for (int j = data.length - 1; j > i; j--) {
                if (((Comparable) data[j]).compareTo(data[j - 1]) < 0) {
                    swap(data, j, j - 1);
                }
            }
        }
    }

    public <T> void sort(T[] data, Comparator<? super T> c) {
        for (int i = 0; i < data.length; i++) {
            for (int j = data.length - 1; j > i; j--) {
                if (c.compare(data[j], data[j - 1]) < 0) {
                    swap(data, j, j - 1);
                }
            }
        }
    }
}