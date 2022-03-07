package me.asu.actor;

import java.util.EventObject;

public class MessageEvent extends EventObject {
	private MessageStatus status;
	private Message       message;
	
	public MessageStatus getStatus() {
		return status;
	}

	public MessageEvent(Object source, Message m, MessageStatus status) {
		super(source);
		this.message = m;
		this.status = status;
	}

}
