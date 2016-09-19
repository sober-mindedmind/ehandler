package com.mindedmind.ehandler.queue;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;

public class BoundedAsyncPriorityEventQueue extends AsyncPriorityEventQueue
{
	private Semaphore semaphore;
	
	public BoundedAsyncPriorityEventQueue(int queueSize)
	{
		this(queueSize, Runtime.getRuntime().availableProcessors());
	}

	public BoundedAsyncPriorityEventQueue(int queueSize, int maxThreads)
	{
		super(maxThreads);
		if (queueSize < 0)
		{
			throw new IllegalArgumentException("Queue size should be >= 0");
		}
		semaphore = new Semaphore(queueSize);
	}
	
	@Override public <T extends Runnable & Priority> void enqueue(final T task)
	{
		class RunnablePriorityTask implements Runnable, Priority
		{
			@Override public int compareTo(Priority o)
			{
				return task.compareTo(o);
			}
			@Override public void run()
			{
				try 
				{
					task.run();
				}
				finally
				{
					semaphore.release();
				}
			}
		}		
		try
		{
			try
			{
				semaphore.acquire();
			}
			catch (InterruptedException e)
			{
			}
			super.enqueue(new RunnablePriorityTask());
		}
		catch (RejectedExecutionException e)
		{
			semaphore.release();			
		}		
	}
	
}
