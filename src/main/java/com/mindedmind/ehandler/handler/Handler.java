package com.mindedmind.ehandler.handler;

import com.mindedmind.ehandler.queue.Priority;

/**
 * Represents an algorithm that will be executed only when a message (event) has type the same as the type argument
 * presented by this handler or if type argument is super class or super interface of a message type.
 */
public interface Handler<T> extends Priority
{		
	void execute(T payload);
}
