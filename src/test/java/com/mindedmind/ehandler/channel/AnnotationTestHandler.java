package com.mindedmind.ehandler.channel;

import com.mindedmind.ehandler.handler.MessageHandler;

public class AnnotationTestHandler
{
	@MessageHandler("ann")
	void executeOwnInterface(Trigger trigger)
	{
		trigger.call();
	}

} 