package com.mindedmind.ehandler.channel;

import com.mindedmind.ehandler.handler.Handler;

public class SimpleHandler  implements Handler<Trigger>
{

	@Override public void execute(Trigger trigger)
	{
		trigger.call();
	}
	
	public void execute(String string)
	{
		throw new AssertionError("This method should never be called");
	}
}
