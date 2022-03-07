package me.asu.util.retry;

/**
 * Abstracts the policy to use when retrying connections
 */
public interface RetryPolicy {

    /**
     * Called when an operation has failed for some reason. This method should
     * return true to make another attempt.
     *
     * @param retryCount    the number of times retried so far (0 the first time)
     * @param elapsedTimeMs the elapsed time in ms since the operation was attempted
     * @param sleeper       use this to sleep - DO NOT call Thread.sleep
     * @return true/false
     */
    boolean allowRetry(int retryCount,
            long elapsedTimeMs,
            RetrySleeper sleeper);
}
