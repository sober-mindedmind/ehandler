package com.mindedmind.ehandler.channel;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.mindedmind.ehandler.handler.Handler;
import com.mindedmind.ehandler.handler.MessageHandler;
import com.mindedmind.ehandler.queue.AsyncPriorityEventQueue;
import com.mindedmind.ehandler.queue.EventQueue;
import com.mindedmind.ehandler.queue.Priority;

public class TypedChannel implements Channel 
{	
	private final static String[] EMPTY_DESTINATIONS = {""}; 
	
	private EventQueue queue = new AsyncPriorityEventQueue();
	
	private Map<String, Map<Class<?>, Set<Handler<?>>>> handlers = new ConcurrentHashMap<>();
	
	private static class PriorityTask implements Runnable, Priority
	{
		private int priority;
		
		private Object msg;
		
		@SuppressWarnings("rawtypes")
		private Handler handler;
		
		PriorityTask(Handler<?> handler, Object msg, int priority)
		{			
			this.handler = handler;
			this.msg = msg;
			this.priority = priority;
		}

		@SuppressWarnings("unchecked")
		@Override public void run()
		{
			handler.execute(msg);
		}
		
		@Override public int getPriority()
		{
			return priority;
		}		
	}
	
	@Override public void setQueue(EventQueue queue)
	{
		this.queue = queue;
	}
		
	@Override public void addHandler(Handler<?> handler, String... destinations)
	{		
		if (handler == null)
		{
			throw new NullPointerException("Handler can't be null");
		}
		for (Type type : handler.getClass().getGenericInterfaces())
		{
			String concreteType = releaseTypeArgument(type);
			if (concreteType.equals(Handler.class.getTypeName()))
			{
				if (!hasTypeArgument(type))
				{
					throw new IllegalArgumentException("Handler must have type argument, for example Handler<String>");
				}
				
				ParameterizedType parameterizedType = (ParameterizedType) type;
				Type msgTypeArg = parameterizedType.getActualTypeArguments()[0];
				if (msgTypeArg instanceof Class<?>)
				{
					putHandler((Class<?>) msgTypeArg , handler , destinations);
				}
				else
				{
					/* try to resolve Class of the message */
					try
					{
						putHandler(Class.forName(releaseTypeArgument(msgTypeArg)) , handler , destinations);
					}
					catch (ClassNotFoundException ex)
					{
						throw new ChannelException(ex);
					}
				}
			}
		}
	}
		
	protected final void putHandler(Class<?> eventClass, Handler<?> handler, String... destinations)
	{
		assert eventClass != null && handler != null;
		for (String dest : getDest(destinations))
		{
			handlers.computeIfAbsent(dest , (e) -> new ConcurrentHashMap<>())
				.computeIfAbsent(eventClass , (e) -> Collections.newSetFromMap(new ConcurrentHashMap<>()))
				.add(handler);
		}
	}
	
	@Override public void addHandler(Object handler, String... destinations)
	{
		Method[] methods = handler.getClass().getDeclaredMethods();
		if (methods == null)
		{
			throw new IllegalArgumentException("Handler does not contain any methods");
		}
		boolean oneHandlerWasFound = false;
		for (Method method : methods)
		{
			oneHandlerWasFound |= parseMessageHandlerAnn(method, handler, destinations);
		}		
		if (!oneHandlerWasFound)
		{
			throw new IllegalArgumentException(String.format("Class '%s' does not contain any method annotated with "
					+ "MessageHandler annotation ", handler.getClass().getTypeName()));
		}
	}
				
	@Override public void close()
	{
		queue.close();
	}

	@Override public void sendMessage(Object msg, String... destinations)
	{		
		sendMessage(msg, 0, destinations);
	}
	
	@Override public void sendMessage(Object msg, int priority, String... destinations)
	{
		for (String dest : getDest(destinations))
		{
			Collection<Handler<?>> handlersSet = findHandlersInDest(msg.getClass(), dest);								
			if (handlersSet != null)
			{				
				if (isPriority(handlersSet))
				{
					/* sort handlers in a priority order */
					List<Handler<?>> copyOfHandlers = new ArrayList<>(handlersSet);					
					Collections.sort(copyOfHandlers);					
					handlersSet = copyOfHandlers;
				}	
				for (Handler<?> handler : handlersSet)
				{
					queue.enqueue(new PriorityTask(handler, msg, priority)); 
				}
			}			
		}
	}	
	
	private Collection<Handler<?>> findHandlersInDest(Class<?> clazz, String dest)
	{
		/* first try to find handlers without traversing the hierarchy */
		Map<Class<?>, Set<Handler<?>>> classHandlers = handlers.get(dest);
		if (classHandlers == null)
		{
			return Collections.emptyList();
		}		
		Set<Handler<?>> handlersSet = classHandlers.get(clazz);
		if (handlersSet != null)
		{
			return handlersSet;
		}
		
		/*
		 * second try to find handlers by traversing the hierarchy of the class that presented by the current message
		 * type
		 */
		Set<Handler<?>> handlersForClass = new HashSet<>();
		for (Class<?> nextSuperClass = clazz; nextSuperClass != null;)
		{
			Set<Handler<?>> superHandlersSet = classHandlers.get(nextSuperClass);
			if (superHandlersSet != null)
			{
				handlersForClass.addAll(superHandlersSet);			
			}			
			nextSuperClass = nextSuperClass.getSuperclass();
		}
		traversInterfaceHierarchy(clazz, handlersForClass, classHandlers);
		
		/*
		 * associate the current class of the message with handlers of its super classes and super interfaces, this
		 * prevent next call of this method (with the same class argument) to traverse the whole hierarchy
		 */
		for (Handler<?> handler : handlersForClass)
		{
			putHandler(clazz , handler , dest);
		}
		
		return handlersForClass;
	}
	
	private void traversInterfaceHierarchy(Class<?> clazz, 
								   		   Collection<Handler<?>> handlers, 
								   		   Map<Class<?>, Set<Handler<?>>> handlersInDest)
	{
		Class<?>[] interfaces = clazz.getInterfaces();
		if (interfaces != null)
		{
			for (Class<?> superInterface : interfaces)
			{
				Set<Handler<?>> handlersForClass = handlersInDest.get(superInterface);				
				if (handlersForClass != null)
				{					
					handlers.addAll(handlersForClass);
				}				
				traversInterfaceHierarchy(superInterface, handlers, handlersInDest);				
			}						
		}
	}
	
	protected boolean parseMessageHandlerAnn(Method method, Object handler, String... additionalDestinations)
	{
		MessageHandler msgHandlerAnn = method.getAnnotation(MessageHandler.class);		
		if (msgHandlerAnn != null)
		{
			Parameter[] params = method.getParameters();
			if (params == null || params.length != 1)
			{
				throw new IllegalArgumentException(String.format("Illegal handler method. Method '%s' annotated"
						+ " with @MessageHandler must contain exactly one parameter" , 
						handler.getClass().getTypeName() + "#" + method.getName()));
			}
			method.setAccessible(true);			
			Class<?> msgType = params[0].getType();
			Handler<Object> typedHandler = new Handler<Object>()
			{
				@Override public void execute(Object payload)
				{
					try
					{
						method.invoke(handler , payload);
					}
					catch (Exception ex)
					{
						throw new ChannelException(ex);
					}
				}
				@Override public int getPriority()
				{
					return msgHandlerAnn.priority();
				}				
			};
			putHandler(msgType, typedHandler, msgHandlerAnn.value());			
			if (additionalDestinations != null && additionalDestinations.length > 0)
			{
				putHandler(msgType, typedHandler, additionalDestinations);
			}								
			return true;
		}
		
		return false;
	}
		
	private static boolean isPriority(Collection<Handler<?>> handlers)
	{
		boolean found = false;
		int first = 0;		
		for (Handler<?> handler : handlers)
		{
			if (!found)
			{
				first = handler.getPriority();
				found = true;
				continue;
			}
			
			if (handler.getPriority() != first)
			{
				return true;
			}			
		}
		return false;
	}
	
	private static String[] getDest(String... destinations)
	{
		return destinations == null || destinations.length == 0 ? EMPTY_DESTINATIONS : destinations;
	}
	
	private static boolean hasTypeArgument(Type t)
	{
		return t.getTypeName().lastIndexOf('>') > 0;
	}
	
	static String releaseTypeArgument(Type t)
	{		
		String concreteType = t.getTypeName();
		return hasTypeArgument(t) ? concreteType.substring(0 , concreteType.indexOf('<'))  
								  : concreteType;
	}
}
