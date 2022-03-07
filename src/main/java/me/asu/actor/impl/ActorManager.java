package me.asu.actor.impl;

import static me.asu.actor.utils.Utils.isEmpty;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import me.asu.actor.*;

@Slf4j
@Data
public class ActorManager implements Manager {

    public static final int    DEFAULT_ACTOR_THREAD_COUNT = 10;
    private static      int    groupCount;
    /**
     * Configuration key for thread count.
     */
    public static final String ACTOR_THREAD_COUNT         = "threadCount";

    static class Holder {

        static ActorManager singleton = new ActorManager();
    }

    /**
     * Get the default instance. Uses Manager.properties for configuration.
     *
     * @return shared instance
     */
    public static ActorManager getInstance() {
        return Holder.singleton;
    }

    private volatile boolean initialized, running, terminated;
    private volatile long lastSendTime, lastDispatchTime;
    private volatile int sendCount, lastSendCount;
    private volatile int dispatchCount, lastDispatchCount;
    protected int trendValue = 0, maxTrendValue = 10;
    private       ThreadGroup        threadGroup;
    private       List<Thread>       threads        = new LinkedList<>();
    private final Map<String, Actor> actors         = new LinkedHashMap<>();
    private final Map<String, Actor> runnableActors = new LinkedHashMap<>();
    private final Map<String, Actor> waiters        = new LinkedHashMap<>();

    private ActorManager() {
        Map<String, Object> options = null;
        Properties          p       = new Properties();
        try {
            p.load(new FileInputStream("Manager.properties"));
        } catch (IOException e) {
            try {
                p.load(new FileInputStream("/resource/Manager.properties"));
            } catch (IOException e1) {
                log.warn("ActorManager: no configutration: ", e);
            }
        }
        if (!isEmpty(p)) {
            options = new HashMap<String, Object>();
            for (Object key : p.keySet()) {
                String skey = (String) key;
                options.put(skey, p.getProperty(skey));
            }
        }
        initialize(options);
    }

    /**
     * Initialize this manager. Call only once.
     *
     * @param options map of options
     */
    private void initialize(Map<String, Object> options) {
        if (!initialized) {
            initialized      = true;
            lastDispatchTime = lastSendTime = System.currentTimeMillis();

            threadGroup = new ThreadGroup("Manager" + (groupCount++));

            startThreadPool(options);
            startCounterThread();
        }
    }


    private void startThreadPool(Map<String, Object> options) {
        int count = getThreadCount(options);
        for (int i = 0; i < count; i++) {
            addThread("actor" + i);
        }
        running = true;
        for (Thread t : threads) {
            t.start();
        }
    }

    private int getThreadCount(Map<String, Object> options) {
        Integer count  = null;
        Object  xcount =
                options != null ? options.get(ACTOR_THREAD_COUNT) : null;
        if (xcount != null) {
            if (xcount instanceof Integer) {
                count = (Integer) xcount;
            } else {
                count = Integer.parseInt(xcount.toString());
            }
        }
        if (count == null) {
            count = DEFAULT_ACTOR_THREAD_COUNT;
        }
        return count;
    }

    /**
     * Add a dynamic thread.
     *
     * @param name
     * @return
     */
    private Thread addThread(String name) {
        Thread t = null;
        synchronized (actors) {
            if (ActorRunnables.containsKey(name)) {
                throw new IllegalStateException("already exists: " + name);
            }
            ActorRunnable r = new ActorRunnable();
            ActorRunnables.put(name, r);
            t = new Thread(threadGroup, r, name);
            threads.add(t);
            //System.out.printf("addThread: %s", name);
        }
        t.setDaemon(true);
        t.setPriority(getThreadPriority());
        return t;
    }

    private void startCounterThread() {
        Thread counter = new Thread(() -> {
            while (running) {
                try {
                    trendValue    = sendCount - dispatchCount;
                    lastSendCount = sendCount;
                    sendCount     = 0;
                    updateLastDispatchCount();
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
            sendCount = lastSendCount = 0;
            clearDispatchCount();
        });
        counter.setDaemon(true);
        counter.start();
    }


    /**
     * Detach an actor.
     */
    @Override
    public void detachActor(Actor actor) {
        String name = actor.getName();
        synchronized (actors) {
            if (actors.containsKey(name)) {
                actors.remove(name);
                runnableActors.remove(name);
                waiters.remove(name);
            } else {
                actor = null;
            }
        }
        if (actor != null) {
            actor.deactivate();
        }
    }

    /**
     * Detach all actors.
     */
    public void detachAllActors() {
        Set<String> xkeys = new HashSet<String>();
        xkeys.addAll(actors.keySet());
        Iterator<String> i = xkeys.iterator();
        while (i.hasNext()) {
            detachActor(actors.get(i.next()));
        }
        synchronized (actors) {
            actors.clear();
            runnableActors.clear();
            waiters.clear();
        }
    }

    /**
     * Count the number of actors of a given type.
     *
     * @param type the class to count (also its subclasses)
     */
    @SuppressWarnings("unchecked")
    @Override
    public int getActorCount(Class type) {
        int res = 0;
        if (type != null) {
            synchronized (actors) {
                for (String key : actors.keySet()) {
                    Actor a = actors.get(key);
                    if (type.isAssignableFrom(a.getClass())) {
                        res++;
                    }
                }
            }
        } else {
            synchronized (actors) {
                res = actors.size();
            }
        }
        return res;
    }

    /**
     * Get actors managed by this manager.
     *
     * @return actors
     */
    @Override
    public Actor[] getActors() {
        Actor[] res = new Actor[actors.size()];
        copyMembers(res);
        return res;
    }

    private void copyMembers(Actor[] res) {
        int count = 0;
        synchronized (actors) {
            for (String key : actors.keySet()) {
                res[count++] = actors.get(key);
            }
        }
    }

    private Map<String, List<Message>> sentMessages = new HashMap<>();

    private boolean recordSentMessages = false;

    /**
     * Get a list of pending messages and then clear it.
     *
     * @param actor receiving actor
     * @return
     */
    public Message[] getAndClearSentMessages(Actor actor) {
        List<Message> res = null;
        synchronized (sentMessages) {
            List<Message> l = sentMessages.get(actor.getName());
            if (!isEmpty(l)) {
                res = new LinkedList<>();
                res.addAll(l);
                l.clear();
            }
        }
        return res != null ? res.toArray(new Message[res.size()]) : null;
    }


    /**
     * Get most recent thread dispatches/second count.
     */
    public int getDispatchPerSecondCount() {
        synchronized (actors) {
            return lastDispatchCount;
        }
    }

    private void incDispatchCount() {
        synchronized (actors) {
            dispatchCount += 1;
            lastDispatchTime = System.currentTimeMillis();
        }
    }

    private void clearDispatchCount() {
        synchronized (actors) {
            dispatchCount     = 0;
            lastDispatchCount = 0;
        }
    }

    private void updateLastDispatchCount() {
        synchronized (actors) {
            lastDispatchCount = dispatchCount;
            dispatchCount     = 0;
        }
    }

    /**
     * Send a message.
     *
     * @param message message to
     * @param from    source actor
     * @param to      target actor
     * @return number of receiving actors
     */
    @Override
    public int send(Message message, Actor from, Actor to) {
        int count = 0;
        if (message != null) {
            if (to != null) {
                if (!to.isShutdown() && !to.isSuspended()
                        && to.accept(message.getSubject())) {
                    Message xmessage = message.assignSender(from);
                    to.addMessage(xmessage);
                    xmessage.fireMessageListeners(new MessageEvent(to,
                                                                   xmessage,
                                                                   MessageStatus.SENT));
                    sendCount++;
                    lastSendTime = System.currentTimeMillis();
                    if (recordSentMessages) {
                        synchronized (sentMessages) {
                            String        aname = to.getName();
                            List<Message> l     = sentMessages.get(aname);
                            if (l == null) {
                                l = new LinkedList<Message>();
                                sentMessages.put(aname, l);
                            }
                            // keep from getting too big
                            if (l.size() < 100) {
                                l.add(xmessage);
                            }
                        }
                    }
                    count++;
                    synchronized (actors) {
                        actors.notifyAll();
                    }
                }
            }
        }
        return count;
    }

    /**
     * Send a message.
     *
     * @param message message to
     * @param from    source actor
     * @param to      target actors
     * @return number of receiving actors
     */
    @Override
    public int send(Message message, Actor from, Actor[] to) {
        int count = 0;
        for (Actor a : to) {
            count += send(message, from, a);
        }
        return count;
    }

    /**
     * Send a message.
     *
     * @param message message to
     * @param from    source actor
     * @param to      target actors
     * @return number of receiving actors
     */
    @Override
    public int send(Message message, Actor from, Collection<Actor> to) {
        int count = 0;
        for (Actor a : to) {
            count += send(message, from, a);
        }
        return count;
    }

    /**
     * Send a message.
     *
     * @param message  message to
     * @param from     source actor
     * @param category target actor category
     * @return number of receiving actors
     */
    @Override
    public int send(Message message, Actor from, String category) {
        int                count      = 0;
        Map<String, Actor> xactors    = cloneActors();
        List<Actor>        catMembers = new LinkedList<Actor>();
        for (String key : xactors.keySet()) {
            Actor to = xactors.get(key);
            if (category.equals(to.getCategory()) && (to.getMessageCount()
                    < to.getMaxMessageCount())) {
                catMembers.add(to);
            }
        }
        // find an actor with lowest message count
        int   min  = Integer.MAX_VALUE;
        Actor amin = null;
        for (Actor a : catMembers) {
            int mcount = a.getMessageCount();
            if (mcount < min) {
                min  = mcount;
                amin = a;
            }
        }
        if (amin != null) {
            count += send(message, from, amin);
            // } else {
            // throw new
            // IllegalStateException("no capable actors for category: " +
            // category);
        }
        return count;
    }

    /**
     * Send a message to all actors.
     *
     * @param message message to
     * @param from    source actor
     * @return number of receiving actors
     */
    @Override
    public int broadcast(Message message, Actor from) {
        int                count   = 0;
        Map<String, Actor> xactors = cloneActors();
        for (String key : xactors.keySet()) {
            Actor to = xactors.get(key);
            count += send(message, from, to);
        }
        return count;
    }

    /**
     * Get the current categories.
     *
     * @return categories
     */
    @Override
    public Set<String> getCategories() {
        Map<String, Actor> xactors = cloneActors();
        Set<String>        res     = new TreeSet<String>();
        for (String key : xactors.keySet()) {
            Actor a = xactors.get(key);
            res.add(a.getCategory());
        }
        return res;
    }

    /**
     * Get the number of actors in a category.
     *
     * @param name
     * @return
     */
    public int getCategorySize(String name) {
        Map<String, Actor> xactors = cloneActors();
        int                res     = 0;
        for (String key : xactors.keySet()) {
            Actor a = xactors.get(key);
            if (a.getCategory().equals(name)) {
                res++;
            }
        }
        return res;
    }

    protected Map<String, Actor> cloneActors() {
        Map<String, Actor> xactors;
        synchronized (actors) {
            xactors = new HashMap<>(actors);
        }
        return xactors;
    }

    /**
     * Suspend an actor until it has a read message.
     *
     * @param actor receiving actor
     */
    @Override
    public void free(Actor actor) {
        synchronized (actors) {
            waiters.put(actor.getName(), actor);
        }
    }

    private Map<String, ActorRunnable> ActorRunnables = new HashMap<>();

    /**
     * Get the Runnable by name.
     *
     * @param name thread name
     * @return runnable
     */
    public ActorRunnable getRunnable(String name) {
        return ActorRunnables.get(name);
    }

    /**
     * Get the number of busy runnableActors (equivalent to threads).
     *
     * @return
     */
    public int getActiveRunnableCount() {
        int res = 0;
        synchronized (actors) {
            for (String key : ActorRunnables.keySet()) {
                if (ActorRunnables.get(key).hasThread) {
                    res++;
                }
            }
        }
        return res;

    }

    /**
     * Get the actor threads.
     *
     * @return
     */
    public Thread[] getThreads() {
        return threads.toArray(new Thread[threads.size()]);
    }

    /**
     * Remove a dynamic thread.
     *
     * @param name
     */
    public void removeThread(String name) {
        synchronized (actors) {
            if (!ActorRunnables.containsKey(name)) {
                throw new IllegalStateException("not running: " + name);
            }
            //System.out.printf("removeThread: %s", name);
            ActorRunnables.remove(name);
            Iterator<Thread> i = threads.iterator();
            while (i.hasNext()) {
                Thread xt = i.next();
                if (xt.getName().equals(name)) {
                    i.remove();
                    xt.interrupt();
                    break;
                }
            }
        }
    }

    /**
     * Get the thread priority to use. Default is 1 less than current.
     *
     * @return priority value
     */
    private int getThreadPriority() {
        return Math.max(Thread.MIN_PRIORITY,
                        Thread.currentThread().getPriority() - 1);
    }

    public class ActorRunnable implements Runnable {

        public boolean hasThread;
        public Actor   actor;

        @Override
        public void run() {
            int delay = 1;
            while (running) {
                try {
                    if (!procesNextActor()) {
                        synchronized (actors) {
                            // TOOD: adjust this delay; possible parameter
                            // we want to minizmize overhead (make bigger);
                            // but it has a big impact on message processing
                            // rate (makesmaller)
                            // actors.wait(delay * 1000);
                            actors.wait(100);
                        }
                        delay = Math.max(5, delay + 1);
                    } else {
                        delay = 1;
                    }
                } catch (InterruptedException e) {
                } catch (Exception e) {
                    log.error("procesNextActor exception", e);
                }
            }
        }

        protected boolean procesNextActor() {
            boolean run = false, wait = false, res = false;
            actor = null;
            synchronized (actors) {
                for (String key : runnableActors.keySet()) {
                    actor = runnableActors.remove(key);
                    break;
                }
            }
            if (actor != null) {
                // first run never started
                run = true;
                actor.setHasThread(true);
                hasThread = true;
                try {
                    actor.run();
                } finally {
                    actor.setHasThread(false);
                    hasThread = false;
                }
            } else {
                synchronized (actors) {
                    for (String key : waiters.keySet()) {
                        actor = waiters.remove(key);
                        break;
                    }
                }
                if (actor != null) {
                    // then waiting for responses
                    wait = true;
                    actor.setHasThread(true);
                    hasThread = true;
                    try {
                        res = actor.receive();
                        if (res) {
                            incDispatchCount();
                        }
                    } finally {
                        actor.setHasThread(false);
                        hasThread = false;
                    }
                }
            }
            // if (!(!run && wait && !res) && a != null) {
            // logger.trace("procesNextActor %b/%b/%b: %s", run, wait, res, a);
            // }
            return run || res;
        }
    }

    /**
     * Terminate processing and wait for all threads to stop.
     */
    @Override
    public void terminateAndWait() {
        log.trace("terminateAndWait waiting on termination of {} threads",
                  threads.size());
        terminate();
        waitForThreads();
    }

    /**
     * Wait for all threads to stop. Must have issued terminate.
     */
    private void waitForThreads() {
        if (!terminated) {
            throw new IllegalStateException("not terminated");
        }
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
            }
        }
    }

    /**
     * Terminate processing.
     */
    @Override
    public void terminate() {
        terminated = true;
        running    = false;
        for (Thread t : threads) {
            t.interrupt();
        }
        synchronized (actors) {
            for (String key : actors.keySet()) {
                actors.get(key).deactivate();
            }
        }
        sentMessages.clear();
        sendCount = lastSendCount = 0;
        clearDispatchCount();
    }

    /**
     * Create an actor and associate it with this manager.
     *
     * @param clazz the actor class
     * @param name  actor name; must be unique
     */
    @Override
    public Actor createActor(Class<? extends Actor> clazz, String name) {
        return createActor(clazz, name, null);
    }

    /**
     * Create an actor and associate it with this manager then start it
     *
     * @param clazz the actor class
     * @param name  actor name; must be unique
     */
    @Override
    public Actor createAndStartActor(Class<? extends Actor> clazz,
                                     String name) {
        return createAndStartActor(clazz, name, null);
    }

    /**
     * Create an actor and associate it with this manager then start it.
     *
     * @param clazz   the actor class
     * @param name    actor name; must be unique
     * @param options actor options
     */
    @Override
    public Actor createAndStartActor(Class<? extends Actor> clazz,
                                     String name,
                                     Map<String, Object> options) {
        Actor res = createActor(clazz, name, options);
        startActor(res);
        return res;
    }

    /**
     * Create an actor and associate it with this manager.
     *
     * @param clazz   the actor class
     * @param name    actor name; must be unique
     * @param options actor options
     */
    @Override
    public Actor createActor(Class<? extends Actor> clazz,
                             String name,
                             Map<String, Object> options) {
        Actor a = null;
        synchronized (actors) {
            if (!actors.containsKey(name)) {
                try {
                    a = clazz.newInstance();
                    a.setName(name);
                    a.setManager(this);
                    if (options != null) {
                        a.setOptions(options);
                    }
                } catch (Exception e) {
                    throw e instanceof RuntimeException ? (RuntimeException) e
                            : new RuntimeException("mapped exception: " + e, e);
                }
            } else {
                throw new IllegalArgumentException(
                        "name already in use: " + name);
            }
        }
        return a;
    }

    /**
     * Start an actor. Must have been created by this manager.
     *
     * @param actor the actor
     */
    @Override
    public void startActor(Actor actor) {
        if (actor.getManager() != this) {
            throw new IllegalStateException("actor not owned by this manager");
        }
        String name = actor.getName();
        synchronized (actors) {
            if (actors.containsKey(name)) {
                throw new IllegalStateException("already started");
            }
            actors.put(name, actor);
            runnableActors.put(name, actor);
        }
        actor.activate();
    }
}
