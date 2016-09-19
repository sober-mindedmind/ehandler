package com.mindedmind.ehandler.queue;

public interface Priority extends Comparable<Priority> 
{
	int LOW_PRIORITY = 0;

	int HIGH_PRIORITY = Integer.MAX_VALUE;
		
	default int getPriority()
	{
		return LOW_PRIORITY;
	}
	
	@Override default int compareTo(Priority o)
	{
		return -Integer.compare(getPriority() , o.getPriority());
	}
}
