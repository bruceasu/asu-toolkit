package me.asu.util.retry;

import java.util.concurrent.TimeUnit;

/**
 * Abstraction for retry policies to sleep
 */
public interface RetrySleeper {

    /**
     * Sleep for the given time
     *
     * @param time time
     * @param unit time unit
     * @throws InterruptedException if the sleep is interrupted
     */
    void sleepFor(long time, TimeUnit unit) throws InterruptedException;

    public static RetrySleeper create() {
        return new DefaultRetrySleeper();
    }

    static class DefaultRetrySleeper implements RetrySleeper {

        @Override
        public void sleepFor(long time, TimeUnit unit)
        throws InterruptedException {
            unit.sleep(time);
        }
    }

}
