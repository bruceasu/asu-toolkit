package me.asu.sort.algorithm;

import java.util.Comparator;

/**
 * @version 1.0
 * @since 2004-10-21
 */
public class ShellSort extends AbstractSort {

    @Override
    public String algorithm() {
        return "shell";
    }

    public <T> void sort(T[] data) {
        for (int i = data.length / 2; i > 2; i /= 2) {
            for (int j = 0; j < i; j++) {
                insertSort(data, j, i);
            }
        }
        insertSort(data, 0, 1);
    }

    public <T> void sort(T[] data, Comparator<? super T> c) {
        for (int i = data.length / 2; i > 2; i /= 2) {
            for (int j = 0; j < i; j++) {
                insertSort(data, j, i, c);
            }
        }
        insertSort(data, 0, 1, c);
    }

    /**
     * @param data
     * @param start
     * @param inc
     */
    @SuppressWarnings("unchecked")
    private <T> void insertSort(T[] data, int start, int inc) {
        for (int i = start + inc; i < data.length; i += inc) {
            for (int j = i; (j >= inc) && (((Comparable) data[j]).compareTo(data[j - inc]) < 0);
                    j -= inc) {
                swap(data, j, j - inc);
            }
        }
    }

    /**
     * @param data
     * @param start
     * @param inc
     * @param c
     */
    private <T> void insertSort(T[] data, int start, int inc, Comparator<? super T> c) {
        for (int i = start + inc; i < data.length; i += inc) {
            for (int j = i; (j >= inc) && (c.compare(data[j], data[j - inc]) < 0); j -= inc) {
                swap(data, j, j - inc);
            }
        }
    }

}