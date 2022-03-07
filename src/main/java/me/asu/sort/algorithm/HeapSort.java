package me.asu.sort.algorithm;

import java.lang.reflect.Array;
import java.util.Comparator;

/**
 * 
 * @version 1.0
 * @since 2004-10-21
 */
public class HeapSort extends AbstractSort {

    @Override
    public String algorithm() {
        return "heap";
    }

    public <T> void sort(T[] data) {
        MaxHeap<T> h = new MaxHeap<T>();
        h.init(data);
        for (int i = 0; i < data.length; i++) {
            h.remove();
        }
        System.arraycopy(h.queue, 1, data, 0, data.length);
    }

    public <T> void sort(T[] data, Comparator<? super T> c) {
        MaxHeap<T> h = new MaxHeap<T>();
        h.init(data, c);
        for (int i = 0; i < data.length; i++) {
            h.remove();
        }
        System.arraycopy(h.queue, 1, data, 0, data.length);
    }

    private class MaxHeap<T> {

        private int size = 0;

        private T[] queue;

        private Comparator<? super T> c = null;

        @SuppressWarnings("unchecked")
        void init(T[] data, Comparator<? super T> c) {
            if (data == null || data.length == 0) {
                return;
            }

            this.queue = (T[]) Array.newInstance(data[0].getClass(), data.length + 1);

            this.c = c;

            for (int i = 0; i < data.length; i++) {
                queue[++size] = data[i];
                fixUp(size);
            }
        }

        @SuppressWarnings("unchecked")
        void init(T[] data) {
            if (data == null || data.length == 0) {
                return;
            }

            this.queue = (T[]) Array.newInstance(data[0].getClass(), data.length + 1);
            for (int i = 0; i < data.length; i++) {
                queue[++size] = data[i];
                fixUp(size);
            }
        }

        public void remove() {
            swap(queue, 1, size--);
            fixDown(1);
        }

        // fixdown
        @SuppressWarnings("unchecked")
        private void fixDown(int k) {
            int j;
            if (this.c == null) {
                while ((j = k << 1) <= size) {
                    if (j < size && (((Comparable) queue[j]).compareTo(queue[j + 1]) < 0)) {
                        j++;
                    }
                    if (((Comparable) queue[k]).compareTo(queue[j]) > 0) // 不用交换
                    {
                        break;
                    }
                    swap(queue, j, k);
                    k = j;
                }
            } else {
                while ((j = k << 1) <= size) {
                    if (j < size && (c.compare(queue[j], queue[j + 1]) < 0)) {
                        j++;
                    }
                    if (c.compare(queue[k], queue[j]) > 0) // 不用交换
                    {
                        break;
                    }
                    swap(queue, j, k);
                    k = j;
                }
            }
        }

        @SuppressWarnings("unchecked")
        private void fixUp(int k) {
            if (this.c == null) {
                while (k > 1) {
                    int j = k >> 1;
                    if (((Comparable) queue[k]).compareTo(queue[j]) < 0) {
                        break;
                    }
                    swap(queue, j, k);
                    k = j;
                }
            } else {
                while (k > 1) {
                    int j = k >> 1;
                    if (c.compare(queue[k], queue[j]) < 0) {
                        break;
                    }
                    swap(queue, j, k);
                    k = j;
                }

            }
        }

    }

}
