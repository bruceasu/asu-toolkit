package me.asu.util.retry;


/**
 * Retry policy that retries a set number of times with an increasing (up to a maximum bound) sleep time between retries
 */
public class BoundedExponentialBackoffRetry extends ExponentialBackoffRetry {

    private final int maxSleepTimeMs;

    /**
     * @param baseSleepTimeMs initial amount of time to wait between retries
     * @param maxSleepTimeMs  maximum amount of time to wait between retries
     * @param maxRetries      maximum number of times to retry
     */
    public BoundedExponentialBackoffRetry(int baseSleepTimeMs,
            int maxSleepTimeMs,
            int maxRetries) {
        super(baseSleepTimeMs, maxRetries);
        this.maxSleepTimeMs = maxSleepTimeMs;
    }

    public int getMaxSleepTimeMs() {
        return maxSleepTimeMs;
    }

    @Override
    protected long getSleepTimeMs(int retryCount, long elapsedTimeMs) {
        return Math.min(maxSleepTimeMs, super.getSleepTimeMs(retryCount, elapsedTimeMs));
    }
}
