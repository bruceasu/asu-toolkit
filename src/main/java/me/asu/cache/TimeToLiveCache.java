package me.asu.cache;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;
import lombok.Getter;
import me.asu.util.NamedThreadFactory;

public class TimeToLiveCache<K, T> implements Serializable {

    private static final long serialVersionUID = -3021761536220090818L;
    ExecutorService executorService = new ThreadPoolExecutor(0,
            Runtime.getRuntime().availableProcessors() * 2, 60L, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>(),
            new NamedThreadFactory("response-timeout-event-thread", true));
    @Getter
    private long                              timeToLive;
    private ConcurrentHashMap<K, CacheObj> cacheMap;
    private CheckThread                       checkThread;
    private List<TimeoutHandler> handlers = new ArrayList<TimeoutHandler>();
    @Getter
    private boolean shutdown = false;
    public TimeToLiveCache(final long timeToLive, final long timerInterval) {
        this.timeToLive = timeToLive;

        cacheMap = new ConcurrentHashMap<K, CacheObj>();

        if (this.timeToLive > 0 && timerInterval > 0) {
            checkThread = new CheckThread(timerInterval);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                shutdownGracefully();
            }
        }, "TimeToLiveCache-Shutdown"));
    }

    public void shutdownGracefully() {
        executorService.shutdownNow();
        try {
            executorService.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) { /*ignore*/ }
        if (checkThread != null) {
            checkThread.cancel();
        }
        shutdown = true;
    }

    public void put(K key, T value) {
        cacheMap.put(key, new CacheObj(key, value, timeToLive));
    }

    public T get(K key) {
        CacheObj c = cacheMap.get(key);

        if (c == null) {
            return null;
        } else {
            c.lastAccess = System.currentTimeMillis();
            return (T) c.obj;
        }
    }

    public T remove(K key) {
        CacheObj remove = cacheMap.remove(key);
        if (remove != null) {
            return (T) remove.obj;
        } else {
            return null;
        }
    }

    public int size() {
        return cacheMap.size();
    }

    public void addTimeoutHandler(TimeoutHandler<K, T> handler) {
        if (handler == null) {
            return;
        }
        handlers.add(handler);
    }

    public void removeTimeoutHandler(TimeoutHandler<K, T> handler) {
        if (handler == null) {
            return;
        }
        handlers.remove(handler);
    }

    private void cleanup() {
        long now = System.currentTimeMillis();
        ArrayList<K> deleteKey = null;

        synchronized (cacheMap) {
            Iterator<Entry<K, CacheObj>> itr = cacheMap.entrySet().iterator();
            deleteKey = new ArrayList<K>((cacheMap.size() / 2) + 1);
            while (itr.hasNext()) {
                Entry<K, CacheObj> next = itr.next();
                K key = next.getKey();
                CacheObj c = next.getValue();
                if (c != null && (now > (timeToLive + c.lastAccess))) {
                    deleteKey.add(key);
                }
            }
        }

        for (K key : deleteKey) {
            CacheObj remove = cacheMap.remove(key);
            notifyTimeoutObject(key, (T)remove.getObj());
            Thread.yield();
        }
    }

    private void notifyTimeoutObject(final K key, final T value) {
        if (handlers != null) {
            for (final TimeoutHandler<K, T> handler : handlers) {
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        handler.fireTimeout(key, value);
                    }
                });
            }
        }
    }

    public interface TimeoutHandler<K, T> {

        void fireTimeout(K k, T v);
    }

    class CheckThread extends Thread {

        boolean running = false;
        private long timerInterval;

        CheckThread(long timerInterval) {
            super("TimeToLiveCache-Check-Thread");
            setDaemon(true);
            this.timerInterval = timerInterval;
        }

        @Override
        public void run() {
            if (running) {
                return;
            }
            running = true;
            while (running) {
                try {
                    Thread.sleep(timerInterval);
                } catch (InterruptedException ex) {
                }
                cleanup();
            }
        }

        public void cancel() {
            running = false;
        }
    }


}
