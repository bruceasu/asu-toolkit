
package me.asu.util;

import java.io.Serializable;

public class SnowflakeIdWorker implements Serializable {

    private static final long serialVersionUID = 4970304228204579351L;
    // ==============================Fields===========================================
    /**
     * 开始时间截 (2018-02-16 即黄帝纪年4716大年初一)
     */
    private final long twepoch = 1518710400000L;

    /**
     * 机器id所占的位数
     */
    private final int workerIdBits = 4;


    /**
     * 支持的最大机器id，结果是63 (这个移位算法可以很快的计算出几位二进制数所能表示的最大十进制数)
     */
    private final int maxWorkerId = -1 ^ (-1 << workerIdBits);


    /**
     * 序列在id中占的位数
     */
    private final int sequenceBits = 12;

    /**
     * 机器ID向左移12位
     */
    private final int workerIdShift = sequenceBits;

    /**
     * 时间截向左移16位(4+12)
     */
    private final int timestampLeftShift = sequenceBits + workerIdBits;

    /**
     * 生成序列的掩码，这里为4095 (0b111111111111=0xfff=4095)
     */
    private final int sequenceMask = -1 ^ (-1 << sequenceBits);

    /**
     * 工作机器ID(0~63)
     */
    private long workerId;

    /**
     * 数据中心ID(0~15)
     */
    private long datacenterId;

    /**
     * 毫秒内序列(0~4095)
     */
    private long sequence = 0L;

    /**
     * 上次生成ID的时间截
     */
    private long lastTimestamp = -1L;

    //==============================Constructors=====================================

    /**
     * 构造函数
     *
     * @param workerId     工作ID (0~63)
     * @param datacenterId 数据中心ID (0~15)
     */
    public SnowflakeIdWorker(long workerId, long datacenterId) {
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(
                    String.format("worker Id can't be greater than %d or less than 0",
                            maxWorkerId));
        }

        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    // ==============================Methods==========================================

    /**
     * 获得下一个ID (该方法是线程安全的)
     *
     * @return SnowflakeId
     */
    public synchronized long nextId() {
        long timestamp = timeGen();

        //如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过这个时候应当抛出异常
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(String.format(
                    "Clock moved backwards.  Refusing to generate id for %d milliseconds",
                    lastTimestamp - timestamp));
        }

        //如果是同一时间生成的，则进行毫秒内序列
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            //毫秒内序列溢出
            if (sequence == 0) {
                //阻塞到下一个毫秒,获得新的时间戳
                timestamp = tilNextMillis(lastTimestamp);
            }
        }
        //时间戳改变，毫秒内序列重置
        else {
            sequence = 0L;
        }

        //上次生成ID的时间截
        lastTimestamp = timestamp;

        //移位并通过或运算拼到一起组成64位的ID
        return ((timestamp - twepoch) << timestampLeftShift) //
                | (workerId << workerIdShift) //
                | sequence;
    }

    public String nextIdStr() {
        return Base62.encode(nextId());
    }
    /**
     * 返回以毫秒为单位的当前时间
     *
     * @return 当前时间(毫秒)
     */
    protected long timeGen() {
        return System.currentTimeMillis();
    }

    /**
     * 阻塞到下一个毫秒，直到获得新的时间戳
     * ：www.xttblog.com
     *
     * @param lastTimestamp 上次生成ID的时间截
     * @return 当前时间戳
     */
    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    public static void main(String[] args) {
        SnowflakeIdWorker gen = new SnowflakeIdWorker(0,0);
        for (int i = 0; i < 20; i++) {
            final long x = gen.nextId();
            System.out.println(x);
            System.out.println(Long.toBinaryString(x));
            System.out.println(Long.toHexString(x));
            System.out.println(Long.toOctalString(x));
            System.out.println("--------------------------");
        }
    }

}
