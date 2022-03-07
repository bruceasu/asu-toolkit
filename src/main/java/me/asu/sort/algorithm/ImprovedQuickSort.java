package me.asu.sort.algorithm;

import java.util.Comparator;

/**
 * 
 * @version 1.0
 * @since 2004-10-21
 */
public class ImprovedQuickSort extends AbstractSort {

    private static int MAX_STACK_SIZE = 4096;
    private static int THRESHOLD = 10;
    @Override
    public String algorithm() {
        return "improved_quick";
    }
    @SuppressWarnings("unchecked")
    public <T> void sort(T[] data) {
        int[] stack = new int[MAX_STACK_SIZE];

        int top = -1;
        T pivot;
        int pivotIndex, l, r;

        stack[++top] = 0;
        stack[++top] = data.length - 1;

        while (top > 0) {
            int j = stack[top--];
            int i = stack[top--];

            pivotIndex = (i + j) / 2;
            pivot = data[pivotIndex];

            swap(data, pivotIndex, j);

            // partition
            l = i - 1;
            r = j;
            do {
                while (((Comparable) data[++l]).compareTo(pivot) < 0) {
                    ;
                }
                while ((r != 0) && (((Comparable) data[--r]).compareTo(pivot) > 0)) {
                    ;
                }
                swap(data, l, r);
            } while (l < r);
            swap(data, l, r);
            swap(data, l, j);

            if ((l - i) > THRESHOLD) {
                stack[++top] = i;
                stack[++top] = l - 1;
            }
            if ((j - l) > THRESHOLD) {
                stack[++top] = l + 1;
                stack[++top] = j;
            }

        }
        // new InsertSort().sort(data);
        insertSort(data);
    }

    public <T> void sort(T[] data, Comparator<? super T> c) {
        int[] stack = new int[MAX_STACK_SIZE];

        int top = -1;
        T pivot;
        int pivotIndex, l, r;

        stack[++top] = 0;
        stack[++top] = data.length - 1;

        while (top > 0) {
            int j = stack[top--];
            int i = stack[top--];

            pivotIndex = (i + j) / 2;
            pivot = data[pivotIndex];

            swap(data, pivotIndex, j);

            // partition
            l = i - 1;
            r = j;
            do {
                while (c.compare(data[++l], pivot) < 0) {
                    ;
                }
                while ((r != 0) && (c.compare(data[--r], pivot) > 0)) {
                    ;
                }
                swap(data, l, r);
            } while (l < r);
            swap(data, l, r);
            swap(data, l, j);

            if ((l - i) > THRESHOLD) {
                stack[++top] = i;
                stack[++top] = l - 1;
            }
            if ((j - l) > THRESHOLD) {
                stack[++top] = l + 1;
                stack[++top] = j;
            }

        }
        // new InsertSort().sort(data);
        insertSort(data, c);
    }

    /**
     * @param data
     */
    @SuppressWarnings("unchecked")
    private <T> void insertSort(T[] data) {
        for (int i = 1; i < data.length; i++) {
            for (int j = i; (j > 0) && (((Comparable) data[j]).compareTo(data[j - 1]) < 0); j--) {
                swap(data, j, j - 1);
            }
        }
    }

    private <T> void insertSort(T[] data, Comparator<? super T> c) {
        for (int i = 1; i < data.length; i++) {
            for (int j = i; (j > 0) && (c.compare(data[j], data[j - 1]) < 0); j--) {
                swap(data, j, j - 1);
            }
        }
    }
}

