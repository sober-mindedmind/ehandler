package com.mindedmind.ehandler.channel;

import static com.mindedmind.ehandler.channel.Utils.sleep;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.mindedmind.ehandler.handler.Handler;
import com.mindedmind.ehandler.queue.AsyncPriorityEventQueue;
import com.mindedmind.ehandler.queue.JoinQueue;

public class TypedChannelTest
{
	private TypedChannel channel; 
	
	@Before 
	public void setUp() throws Exception
	{
		channel = new TypedChannel();
		channel.setQueue(new JoinQueue());
		AnnotationTestHandler handler = new AnnotationTestHandler();
		SimpleHandler simpleHandler = new SimpleHandler();
		channel.addHandler(handler, "ann1");
		channel.addHandler(simpleHandler, "simple");
	}

	@After
	public void shutdown()
	{
		channel.close();
	}
	
	@Test 
	public void sendMessage_HandlerWithInterfaceParamExecuted_True()
	{
		Trigger trigger = Mockito.mock(Trigger.class);
		channel.sendMessage(trigger, "ann");
		channel.sendMessage(trigger, "ann1");
		Mockito.verify(trigger, Mockito.times(2)).call();
	}

	@Test 
	public void sendMessage_CorrectExecuteMethodIsFound_True()
	{
		Trigger trigger = Mockito.mock(Trigger.class);		
		channel.sendMessage(trigger, "simple");		
		Mockito.verify(trigger, Mockito.times(1)).call();
	}
	
	@Test 
	public void sendMessage_WrongExecuteMethodIsNotExecuted_True()
	{		
		channel.sendMessage("fail", "simple");
	}
	
	@Test 
	public void sendMessage_TasksExecuteInPriorityOrder_True() throws InterruptedException
	{			
		Trigger triggerWait = () -> sleep(100);
		AtomicInteger cnt = new AtomicInteger(0);		
		AtomicBoolean failed = new AtomicBoolean(false); 
		channel.setQueue(AsyncPriorityEventQueue.newSyncQueue());
		channel.sendMessage(triggerWait, "simple");				
		for (int i = 0; i < 10; i++)
		{
			Trigger lowPriorityTrigger0 = () -> {
				if (cnt.get() < 10) 
				{
					failed.getAndSet(true);
				}
				cnt.incrementAndGet();
			};
			Trigger highPriorityTrigger1 = () -> {
				if (cnt.get() > 10) 
				{
					failed.getAndSet(true);
				}
				cnt.incrementAndGet();
			};
			channel.sendMessage(lowPriorityTrigger0, 0, "simple");
			channel.sendMessage(highPriorityTrigger1, 1, "simple");
		}	
		sleep(100);
		if (failed.get())
		{
			fail();
		}		
	}
	
	@Test
	public void addHandler_HandlersExecutedInPriorityOrder_True()
	{
		AtomicInteger integer = new AtomicInteger(2);		
		class PriorityHandler implements Handler<Integer>
		{
			private int priority;
			
			PriorityHandler(int priority)
			{
				this.priority = priority;
			}
			
			@Override public void execute(Integer payload)
			{
				assertEquals(integer.getAndDecrement() , priority);
			}
			
			@Override public int getPriority()
			{
				return priority;
			}			
		}
		
		channel.addHandler(new PriorityHandler(0) , "ph");
		channel.addHandler(new PriorityHandler(1) , "ph");
		channel.addHandler(new PriorityHandler(2) , "ph");
		
		channel.sendMessage(1 , "ph");
	}
	
	@Test
	public void releaseTypeArgument_InGenericType_DiamondsAreRemoved()
	{
		String type = TypedChannel.releaseTypeArgument(new SimpleHandler().getClass().getGenericInterfaces()[0]);
		assertEquals(Handler.class.getTypeName() , type);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void addHandler_TypeArgumentDoesNotExist_ExceptionThrown()
	{
		class NoTypeArgumentHandler implements Handler
		{
			@Override public void execute(Object payload){}			
		}		
		channel.addHandler(new NoTypeArgumentHandler());
	}
		
}
