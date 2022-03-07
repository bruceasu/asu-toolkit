package me.asu.util.retry;


/**
 * A retry policy that retries until a given amount of time elapses
 */
public class RetryUntilElapsed extends SleepingRetry {

    private final int maxElapsedTimeMs;
    private final int sleepMsBetweenRetries;

    public RetryUntilElapsed(int maxElapsedTimeMs, int sleepMsBetweenRetries) {
        super(Integer.MAX_VALUE);
        this.maxElapsedTimeMs      = maxElapsedTimeMs;
        this.sleepMsBetweenRetries = sleepMsBetweenRetries;
    }

    @Override
    public boolean allowRetry(int retryCount,
            long elapsedTimeMs,
            RetrySleeper sleeper) {
        return super.allowRetry(retryCount, elapsedTimeMs, sleeper) && (
                elapsedTimeMs < maxElapsedTimeMs);
    }

    @Override
    protected long getSleepTimeMs(int retryCount, long elapsedTimeMs) {
        return sleepMsBetweenRetries;
    }
}
