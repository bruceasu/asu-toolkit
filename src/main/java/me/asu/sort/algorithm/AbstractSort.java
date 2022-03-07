package me.asu.sort.algorithm;

import java.util.Comparator;
import me.asu.sort.Sort;

/**
 *  增加泛型支持
 * @version 1.1
 * @since 2004-10-21
 */
public abstract class AbstractSort implements Sort {
    public String algorithm() {
     return getClass().getSimpleName();
    }

    public <T> void swap(T[] data, int i, int j) {
        T temp = data[i];
        data[i] = data[j];
        data[j] = temp;
    }
}
