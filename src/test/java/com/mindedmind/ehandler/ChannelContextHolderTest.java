package com.mindedmind.ehandler;

import static org.junit.Assert.assertNotNull;

import org.junit.BeforeClass;
import org.junit.Test;

import com.mindedmind.ehandler.ContextHolder;
import com.mindedmind.ehandler.channel.AnnotationTestHandler;
import com.mindedmind.ehandler.channel.TypedChannel;

public class ChannelContextHolderTest
{
	@Test 
	public void findChannel_ChannelExistst_True()
	{
		TypedChannel channel = new TypedChannel();
		channel.addHandler(new AnnotationTestHandler());
		ContextHolder.getContext().addChannel("ch" , channel);
		assertNotNull(ContextHolder.getContext().findChannel("ch"));
	}
}
