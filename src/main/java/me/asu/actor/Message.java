package me.asu.actor;

public interface Message extends MessageListenerFriable {
	Actor getSource();
	String getSubject();
	Object getData();
	/** Ge the delay value. */
	default long getDelayUntil() {return -1L;}

	/**
	 * Used to delay message execution until some moment in time has passed.
	 *
	 * @param delayUntil
	 *            future time (in millis since epoch)
	 **/
	void setDelayUntil(long delayUntil);
	Message assignSender(Actor sender);
	boolean subjectMatches(String s);
}
