package me.asu.util.retry;

import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;


/**
 * {@link RetryPolicy} implementation that always <i>allowsRetry</i>.
 */
@Slf4j
public class RetryForever implements RetryPolicy {

    private final int retryIntervalMs;

    public RetryForever(int retryIntervalMs) {
        if (retryIntervalMs < 1) {
            throw new RuntimeException("retryIntervalMs must greater then 1.");
        }
        this.retryIntervalMs = retryIntervalMs;
    }

    @Override
    public boolean allowRetry(int retryCount,
            long elapsedTimeMs,
            RetrySleeper sleeper) {
        try {
            sleeper.sleepFor(retryIntervalMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Error occurred while sleeping", e);
            return false;
        }
        return true;
    }
}

