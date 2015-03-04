package overlayVideo;

import gateway.RTPGateway;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import overlayTransport.OverlayTransport;
import routingInterface.RoutingInterface;

import utils.Constants;
import utils.Debugger;


public class StreamManager{
	
	private RTPGateway RTPGw;
	private String id;
	private Stream streamStore;
	private String source;
	private String destination;
	private String nextHop;
	private int currentRequestId = 0;
	private Map<String, LinkedList<Long>> acks;
	private RRTimeout rrTo;
	private Timer timer;
	private Map<String, RTCPPPacket> lastRR;	// for rtx
	private LinkedList<Long> outQueue; 
	
//	private double lossRatio;
			
	/**
	 * 
	 * With Gateway
	 * 
	 * @param _id
	 * @param _session
	 * @param _rtp
	 * @param _rtcp
	 */
	public StreamManager(String _id, int _srtp, int _drtp)
	{
		RTPGw = new RTPGateway(_id, "localhost", _srtp, _drtp, this);
		init(_id);
	}
	
	/**
	 * 
	 * Without Gateway
	 * 
	 * @param _id
	 * @param _session
	 * @param _rtp
	 * @param _rtcp
	 */
	public StreamManager(String _id)
	{
		RTPGw = null;
		init(_id);
	}
	
	/**
	 * 
	 * Init main class variables
	 * 
	 * @param _id
	 */
	private void init(String _id)
	{
		id = _id;
		source = Constants.SERVER_ADDR;
		destination = Constants.CLIENT_ADDR; 
		nextHop = Constants.LOCALHOST_ADDR;
		
		acks = Collections.synchronizedMap(new HashMap<String, LinkedList<Long>>());
		lastRR = Collections.synchronizedMap(new HashMap<String, RTCPPPacket>());
		outQueue = new LinkedList<Long>();
		
		streamStore = new Stream(id, source, destination);
		rrTo = null;
		timer = new Timer();
	}

	
	/**
	 * 
	 * Send stream to nextHop
	 * 
	 * @param _nh
	 */
	public void play(String _nh)
	{
		
		Debugger.dumpMsg(this, "Play stream to "+_nh, Debugger.RTP);

		// Play with a different nextHop
		if(!_nh.equals(nextHop))
		{
			nextHop = _nh;
			if(streamStore.getNumberOfPacketsStored() > 0)	// if any packets to send
				sendSR(_nh, currentRequestId++);
		}

	}

	
	/**
	 * Stop sending stream
	 */
	public void stop()
	{
		Debugger.dumpMsg(this, "Stop stream to "+nextHop, Debugger.RTP);
		nextHop = Constants.MY_ADDR;
		cancelRRTimeout(); 
	}
	
	/**
	 * 
	 * Receive RTP+ packets from GW
	 * 
	 * @param _rtp
	 */
	public void receive(RTPPPacket _rtp)
	{
		streamStore.addPacket(_rtp);
			//Debugger.dumpMsg(this, "Received packet from gateway: "+_rtp.getSequenceNumber(), Debugger.RTP);
	}
	
	public void receive(LinkedList<RTPPPacket> _packets)
	{
		// Do not send while getting from GW
		synchronized(outQueue)
		{
			Iterator<RTPPPacket> it = _packets.iterator();
			while(it.hasNext())
			{
				receive(it.next());
			}
		}
	}
	/**
	 * 
	 * Receive RTP+ packets.
	 * If the client, forward them
	 * Otherwise, store them and add the packet to the ACK list
	 * 
	 * @param _sender
	 * @param _rtp
	 */
	public void receive(String _sender, RTPPPacket _rtp)
	{
		ackPacket(_sender, _rtp.getSequenceNumber());
		if(streamStore.isPacket(_rtp.getSequenceNumber()))
		{
			Debugger.dumpMsg(this, "Duplicated RTP packet "+_rtp.getSequenceNumber()+" received from: "+_sender, Debugger.RTP);
		}
		else
		{
			streamStore.addPacket(_rtp);

			// Am I destination?
			if(destination.equals(Constants.MY_ADDR)) // yes
			{
				RTPGw.send(_rtp.getRTPPacket());
				Debugger.dumpMsg(this, "Forwarded RTP packet to gateway: "+_rtp.getSequenceNumber()+" received from: "+_sender, Debugger.RTP);
			}
			else // no
			{
				Debugger.dumpMsg(this, "Storing RTP packet: "+_rtp.getSequenceNumber()+" received from: "+_sender, Debugger.RTP);
			
				// start forwarding
				if(streamStore.getNumberOfPacketsStored() == 1 && !nextHop.equals(Constants.LOCALHOST_ADDR))	// if the first packet received, then start forwarding...
				{
					sendSR(nextHop, currentRequestId++);
				}
			}
		}
	}
	
	/**
	 * 
	 * Receive RTCP+ packets.
	 * 
	 * If RR, remove acked packets and go on streaming
	 * If SR, build RR and answer
	 * 
	 * @param _sender
	 * @param _rtcp
	 */
	public void receive(String _sender, RTCPPPacket _rtcp)
	{
		if(_rtcp.getType() == Constants.MT_RTCP_RR)		// RR received
		{
			Debugger.dumpMsg(this, "Received RTCP RR "+_rtcp.getRequestId()+"/"+rrTo.requestId+" from: "+_sender+"/"+nextHop+" ACK "+_rtcp.getACK().length, Debugger.RTP);
			
			// check ACKs
			long[] llacks = _rtcp.getACK();
			// remove ACKed packets
			for (int i = 0; i < llacks.length; i++) {
				if(streamStore.isPacket(llacks[i]))
				{
					synchronized(outQueue)
					{
						streamStore.removePacket(llacks[i]);
						outQueue.remove(llacks[i]);
						
						Debugger.dumpMsg(this, "Removing ACKed Packet:"+llacks[i]+" from "+_sender, Debugger.RTP);
					}
				}
			}
			
			if(_rtcp.getRequestId() == rrTo.requestId && _sender.equals(nextHop))	// is the expected RR (it could be a previous one, or from a different node)
			{
				cancelRRTimeout();
				sendPackets(nextHop);
			}
		}
		else if(_rtcp.getType() == Constants.MT_RTCP_SR)  // SR received
		{
			Debugger.dumpMsg(this, "Received RTCP SR from: "+_sender, Debugger.RTP);

			if(lastRR.containsKey(_sender) && _rtcp.getRequestId() == lastRR.get(_sender).getRequestId()) // there was RR and it was lost
			{
					// rtx RR
					OverlayTransport.instance().send(Constants.MT_RTCP, lastRR.get(_sender).getMessage(), _sender);
					Debugger.dumpMsg(this, "Retransmitting RTCP RR "+_rtcp.getRequestId()+" to: "+_sender+" ACK "+lastRR.get(_sender).getACK().length, Debugger.RTP);

			}
			else	// otherwise
			{
				// new RR
				sendRR(_rtcp.getRequestId(),_sender);
			}
		}
	}
	
	
	/**
	 * Send packets, implements ordering FIFO, at the end we send an SR
	 */
	private void sendPackets(String _nh)
	{
		synchronized(outQueue)
		{
			int sentNumber = 0;
			// fill outQueue
			if(outQueue.size() < Constants.RTP_CREDIT_MAX)
			{
				/***
				 * This line can return any number of packets...
				 * 	 - 50 or less if
				 * 	 - more than 50 when there are all packets from the same frame
				 */
				int pleft = Constants.RTP_CREDIT_MAX-outQueue.size();
//				int qSizeb = outQueue.size();
//				long begin = System.currentTimeMillis();
				outQueue.addAll(streamStore.schedulePackets(pleft, Constants.RTP_ORDERING_MODE));
//				long end = System.currentTimeMillis();
//				int qSizee = outQueue.size();
//				Debugger.dumpMsg(this, "Schedule "+pleft+"/"+(qSizee-qSizeb)+" packets time: "+(end-begin), Debugger.RTP);
			}
				
			
			Iterator<Long> iterator = outQueue.iterator();
			while(iterator.hasNext() && sentNumber < Constants.RTP_CREDIT_MAX)
			{
				Long p = (Long) iterator.next();
				if(send(_nh, p))   // successfully sent
				{
					sentNumber++;
				}
			}
				// select packets from the stream
				// for those not already sent
		
			Debugger.dumpMsg(this, "Sent "+sentNumber+"/"+outQueue.size()+"/"+streamStore.getNumberOfPacketsStored()+"/"+streamStore.getNumberOfPacketsToSchedule()+" packets to "+_nh, Debugger.RTP);
			
			if(sentNumber > 0)
			{
				// send SR
				sendSR(_nh, currentRequestId++);
			}
		}
	}
	
	/**
	 * 
	 * Send one packet
	 * 
	 * @param _sn
	 * @return
	 */
	private boolean send(String _ip, long _sn)
	{
		if(streamStore.isPacket(_sn))
		{
			if(!nextHop.equals(Constants.LOCALHOST_ADDR))
			{
			OverlayTransport.instance().send(Constants.MT_RTP, streamStore.getPacket(_sn).getMessage(), _ip);
			Debugger.dumpMsg(this, "Sending RTP packet "+_sn+" to: "+nextHop, Debugger.RTP);

			return true;
			}
			else
			{
				return false;
			}
		}
		else
		{
			Debugger.dumpMsg(this, "Error: The packet "+_sn+" was in the outQueue but not in the store!!", Debugger.RTP);
			
			// Fix: Dude! CANNOT touch the queue, I'm iterating up there!
			//outQueue.remove(_sn);
			return false;
		}
	}
	

	
	/**
	 * 
	 * Send a SR with a given requestId
	 * 
	 * @param _requestId
	 */
	private void sendSR(String _ip, int _requestId)
	{
			if(		!_ip.equals(Constants.MY_ADDR)  &&
					!_ip.equals(Constants.LOCALHOST_ADDR) &&
					RoutingInterface.instance().isConnected(_ip))
			{
				RTCPPPacket sr = new RTCPPPacket(Constants.MT_RTCP_SR, this.id, _requestId);
				OverlayTransport.instance().send(Constants.MT_RTCP, sr.getMessage(), _ip);
				setRRTimeout(_ip, _requestId);
				Debugger.dumpMsg(this, "Sending RTCP SR "+_requestId+" to: "+_ip, Debugger.RTP);
			}	
			else
			{
				stop();
			}
	}
	
	/**
	 * 
	 * Send a RR
	 *  
	 * @param _SRrequestId
	 * @param _sender
	 */
	private void sendRR(int _SRrequestId, String _sender)
	{
		if(!_sender.equals(Constants.MY_ADDR) && !_sender.equals(Constants.LOCALHOST_ADDR)) // never to myself
		{
			RTCPPPacket rr = new RTCPPPacket(Constants.MT_RTCP_RR, this.id, _SRrequestId);
			int ackSize = 0;
			synchronized (acks) {
				// Add ACKs if any
				if(acks.containsKey(_sender))
				{
					LinkedList<Long> llacks = acks.remove(_sender);
					ackSize = llacks.size();
					for(Iterator<Long> i = llacks.iterator(); i.hasNext();)
					{
						rr.addACK(i.next());
					}
				}
			}
			lastRR.put(_sender, rr);
			// send
			OverlayTransport.instance().send(Constants.MT_RTCP, rr.getMessage(), _sender);
			Debugger.dumpMsg(this, "Sending RTCP RR "+_SRrequestId+" to: "+_sender+" ACK "+ackSize, Debugger.RTP);
		}
	}
	
	/**
	 * 
	 * Add packet sequence number to ACK list...
	 * @param _sender
	 * @param _sn
	 */
	private void ackPacket(String _sender, long _sn)
	{
		// ACK packet
		synchronized(acks)
		{
			if(!acks.containsKey(_sender))
			{
				acks.put(_sender, new LinkedList<Long>());
			}
			acks.get(_sender).add(_sn);
		}
	}
	
	/**
	 * Set RR Timeout
	 */
	private void setRRTimeout(String _ip, int _requestId)
	{
		cancelRRTimeout();
		synchronized(timer)
		{
			rrTo = new RRTimeout(_ip, _requestId);
			timer = new Timer();
			timer.schedule(rrTo, Constants.RTCP_RR_Timeout);
		}
			
			
	}
	
	private void cancelRRTimeout()
	{
		try
		{
			synchronized(timer)
			{
				timer.cancel();
				if(rrTo != null)
				{
					rrTo.cancel();	
				}
			}
		}
		catch(Exception e)
		{
			Debugger.dumpErr(this, "Controled "+e.getMessage());
		}
		
	}
	
	/**
	 * 
	 * Retransmission of SR in case of RR Timeout
	 * 
	 * @author sergio
	 *
	 */
	class RRTimeout extends TimerTask
	{

		public int requestId;
		private String ip;
		
		private RRTimeout(String _ip, int _requestId)
		{
			ip = _ip;
			requestId = _requestId;
		}
		

		public void run() {
				// Send a SR with the same request Id
				sendSR(ip, requestId);
		}
	}

}
