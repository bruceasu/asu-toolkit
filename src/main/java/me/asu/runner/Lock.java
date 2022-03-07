package me.asu.runner;

/**
 * 锁对象,用来控制Runner的停止
 */
public class Lock {

    private boolean stop;

    /**
     * 锁对象
     */
    public Lock() {
    }

    /**
     * 是否已经停止
     *
     * @return true, 如果已经停止
     */
    public boolean isStop() {
        return stop;
    }

    /**
     * 设置停止位
     *
     * @param stop 是否停止
     * @return 当前对象, 用于链式赋值
     */
    public Lock setStop(boolean stop) {
        this.stop = stop;
        return this;
    }

    /**
     * 设置为停止
     *
     * @return 当前对象, 用于链式赋值
     */
    public Lock stop() {
        return setStop(true);
    }

    /**
     * 唤醒所有等待本对象的线程
     */
    public void wakeup() {
        synchronized (this) {
            this.notifyAll();
        }
    }
}







