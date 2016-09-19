package com.mindedmind.ehandler.channel;

public class Utils
{
	public static void sleep(int mil)
	{
		try
		{
			Thread.sleep(mil);
		}
		catch (Exception e)
		{
			throw new AssertionError(e); 
		}		
	}
}
