package me.asu.util.retry;


/**
 * A retry policy that retries only once
 */
public class RetryOneTime extends org.apache.curator.retry.RetryNTimes {

    public RetryOneTime(int sleepMsBetweenRetry) {
        super(1, sleepMsBetweenRetry);
    }
}
