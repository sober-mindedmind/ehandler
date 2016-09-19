package com.mindedmind.ehandler.channel;

import java.io.Closeable;

import com.mindedmind.ehandler.handler.Handler;
import com.mindedmind.ehandler.queue.EventQueue;

/**
 * Maps message handler on concrete message (event) that this handler can execute in runtime. Channel can be accessed
 * by many threads at one time.
 */
public interface Channel extends Closeable
{
	void setQueue(EventQueue queue);
	
	/**
	 * Sends message to the given destination(s). If destination contains handlers that can handle this type of message
	 * or contains handlers that can handle any super class or super interface of this message then such handlers are
	 * executed. Message will be executed with default priority which is the lowest priority.
	 * 
	 * @param msg - concrete message of any type
	 * @param destinations - the destination to which this message will be sent, can be {@code null}
	 */
	void sendMessage(Object msg, String... destinations);
	
	void sendMessage(Object msg, int priority, String... destinations);
		
	/**
	 * Registers handler in the given destination(s). The actual type argument of the given handler is the type of the
	 * messages that this handler can handle. Also this handler can handle any subtype of the given type argument.
	 * 
	 * @param handler - the concrete handler implementation
	 * @param destinations - the destination(s) in which this handler registers, can be {@code null}
	 */
	void addHandler(Handler<?> handler, String... destinations);
		
	/**
	 * Registers annotation driven handler.
	 * 
	 * @param handler
	 * @param destinations
	 */
	void addHandler(Object handler, String... destinations);
			
	@Override void close();

}
