package me.asu.util.retry;

/**
 * Retry policy that retries a max number of times
 */
public class RetryNTimes extends SleepingRetry {

    private final int sleepMsBetweenRetries;

    public RetryNTimes(int n, int sleepMsBetweenRetries) {
        super(n);
        this.sleepMsBetweenRetries = sleepMsBetweenRetries;
    }

    @Override
    protected long getSleepTimeMs(int retryCount, long elapsedTimeMs) {
        return sleepMsBetweenRetries;
    }
}
