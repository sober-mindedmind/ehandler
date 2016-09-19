package com.mindedmind.ehandler.queue;

public class JoinQueue implements EventQueue
{

	@Override public <T extends Runnable & Priority> void enqueue(T task)
	{
		task.run();
	}

	@Override public void close()
	{
	}

}
