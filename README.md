Ehandler is an implementation of (some kind of) an event listener where producers produce messages (of some type) and consumers (i.e. handlers) consume these messages. 
All consumers are typed handlers i.e. each consumer can handle only messages of specific type(s). For example: if message is of type String then this message will
 be handled only by handlers that subscribed on this type or by handlers that subscribed on super class or super interface(s) of that type. By default all messages are
 handled in threads that are different from the thread that sent a message and .from the thread that specified a handler. 
 
Example of code based on annotations:
 
Initialization:
```
ContextHolder.getContext().addChannel("ch1", new TypedChannel());				
 ....
 ```

Register message handler that will handle messages of type CharSequence. In this case class can contain any 
amount of message handlers:
```
class CharSequenceHandler
{
	@MessageHandler
	void charSequenceHandler(CharSequence payload)
	{
		System.out.println(payload);
	}
}
Channel ch1 = ContextHolder.getContext().findChannel("ch1");
ch1.addHandler(new CharSequenceHandler());
... 
```

Send message of type String which implements CharSequence:
```
ContextHolder.getContext().findChannel("ch1").sendMessage("Hello");
```
"Hello" is printed to the output stream after execution of the sendMessage method.
   
And example of  the same functionality but without annotations:
 
Initialization:
```
ContextHolder.getContext().addChannel("ch1", new TypedChannel());		
 ....
 ```
Register message handler that will handle messages of type CharSequence or any subtype of this type. Can handle only one type of message:
```
Channel ch1 = ContextHolder.getContext().findChannel("ch1");
ch1.addHandler(new Handler<CharSequence>() {
	@Override public void execute(CharSequence payload)
	{
		System.out.println(payload);
	}});
 ... 
```

Asynchronously send message of type String (which implements CharSequence) :
```
ContextHolder.getContext().findChannel("ch1").sendMessage("Hello");
```