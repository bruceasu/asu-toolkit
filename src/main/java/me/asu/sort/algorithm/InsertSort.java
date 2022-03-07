package me.asu.sort.algorithm;

import java.util.Comparator;

/**
 * 
 * @version 1.0
 * @since 2004-10-21
 */
public class InsertSort extends AbstractSort {

    @Override
    public String algorithm() {
        return "insert";
    }

    @SuppressWarnings("unchecked")
    public <T> void sort(T[] data) {
        for (int i = 1; i < data.length; i++) {
            for (int j = i; (j > 0) && (((Comparable) data[j]).compareTo(data[j - 1]) < 0); j--) {
                swap(data, j, j - 1);
            }
        }
    }

    public <T> void sort(T[] data, Comparator<? super T> c) {
        for (int i = 1; i < data.length; i++) {
            for (int j = i; (j > 0) && (c.compare(data[j], data[j - 1]) < 0); j--) {
                swap(data, j, j - 1);
            }
        }
    }

}