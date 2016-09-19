package com.mindedmind.ehandler.queue;

import java.io.Closeable;
import java.io.Serializable;

public interface EventQueue extends Serializable, Closeable
{
	<T extends Runnable & Priority> void enqueue(T task);
	
	@Override void close();	
}
