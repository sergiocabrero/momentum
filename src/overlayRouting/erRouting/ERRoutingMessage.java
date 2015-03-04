package overlayRouting.erRouting;

import overlayTransport.OTMessage;
//import overlayTransport.store.MessageStore;
import routingInterface.RoutingInterface;
import utils.Constants;

public class ERRoutingMessage {
	private static String headerFieldSeparator = "\n";
	private static int ids = 0;
	private int requestID;
	private String source;
	private String destination;

	private int nodeType;
	private long lastEncounter;
//	private int memory;

	
	// Generated Requests
	protected ERRoutingMessage(String _destination)
	{
		source = Constants.MY_ADDR;
		requestID = ids++;
		destination = _destination;
		nodeType = Constants.NODE_TYPE;
		lastEncounter = RoutingInterface.instance().milliSecondsSinceLastEncounter(_destination);
	}
	
	// Generated Responses
	protected ERRoutingMessage(int _requestID, String _destination)
	{
		source = Constants.MY_ADDR;
		requestID = _requestID;
		destination = _destination;
		nodeType = Constants.NODE_TYPE;
		lastEncounter = RoutingInterface.instance().milliSecondsSinceLastEncounter(_destination);
//		memory = MessageStore.instance().getFreeBuffer();
	}

	// Received requests and responses
	protected ERRoutingMessage(String _source, byte[] _message)
	{
		source = _source;
		
		String message = new String(_message);
		String[] fields = message.split(headerFieldSeparator);
		requestID  = Integer.parseInt(fields[0]);
		destination = fields[1];
		nodeType = Integer.parseInt(fields[2]);
		lastEncounter = Long.parseLong(fields[3]);
//		memory = Integer.parseInt(fields[4]);
	}

	public byte[] getMessageBytes()
	{
		String message ="";
		message += requestID+headerFieldSeparator;
		message += destination+headerFieldSeparator;
		message += nodeType+headerFieldSeparator;
		message += lastEncounter+headerFieldSeparator;

		return message.getBytes();
		
	}
	public int getRequestID() {
		return requestID;
	}


	public String getDestination() {
		return destination;
	}

	public int getNodeType() {
		return nodeType;
	}
	
	public long getLastEncounter()
	{
		return lastEncounter;
		
	}
	/*
	public int getMemory()
	{
		return memory;
		
	}*/
	
	public String getSource()
	{
		return source;
		
	}
}
