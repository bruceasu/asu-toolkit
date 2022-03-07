package me.asu.sort.algorithm;

import java.lang.reflect.Array;
import java.util.Comparator;

/**
 * 
 * @version 1.0
 * @since 2004-10-21
 */
public class ImprovedMergeSort extends AbstractSort {

    private static final int THRESHOLD = 10;
    @Override
    public String algorithm() {
        return "improved_merge";
    }
    @SuppressWarnings("unchecked")
    public <T> void sort(T[] data) {
        if (data == null || data.length == 0) {
            return;
        }
        T[] temp = (T[]) Array.newInstance(data[0].getClass(), data.length);
        mergeSort(data, temp, 0, data.length - 1);
    }

    @SuppressWarnings("unchecked")
    public <T> void sort(T[] data, Comparator<? super T> c) {
        if (data == null || data.length == 0) {
            return;
        }
        T[] temp = (T[]) Array.newInstance(data[0].getClass(), data.length);
        mergeSort(data, temp, 0, data.length - 1, c);
    }

    private <T> void mergeSort(T[] data, T[] temp, int l, int r, Comparator<? super T> c) {
        int i, j, k;
        int mid = (l + r) / 2;
        if (l == r) {
            return;
        }
        if ((mid - l) >= THRESHOLD) {
            mergeSort(data, temp, l, mid, c);
        } else {
            insertSort(data, l, mid - l + 1, c);
        }
        if ((r - mid) > THRESHOLD) {
            mergeSort(data, temp, mid + 1, r, c);
        } else {
            insertSort(data, mid + 1, r - mid, c);
        }

        for (i = l; i <= mid; i++) {
            temp[i] = data[i];
        }
        for (j = 1; j <= r - mid; j++) {
            temp[r - j + 1] = data[j + mid];
        }
        T a = temp[l];
        T b = temp[r];
        for (i = l, j = r, k = l; k <= r; k++) {
            if (c.compare(a, b) < 0) {
                data[k] = temp[i++];
                a = temp[i];
            } else {
                data[k] = temp[j--];
                b = temp[j];
            }
        }
    }

    /**
     * @param data
     * @param start
     * @param len
     * @param c
     */
    private <T> void insertSort(T[] data, int start, int len, Comparator<? super T> c) {
        for (int i = start + 1; i < start + len; i++) {
            for (int j = i; (j > start) && (c.compare(data[j], data[j - 1]) < 0); j--) {
                swap(data, j, j - 1);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void mergeSort(T[] data, T[] temp, int l, int r) {
        int i, j, k;
        int mid = (l + r) / 2;
        if (l == r) {
            return;
        }
        if ((mid - l) >= THRESHOLD) {
            mergeSort(data, temp, l, mid);
        } else {
            insertSort(data, l, mid - l + 1);
        }
        if ((r - mid) > THRESHOLD) {
            mergeSort(data, temp, mid + 1, r);
        } else {
            insertSort(data, mid + 1, r - mid);
        }

        for (i = l; i <= mid; i++) {
            temp[i] = data[i];
        }
        for (j = 1; j <= r - mid; j++) {
            temp[r - j + 1] = data[j + mid];
        }
        T a = temp[l];
        T b = temp[r];
        for (i = l, j = r, k = l; k <= r; k++) {
            if (((Comparable) a).compareTo(b) < 0) {
                data[k] = temp[i++];
                a = temp[i];
            } else {
                data[k] = temp[j--];
                b = temp[j];
            }
        }
    }

    /**
     * @param data
     * @param start
     * @param len
     */
    @SuppressWarnings("unchecked")
    private <T> void insertSort(T[] data, int start, int len) {
        for (int i = start + 1; i < start + len; i++) {
            for (int j = i; (j > start) && ((Comparable) data[j]).compareTo(data[j - 1]) < 0; j--) {
                swap(data, j, j - 1);
            }
        }
    }

}

