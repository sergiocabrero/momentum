package overlayVideo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import utils.ExceptionManager;
import utils.Functions;


public class RTPPPacket{

	private String streamId;
	private long sequenceNumber;
	private int priority = 0;		// packet priority
	private long frameSN;		// frames contained in this packet...
//	private long frameStartSN;	// sn of the packet where the frame started
//	private int numberOfPacketsOfFrame;	// number of packets with the frame
	private byte[] RTPpacket;	
	private long GOP;
	
	
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
//	public RTPPPacket(String _streamId, long _sequenceNumber, long _GOP, long[] _framesSN, long _frameStartSN, int _priority, byte[] _payload){
//		this.streamId = _streamId;
//		this.sequenceNumber = _sequenceNumber;
//		this.framesSN = _framesSN;
//		this.frameStartSN = _frameStartSN;
//		this.RTPpacket = _payload;
//		this.priority = _priority;	
//		this.GOP = _GOP;
////		this.numberOfPacketsOfFrame = 1; // default, this must be changed by GW
//	}
	
	public RTPPPacket(String _streamId, long _sequenceNumber, long _GOP, long _frameSN, int _priority, byte[] _payload){
		this.streamId = _streamId;
		this.sequenceNumber = _sequenceNumber;
		this.frameSN = _frameSN;
//		this.frameStartSN = _frameStartSN;
		this.RTPpacket = _payload;
		this.priority = _priority;	
		this.GOP = _GOP;
//		this.numberOfPacketsOfFrame = 1; // default, this must be changed by GW
	}
	
	/**
	 * 
	 * Constructor from raw bytes (receive)
	 * 
	 * @param _data
	 */
	public RTPPPacket(byte[] _data)
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
			baos.write(streamId.getBytes());
			baos.write(headerFieldSeparator.getBytes());
		
			baos.write(Long.toString(sequenceNumber).getBytes());
			baos.write(headerFieldSeparator.getBytes());
			
			baos.write(Integer.toString(priority).getBytes());
			baos.write(headerFieldSeparator.getBytes());
			
			baos.write(Long.toString(GOP).getBytes());
			baos.write(headerFieldSeparator.getBytes());
			
			baos.write(Long.toString(frameSN).getBytes());
			baos.write(headerFieldSeparator.getBytes());
			
			baos.write(RTPpacket);
	
		} catch (IOException e) {
			ExceptionManager.catchException(e, "rtpplus", "RTPPMessage", "getMessage");
		}
		
			return baos.toByteArray();
			
		
		
		
	}
	
	
	private void parseMessage(byte[] _message)
	{

		String message = new String(_message);
		
		String[] fields = message.split(headerFieldSeparator,6);
		streamId = fields[0];
		sequenceNumber = Long.parseLong(fields[1]);
		priority = Integer.parseInt(fields[2]);
		GOP = Long.parseLong(fields[3]);
		frameSN = Long.parseLong(fields[4]);
				
		int offset = fields[0].getBytes().length + fields[1].getBytes().length + fields[2].getBytes().length + fields[3].getBytes().length + fields[4].getBytes().length + 5*headerFieldSeparator.getBytes().length;
		RTPpacket = new byte[_message.length - offset];
		System.arraycopy(_message, offset, RTPpacket, 0, RTPpacket.length);
	}
	
	/*
	 * Auto-generated getters
	 * */
	public String getStreamId() {
		return streamId;
	}
	
	public long getSequenceNumber(){
		
		return sequenceNumber;
	}
	
	public long getGOP(){
		
		return GOP;
	}

	public int getPriority(){
		return priority;
	}
	


	public byte[] getRTPPacket() {
		return RTPpacket;
	}
	
	public void setPriority(int _p)
	{
		this.priority = _p;
	}

		
	public long getFrameSN() {
		return frameSN;
	}



//	public void setNumberOfPacketsOfFrame(int numberOfPacketsOfFrame) {
//		this.numberOfPacketsOfFrame = numberOfPacketsOfFrame;
//	}

//	public int getNumberOfPacketsOfFrame() {
//		return numberOfPacketsOfFrame;
//	}
	
	/**
	 * Gets the payload of the RTP packet
	 * 
	 * 
	 * @return
	 */
	public byte[] getRTPPayload()
	{
		// Calculate offset
		int CC = getCC();
		int byteoffset = 12 + CC*4;
		
		byte[] payload = new byte[RTPpacket.length - byteoffset];
		
		System.arraycopy(RTPpacket, byteoffset, payload, 0, payload.length);
		
		return payload;
	}
	
	/**
	 * Gets the Contributing Source Number
	 * 
	 * @return
	 */
	public int getCC()
	{
		byte ba;
		int CC;
		// First byte of packet
		ba = RTPpacket[0];
		// Shift to the right
		CC = ba>>0;
		
		// Get last 4 bits
		CC = (CC & 0x0F); 
		
		return CC;
		
	}
	
	/**
	 * 
	 * Gets the special Mark
	 * 
	 * @return
	 */
	public boolean getM()
	{
		byte ba;
		int M;
		// Second byte of packet
		ba = RTPpacket[1];
		// Shift to the right
		M = ba>>7;
		
		return (M==1);
	}
	
	//Funciones para la carga útil.
	
	public byte[] getTSPacket(int pos)
	{
		byte[] ba = new byte[188];
		System.arraycopy(this.getRTPPayload(), pos*188, ba, 0, 188);
		/*for(int i = 0; i < 188; i++){
			ba[i] = this.getPayload()[pos*188 + i];
		}*/
		
		return ba;
	}
	
	/**
	 * Gets the RTP Sequence Number
	 * 
	 * @return
	 */
	public int getRTPSequenceNumber()
	{
		byte[] ba = {RTPpacket[2], RTPpacket[3]};
		
		return Functions.unsignedShortToInt(ba);
	}
	
	/**
	 * 
	 * Gets the timestamp of the RTP Packet
	 * 
	 * @return
	 */
	public int getRTPTimeStamp()
	{
		byte[] ba = {RTPpacket[4], RTPpacket[5], RTPpacket[6], RTPpacket[7]};
		return Functions.byteArrayToInt(ba);
	}
	
}
