package com.mindedmind.ehandler.handler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface MessageHandler
{
	/**
	 * @return the names of the destinations paths which this handler will handle
	 */
	String[] value() default "";
	
	/** 
	 * @return the priority of this handler between other handlers
	 */
	int priority() default 0;
}
