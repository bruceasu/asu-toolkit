package me.asu.actor.impl;

import lombok.extern.slf4j.Slf4j;
import me.asu.actor.Message;

@Slf4j
public abstract class SafeActor extends AbstractActor {

	@Override
	protected void loopBody(Message m) {
		try {
			log.trace("SafeActor loopBody: %s", m);
			doBody((ActorMessage) m);
		} catch (Exception e) {
			log.error("SafeActor: exception", e);
		}
	}

	@Override
	protected void runBody() {
		// by default, nothing to do
	}

	/**
	 * Override to define message reception behavior. 
	 * 
	 * @param m
	 * @throws Exception
	 */
	protected abstract void doBody(ActorMessage m) throws Exception;

}