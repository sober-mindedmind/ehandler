package com.mindedmind.ehandler.channel;

public class ChannelException extends RuntimeException
{
	public ChannelException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public ChannelException(String message)
	{
		super(message);
	}

	public ChannelException(Throwable cause)
	{
		super(cause);
	}
}
