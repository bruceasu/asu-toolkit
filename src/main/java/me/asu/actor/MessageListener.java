package me.asu.actor;

public interface MessageListener {
	/**
	 * Call-back for message reception.
	 * 
	 * @param e event
	 */
	void onMessage(MessageEvent e);
}
