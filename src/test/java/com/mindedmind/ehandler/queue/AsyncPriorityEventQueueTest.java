package com.mindedmind.ehandler.queue;

import java.util.concurrent.RejectedExecutionException;

import org.junit.Test;

import com.mindedmind.ehandler.queue.AsyncPriorityEventQueue;

public class AsyncPriorityEventQueueTest
{

	@Test(expected = RejectedExecutionException.class) 
	public void close_CanNotAddTaskToClosedQueue_ExceptionThrown()
	{
		AsyncPriorityEventQueue eventQueue = new AsyncPriorityEventQueue();		
		eventQueue.enqueue(() -> {});
		eventQueue.close();
		eventQueue.enqueue(() -> {});		
	}
	
}
