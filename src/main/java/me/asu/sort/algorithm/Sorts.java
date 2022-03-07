package me.asu.sort.algorithm;

import java.util.HashMap;
import java.util.Map;
import me.asu.sort.Sort;
import me.asu.sort.algorithm.*;

public class Sorts {
    private static Map<String, Sort> algorithms = new HashMap<>();

    static {
        Sort[]   impl = new Sort[]{new InsertSort(), new BubbleSort(),
                new SelectionSort(), new ShellSort(), new QuickSort(), new ImprovedQuickSort(),
                new MergeSort(), new ImprovedMergeSort(), new HeapSort()};
        for (int i = 0; i < impl.length; i++) {
            algorithms.put(impl[i].algorithm(), impl[i]);
        }
    }

    public static Sort get(String algorithm) {
        return algorithms.get(algorithm);
    }
}
