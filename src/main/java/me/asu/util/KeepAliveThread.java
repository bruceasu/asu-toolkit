package me.asu.util;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeepAliveThread extends Thread{

	private final static Logger logger = LoggerFactory.getLogger(KeepAliveThread.class);
	
	private Consumer<Integer> consumer;
	private boolean executeOnceStart;
	private int i = 0;
	private int interval = 10000;
	
	private Object clock = new Object();
	
	boolean done = false;
	
	private Thread shutdownHookThread;
	private static boolean shutingdown = false;
	
	public KeepAliveThread(Consumer<Integer> consumer, boolean executeOnceStart) {
		this.consumer = consumer;
		this.executeOnceStart = executeOnceStart;
	}
	
	@Override
	public void run() {
		if(executeOnceStart) {
			consumer.accept(i++);	
		}
		while(!done) {
			synchronized(clock) {
				try {
					clock.wait(interval);
				} catch (InterruptedException e) {
					logger.error("", e);
				}
			}
			consumer.accept(i++);
		}
	}

	public synchronized KeepAliveThread start(int interval) {
		this.interval = interval;
		super.start();
		return this;
	}

	public int getInterval() {
		return interval;
	}

	public KeepAliveThread setInterval(int interval) {
		this.interval = interval;
		return this;
	}

	public void done() {
		this.done = true;
		try{
			if(!shutingdown && shutdownHookThread != null) {
				Runtime.getRuntime().removeShutdownHook(shutdownHookThread);
			}
			synchronized(this.clock) {
				this.clock.notify();	
			}
			this.join();
			
		} catch (InterruptedException e) {
			logger.error("", e);
		}
	}
	
	public KeepAliveThread doneBeforeJvmShutdown() {
		shutdownHookThread = new Thread() {

			@Override
			public void run() {
				shutingdown = true;
				done();
			}
			
		};
		
		Runtime.getRuntime().addShutdownHook(shutdownHookThread);
		return this;
	}
	
	public static void main(String[] args) throws InterruptedException {
		KeepAliveThread t = new KeepAliveThread(i -> {
			System.out.println(i);
		}, false).doneBeforeJvmShutdown().start(5000);
		
		
		Thread.sleep(30000);
//		System.exit(0);
		
		t.done();
	}
	
}
