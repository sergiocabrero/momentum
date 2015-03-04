package overlayVideo;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import overlayTransport.OverlayTransport;


import utils.Constants;
import utils.Debugger;
import utils.MessageReceivedEventListener;

public class OverlayVideo implements MessageReceivedEventListener{

	static private OverlayVideo _instance = null; // Singleton
	private Map<String, StreamManager> streams;
	private int portCounter = 0;
	/**
	* @return The unique instance of this class.
	*/
	static synchronized public OverlayVideo instance() {
	      if(null == _instance) {
	         _instance = new OverlayVideo();
	      }
	      return _instance;
	   }
	
	/**
	 * Constructor
	 */
	private OverlayVideo()
	{
		streams = Collections.synchronizedMap(new HashMap<String, StreamManager>());
		OverlayTransport.instance().addEventListener(this);
		// a new server stream is created in the main function
		
	}


	public void receiveMessage(int _messageType, String _parentAddr, byte[] _message) {
	
		if(_messageType == Constants.MT_RTP)
		{
			RTPPPacket rtppmessage = new RTPPPacket(_message);
			String key = rtppmessage.getStreamId();
			if(streams.containsKey(key))
			{
				streams.get(key).receive(_parentAddr, rtppmessage);
			}
		}
		else if(_messageType == Constants.MT_RTCP)
		{
			RTCPPPacket rtcppmessage = new RTCPPPacket(_message);
			String key = rtcppmessage.getStreamId();

			if(streams.containsKey(key))
			{
				streams.get(key).receive(_parentAddr, rtcppmessage);
			}
			// Create new streams
//			else if(Constants.MY_ADDR.equals(Constants.CLIENT_ADDR))
//			{
//				newClientStream(key);
//				streams.get(key).receive(rtcppmessage);
//			} 
//			else
//			{
//				newStream(key);
//				streams.get(key).receive(rtcppmessage);
//			}
			
		}
//					System.out.println("Received RTP packet "+rtppmessage.getSequenceNumber()+" in Stream "+rtppmessage.getId()+"/"+Constants.MY_ADDR+" Gop:"+rtppmessage.getGoPSequenceNumber()+" "+rtppmessage.getMPEGFrameType());

					
}
			

	
	public StreamManager newStream(String _id)
	{
		if(!streams.containsKey(_id))
		{
			StreamManager stream;
			if(Constants.MY_ADDR.equals(Constants.SERVER_ADDR))  //server
			{
				portCounter++;
				Debugger.dumpMsg(this," creating new server stream "+_id, Debugger.RTP);
	
				stream = new StreamManager(_id, 65100+portCounter, 65000+portCounter);
				
			}
			else if(Constants.MY_ADDR.equals(Constants.CLIENT_ADDR)) //client
			{
				portCounter++;
				Debugger.dumpMsg(this," creating new client stream "+_id, Debugger.RTP);
				stream = new StreamManager(_id, 65200+portCounter, 65300+portCounter);

			}
			else // others
			{
				Debugger.dumpMsg(this," creating new stream "+_id, Debugger.RTP);
				stream = new StreamManager(_id);
			}
			streams.put(_id, stream);
			return stream;
		}
		else
		{
			return streams.get(_id);
		}
	}


	
	
//
//	public void close() {
//		alive = false;
//		
//		Iterator<String> myVeryOwnIterator = streams.keySet().iterator();
//		while(myVeryOwnIterator.hasNext()) {
//			String key = myVeryOwnIterator.next();
//			 streams.get(key).close();
//			 streams.remove(key);
//		}
//	
//	}

	
}
