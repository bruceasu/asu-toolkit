package org.jboss.netty.util;


import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;

/**
 * Warn when user creates too many instances to avoid {@link OutOfMemoryError}.
 */
@Slf4j
public class SharedResourceMisuseDetector {

    private static final int MAX_ACTIVE_INSTANCES = 256;
    private final Class<?> type;
    private final AtomicLong    activeInstances = new AtomicLong();
    private final AtomicBoolean logged          = new AtomicBoolean();

    public SharedResourceMisuseDetector(Class<?> type) {
        if (type == null) {
            throw new NullPointerException("type");
        }
        this.type = type;
    }

    public void increase() {
        if (activeInstances.incrementAndGet() > MAX_ACTIVE_INSTANCES) {
            if (logged.compareAndSet(false, true)) {
                log.warn(
                        "You are creating too many " + type.getSimpleName() +
                                " instances.  " + type.getSimpleName() +
                                " is a shared resource that must be reused across the" +
                                " application, so that only a few instances are created.");
            }
        }
    }

    public void decrease() {
        activeInstances.decrementAndGet();
    }
}
