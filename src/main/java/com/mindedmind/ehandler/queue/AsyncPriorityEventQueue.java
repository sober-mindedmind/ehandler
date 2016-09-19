package com.mindedmind.ehandler.queue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class AsyncPriorityEventQueue implements EventQueue
{
	private ExecutorService execService;
	
	private AsyncPriorityEventQueue(ExecutorService execService)
	{
		this.execService = execService;
	}
	
	public AsyncPriorityEventQueue(int maxThreads)
	{
		execService = newPriorityThreadPool(maxThreads);	
	}
	
	public AsyncPriorityEventQueue()
	{
		this(Runtime.getRuntime().availableProcessors());
	}
		
	@Override public <T extends Runnable & Priority> void enqueue(T task)
	{
		execService.submit(task);
	}
	
	@Override public void close()
	{
		execService.shutdown();
	}
	
	public static EventQueue newSyncQueue()
	{
		return new AsyncPriorityEventQueue(newPriorityThreadPool(1));
	}

	static ThreadPoolExecutor newPriorityThreadPool(int maxThreads)
	{
		return new PriorityThreadPool(maxThreads,
				 					  maxThreads,
                					  0L,
                					  TimeUnit.MILLISECONDS,
                					  new PriorityBlockingQueue<Runnable>());
	}
	
	protected static class PriorityThreadPool extends ThreadPoolExecutor
	{
		PriorityThreadPool(int corePoolSize,
						   int maximumPoolSize,
						   long keepAliveTime,
						   TimeUnit unit,
						   BlockingQueue<Runnable> workQueue)	
		{
			super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
		}

		@Override protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value)
		{
			assert runnable instanceof Comparable<?>; 
			
			/* our implementation of runnable always implements Comparable, we control this in 'enqueue' method */
			return new PriorityRunnableFuture(runnable);
		}		
	}
	
	protected static class PriorityRunnableFuture<T extends Runnable & Priority>	
			extends FutureTask<T>
			implements Comparable<PriorityRunnableFuture<T>>
	{
		private T task;
		
		public PriorityRunnableFuture(T task)
		{
			super(task, null);
			this.task = task;
		}

		@Override public int compareTo(PriorityRunnableFuture<T> o)
		{
			return task.compareTo(o.task);
		}		
	}
 
}
