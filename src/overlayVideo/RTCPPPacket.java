package overlayVideo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import utils.ExceptionManager;

public class RTCPPPacket{

	private int messageType;	
	private String streamId;//	stream
	// Packet
	private int requestId;    // Pair responses and requests
//	private int credit;	
//	private int ordering;	

	private LinkedList<Long> ACK;
	private static String headerFieldSeparator = "\n";
	

	/**
	 * 
	 * Constructor from parameters (send)
	 * 
	 * @param _session
	 * @param _id
	 * @param _protocol
	 * @param _payload
	 */
	public RTCPPPacket(int _messageType, String _stream, int _requestId){
		this.messageType = _messageType;
		this.streamId = _stream;
		this.requestId = _requestId;
		this.ACK=new LinkedList<Long>();
	}
	
	/**
	 * 
	 * Constructor from raw bytes (receive)
	 * 
	 * @param _data
	 */
	public RTCPPPacket(byte[] _data)
	{
		parseMessage(_data);
	}
	
	
	
	/**
	 * Gets the message in bytes
	 * 
	 * @return
	 */
	public byte[] getMessage()
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			baos.write(Integer.toString(messageType).getBytes());
			baos.write(headerFieldSeparator.getBytes());
			baos.write(streamId.getBytes());
			baos.write(headerFieldSeparator.getBytes());
			baos.write(Integer.toString(requestId).getBytes());
			baos.write(headerFieldSeparator.getBytes());

			// ACKs
			String acks = "";
			for (Iterator<Long> iterator = ACK.iterator(); iterator.hasNext();) {
				Long type = (Long) iterator.next();
				acks += type+",";
			}
			
			baos.write(acks.getBytes());
			
	
		} catch (IOException e) {
			ExceptionManager.catchException(e, "rtpplus", "RTPPMessage", "getMessage");
		}
		
			return baos.toByteArray();
			
		
		
		
	}
	
	
	private void parseMessage(byte[] _message)
	{

		String message = new String(_message);
		
		String[] fields = message.split(headerFieldSeparator,4);
		messageType = Integer.parseInt(fields[0]);
		streamId = fields[1];
		requestId = Integer.parseInt(fields[2]);
		/*
		 * get linked list from message
		 * */
		ACK = new LinkedList<Long>();
		
		if(fields.length > 3 && !fields[3].isEmpty())
		{		
			String[] acks = fields[3].split(",");
		
			for (int i = 0; i < acks.length; i++) {
				ACK.add(Long.parseLong(acks[i]));
			}
		}
	}
	
	/*
	 * Auto-generated getters
	 * */
	public int getType() {
		return messageType;
	}
	
//	public int getCredit() {
//		return credit;
//	}
	public int getRequestId() {
		return requestId;
	}
	
	public String getStreamId() {
		return streamId;
	}
	
	public void addACK(long _sn)
	{
		synchronized (ACK) {
			ACK.add(_sn);
		}
	}
	
	public long[] getACK(){
		long[] lACK = new long[ACK.size()];
		synchronized (ACK) {
			int i=0;
			for (Iterator<Long> iterator = ACK.iterator(); iterator.hasNext();) {
				lACK[i] = ((Long) iterator.next()).longValue();
				i++;
			}
			
		}
		return lACK;
	
	}
}
