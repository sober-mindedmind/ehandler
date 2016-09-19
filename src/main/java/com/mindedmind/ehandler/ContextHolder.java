package com.mindedmind.ehandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.mindedmind.ehandler.channel.Channel;

/**
 * Keeps association between channel and his name and allows to different parts of a program have access to channels or
 * register them.
 */
public final class ContextHolder
{
	private static final ContextHolder context = new ContextHolder();

	private Map<String, Channel> channels = new ConcurrentHashMap<>(); 
	
	private ContextHolder() {}
	
	public Channel findChannel(String alias)
	{
		return channels.get(alias);
	}
	
	public void addChannel(String alias, Channel channel)
	{
		channels.put(alias, channel);
	}
	
	public static ContextHolder getContext()
	{
		return context;
	}
}
