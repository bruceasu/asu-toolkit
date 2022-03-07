package me.asu.sort.algorithm;

import java.lang.reflect.Array;
import java.util.Comparator;

/**
 * 
 * @version 1.0
 * @since 2004-10-21
 */
public class MergeSort extends AbstractSort {

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

    @SuppressWarnings("unchecked")
    private <T> void mergeSort(T[] data, T[] temp, int l, int r) {
        int mid = (l + r) / 2;
        if (l == r) {
            return;
        }
        mergeSort(data, temp, l, mid);
        mergeSort(data, temp, mid + 1, r);
        for (int i = l; i <= r; i++) {
            temp[i] = data[i];
        }
        int i1 = l;
        int i2 = mid + 1;
        for (int cur = l; cur <= r; cur++) {
            if (i1 == mid + 1) {
                data[cur] = temp[i2++];
            } else if (i2 > r) {
                data[cur] = temp[i1++];
            } else if (((Comparable) temp[i1]).compareTo(temp[i2]) < 0) {
                data[cur] = temp[i1++];
            } else {
                data[cur] = temp[i2++];
            }
        }
    }

    private <T> void mergeSort(T[] data, T[] temp, int l, int r, Comparator<? super T> c) {
        int mid = (l + r) / 2;
        if (l == r) {
            return;
        }
        mergeSort(data, temp, l, mid);
        mergeSort(data, temp, mid + 1, r);
        for (int i = l; i <= r; i++) {
            temp[i] = data[i];
        }
        int i1 = l;
        int i2 = mid + 1;
        for (int cur = l; cur <= r; cur++) {
            if (i1 == mid + 1) {
                data[cur] = temp[i2++];
            } else if (i2 > r) {
                data[cur] = temp[i1++];
            } else if (c.compare(temp[i1], temp[i2]) < 0) {
                data[cur] = temp[i1++];
            } else {
                data[cur] = temp[i2++];
            }
        }
    }

}
