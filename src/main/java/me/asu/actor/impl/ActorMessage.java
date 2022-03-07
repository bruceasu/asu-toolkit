package me.asu.actor.impl;

import lombok.Data;
import me.asu.actor.Actor;
import me.asu.actor.Message;
import me.asu.actor.utils.Utils;

@Data
public class ActorMessage extends MessageListenersSupport implements Message {

    /**
     * works like Long.MIN_VALUE;
     */
    private long delayUntil = -1;

    private Actor source;

    private String subject;

    private Object data;

    public ActorMessage(String subject, Object data) {
        this(subject);
        this.data = data;
    }

    public ActorMessage(String subject) {
        this();
        this.subject = subject;
    }

    protected ActorMessage() {
    }

    /**
     * Set the sender of a clone of this message.
     */
    @Override
    public Message assignSender(Actor sender) {
        ActorMessage res = new ActorMessage(subject, data);
        res.source = sender;
        return res;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + bodyString() + "]";
    }

    protected String bodyString() {
        return "source=" + source + ", subject=" + subject + ", data="
                + Utils.truncate(data) + ", delay=" + delayUntil;
    }

    @Override
    public boolean subjectMatches(String s) {
        return subject != null && subject.equals(s);
    }


}
