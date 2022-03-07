package me.asu.sort;

import java.util.Comparator;
import me.asu.sort.algorithm.Sorts;

public interface Sort {

    <T> void sort(T[] data);

    <T> void sort(T[] data, Comparator<? super T> c);

    String algorithm();


    static <T> void sortData(T[] data) {
        sortData(data, "improved_quick");
    }

    static <T> void sortData(T[] data, String algorithm) {
        int index = -1;
        if (data == null || data.length == 0) {
            return;
        }
        Sort Sort = Sorts.get(algorithm);
        if (Sort != null) {
            Sort.sort(data);
        } else {
            System.out.println("There's no " + algorithm);
        }
    }


    static <T> void sortData(T[] data, String algorithm, Comparator<? super T> c) {
        int index = -1;
        if (data == null || data.length == 0) {
            return;
        }

        Sort Sort = Sorts.get(algorithm);
        if (Sort != null) {
            if (c == null) {
                Sort.sort(data);
            } else {
                Sort.sort(data, c);
            }
        } else {
            System.out.println("There's no " + algorithm);
        }
    }
}
