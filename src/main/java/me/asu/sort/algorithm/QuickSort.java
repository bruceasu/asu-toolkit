package me.asu.sort.algorithm;

import java.util.Comparator;

/**
   * @version 1.0
   * @since 2004-10-21
   */
  public class QuickSort extends AbstractSort {
    public <T> void sort(T[] data) {
      quickSort(data, 0, data.length - 1);
    }

    @Override
    public String algorithm() {
        return "quick";
    }

    @Override
    public <T> void sort(T[] data, Comparator<? super T> c) {
      quickSort(data, 0, data.length - 1, c);
    }

    private <T> void quickSort(T[] data, int i, int j) {
      int pivotIndex = (i + j) / 2;
      // swap
      swap(data, pivotIndex, j);

      int k = partition(data, i - 1, j, data[j]);
      swap(data, k, j);
        if ((k - i) > 1) {
            quickSort(data, i, k - 1);
        }
        if ((j - k) > 1) {
            quickSort(data, k + 1, j);
        }

    }

    /**
     * @param data
     * @param l
     * @param r
     * @param pivot
     * @return int
     */
    @SuppressWarnings("unchecked")
    private <T> int partition(T[] data, int l, int r, T pivot) {
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
      return l;
    }

    private <T> void quickSort(T[] data, int i, int j,
                               Comparator<? super T> c) {
      int pivotIndex = (i + j) / 2;
      // swap
      swap(data, pivotIndex, j);

      int k = partition(data, i - 1, j, data[j], c);
      swap(data, k, j);
        if ((k - i) > 1) {
            quickSort(data, i, k - 1);
        }
        if ((j - k) > 1) {
            quickSort(data, k + 1, j);
        }

    }

    /**
     * @param data
     * @param l
     * @param r
     * @param pivot
     * @return int
     */
    private <T> int partition(T[] data, int l, int r, T pivot,
                              Comparator<? super T> c) {
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
      return l;
    }

  }

