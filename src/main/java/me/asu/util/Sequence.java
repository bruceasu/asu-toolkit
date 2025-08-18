
package me.asu.util;

import java.util.concurrent.atomic.AtomicLong;

public class Sequence {
    /**
     * 开始时间截 (2018-02-16 即黄帝纪年4716大年初一)
     */
    private static final long twepoch = 1518710400000L;
    final AtomicLong seq;

    public Sequence() {
        // 10 位
        this(((System.currentTimeMillis() - twepoch) / 1000 << 30));
    }

    public Sequence(final long initial) {
        seq = new AtomicLong(initial);
    }

    public long nextId() {
        return seq.incrementAndGet();
    }

    public String nextIdString() {
        return Base62.encode(seq.incrementAndGet());
    }

    public static void main(String[] args) {
        long x = (System.currentTimeMillis() - twepoch) / 1000;
        System.out.println(x);
        System.out.println(Long.toHexString(x));
        System.out.println((x * 1000000000L));
        System.out.println(Long.toHexString(x * 1000000000L));
        Sequence seq = new Sequence();
        for (int i = 0; i < 5; i++) {
            System.out.println(seq.nextId());
            //System.out.println(seq.nextIdString());
        }
    }

}
