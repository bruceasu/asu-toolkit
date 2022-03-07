package me.asu.actor.impl;

import static me.asu.actor.utils.Utils.isEmpty;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import me.asu.actor.*;

@Slf4j
@Data
public abstract class AbstractActor implements Actor {

    public static final int     DEFAULT_MAX_MESSAGES = 100;
    protected           Manager manager;

    @Override
    public void setManager(Manager manager) {
        if (this.manager != null && manager != null) {
            throw new IllegalStateException(
                    "cannot change manager of attached actor");
        }
        this.manager = manager;
    }

    protected String name;

    @Override
    public void setName(String name) {
        if (manager != null) {
            throw new IllegalStateException("cannot change name if manager set");
        }
        this.name = name;
    }

    protected String category = DEFAULT_CATEGORY;

    Map<String, Object> options = new HashMap<>();

    /**
     * Process a message conditionally. If testMessage() returns null no message
     * will be consumed.
     *
     * @see AbstractActor#testMessage()
     */
    @Override
    public boolean receive() {
        Message m   = testMessage();
        boolean res = m != null;
        if (res) {
            try {
                m.fireMessageListeners(new MessageEvent(this,
                                                        m,
                                                        MessageStatus.DELIVERED));
                loopBody(m);
                m.fireMessageListeners(new MessageEvent(this,
                                                        m,
                                                        MessageStatus.COMPLETED));
            } catch (Exception e) {
                m.fireMessageListeners(new MessageEvent(this,
                                                        m,
                                                        MessageStatus.FAILED));
                log.error("loop exception", e);
            }
        }
        manager.free(this);
        return res;
    }

    /**
     * Test to see if a message should be processed. Subclasses should override
     */
    @Override
    public boolean accept(String subject) {
        // default receive all subjects
        return !isEmpty(subject);
    }

    /**
     * Test the current message. Default action is to accept all.
     */
    protected Message testMessage() {
        Message res;
        synchronized (messages) {
            res = peek();
        }
        return res;
    }


    protected BlockingQueue<Message> messages = new LinkedBlockingQueue<>(
            getMaxMessageCount());

    @Override
    public int getMessageCount() {
        return messages.size();
    }

    /**
     * Limit the number of messages that can be received.  Subclasses should override.
     */
    @Override
    public int getMaxMessageCount() {
        return DEFAULT_MAX_MESSAGES;
    }

    /**
     * Queue a messaged to be processed later.
     */
    @Override
    public void addMessage(Message message) {
        if (message != null) {
            boolean offer = messages.offer(message);
            if (!offer) {
                throw new IllegalStateException("too many messages, cannot add");
            }
        }
    }

    private Message peek() {
        Message res = null;
        if (active) {
            long now = System.currentTimeMillis();
            while (true) {
                Message m = messages.poll();
                if (m == null) {
                    break;
                }
                if (m.getDelayUntil() <= now) {
                    res = m;
                    break;
                }
            }
        }
        return res;
    }


    protected boolean active;

    @Override
    public void activate() {
        active = true;
    }

    @Override
    public void deactivate() {
        active = false;
    }

    /**
     * Do startup processing.
     */
    protected void runBody() {
        Message m = new ActorMessage("init");
        getManager().send(m, null, this);
    }

    @Override
    public void run() {
        runBody();
        getManager().free(this);
    }

    protected boolean hasThread;

    protected volatile boolean shutdown = false;

    @Override
    public void shutdown() {
        shutdown = true;
    }

    protected volatile boolean suspended;

    /**
     * Process the accepted subject.
     */
    protected abstract void loopBody(Message m);

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + bodyString() + "]";
    }

    protected String bodyString() {
        return "name=" + name + ", category=" + category + ", messages="
                + messages.size();
    }
}
