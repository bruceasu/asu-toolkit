package me.asu.runner;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 封装Runnable的带lock的启动器
 *
 */
public abstract class Runner implements Runnable {

    protected Logger log;

    /**
     * 所属的关联线程
     */
    protected Thread myThread;

    /**
     * 本运行器名称
     */
    protected String rnm;

    /**
     * 线程锁
     */
    protected Lock lock;

    /**
     * 累积启动次数
     */
    protected int count;

    /**
     * 本次睡眠时间
     */
    protected long interval;

    /**
     * 报错以后睡眠时间
     */
    protected int sleepAfterError;

    /**
     * 启动于
     */
    protected Date upAt;

    /**
     * 睡眠于，如果本值不为 null，表示本线程正在睡眠，否则为运行中
     */
    protected Date downAt;

    /**
     * 新建一个启动器
     *
     * @param rname 本启动器的名称
     */
    public Runner(String rname) {
        this.rnm             = rname;
        this.count           = 0;
        this.sleepAfterError = 30;
        this.lock            = new Lock();
    }

    /**
     * 设置报错以后睡眠时间（单位秒）
     *
     * @param sec 秒
     * @return 冷却时间
     */
    public Runner setSleepAfterError(int sec) {
        this.sleepAfterError = sec;
        return this;
    }

    /**
     * 主逻辑,用户代码不应该覆盖.
     */
    @Override
    public void run() {
        if (log == null) {
            log = LoggerFactory.getLogger(rnm);
        }
        myThread = Thread.currentThread();

        beforeStart(this);
        doIt();
        afterStop(this);
    }

    /**
     * 开始之前,一般做一些准备工作,比如资源初始化等
     *
     * @param me runner本身
     */
    protected void beforeStart(Runner me) {
    }

    /**
     * 做一些需要定期执行的操作
     */
    protected void doIt() {
        while (!lock.isStop()) {
            synchronized (lock) {
                try {
                    // 修改一下本线程的时间
                    upAt   = new Date(System.currentTimeMillis());
                    downAt = null;
                    log.debug("{} [{}] : up", rnm, ++count);

                    // 执行业务
                    interval = exec();

                    if (interval < 1) {
                        interval = 1; // 不能间隔0或者负数,会死线程的
                    }

                    // 等待一个周期
                    downAt = new Date(System.currentTimeMillis());
                    log.debug("{} [{}] : wait {} s({} ms)", rnm, count,
                            interval / 1000, interval);
                    lock.wait(interval);
                } catch (InterruptedException e) {
                    log.warn(String.format("%s has been interrupted", rnm), e);
                    break;
                } catch (Throwable e) {
                    log.warn(String.format("%s has some error", rnm), e);
                    try {
                        lock.wait(sleepAfterError * 1000);
                    } catch (Throwable e1) {
                        log.warn(String.format("%s has some error again", rnm), e);
                        break;
                    }
                }
            }
        }
    }

    /**
     * 停止之后,一般是做一些资源回收
     *
     * @param me runner本身
     */
    protected void afterStop(Runner me) {
    }

    /**
     * 具体的业务实现,返回一个sleep数
     *
     * @return 本次运行后还需要等待多少个毫秒
     */
    public abstract long exec() throws Exception;

    /**
     * 返回格式为 [名称:总启动次数] 最后启动时间:最后休眠时间 - 休眠间隔
     */
    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        return String
                .format("[%s:%d] %s/%s - %d", rnm, count,
                        upAt == null ? "NONE" : sdf.format(upAt),
                        downAt == null ? "NONE" : sdf.format(downAt), interval);
    }

    /**
     * 是否正在等待运行
     *
     * @return true, 如果正在等待
     */
    public boolean isWaiting() {
        return null != downAt;
    }

    /**
     * 是否正在执行用户代码
     *
     * @return true, 如果正在exec方法内部
     */
    public boolean isRunning() {
        return null == downAt;
    }

    /**
     * 获取执行间隔
     *
     * @return 执行间隔
     */
    public long getInterval() {
        return interval;
    }

    /**
     * 获取最后启动时间
     *
     * @return 最后启动时间
     */
    public Date getUpAt() {
        return upAt;
    }

    /**
     * 获取最后一次等待开始的时间
     *
     * @return 最后一次等待开始的时间
     */
    public Date getDownAt() {
        return downAt;
    }

    /**
     * 获取本启动器的名称
     *
     * @return 本启动器的名称
     */
    public String getName() {
        return rnm;
    }

    /**
     * 获取累计的启动次数
     *
     * @return 总启动次数
     */
    public int getCount() {
        return count;
    }

    /**
     * 获取线程NutLock锁
     *
     * @return 线程NutLock锁
     */
    public Lock getLock() {
        return lock;
    }

    /**
     * 获取所属线程是否存活
     *
     * @return 所属线程是否存活
     */
    public boolean isAlive() {
        if (myThread != null) {
            return myThread.isAlive();
        }
        return false;
    }

    /**
     * 强行关闭所属线程
     *
     * @param err 传给Thread.stop方法的对象
     */
    @SuppressWarnings("deprecation")
    public void stop(Throwable err) {
        myThread.stop(err);
    }

}







