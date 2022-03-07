package me.asu.actor;

import java.util.Map;

public interface Actor extends Runnable {
	String DEFAULT_CATEGORY = "default";

	Manager getManager();

	void setManager(Manager manager);

	String getName();

	void setName(String name);

	String getCategory();

	void setCategory(String category);

	void setOptions(Map<String, Object> opts);
	Map<String, Object>  getOptions();

	boolean receive();

	boolean accept(String subject);

	void activate();

	void deactivate();

	void setSuspended(boolean f);

	boolean isSuspended();

	void shutdown();

	void setHasThread(boolean f);

	boolean isShutdown();

	int getMessageCount();

	int getMaxMessageCount();

	void addMessage(Message message);
}
