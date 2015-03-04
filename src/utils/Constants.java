package utils;

import overlayVideo.Stream;


/**
 * Constants class stores constants and configuration parameters of MOMENTUM
 * 
 * @author		Sergio Cabrero
 * @version     %I%, %G%
 * @since       1.0
 */

public class Constants {
	public static final int DEFAULT_SERVICE_PORT = 3447;		// Default Port for OLSR plugin 
	public static final int ROUTINGPROT_PORT = 2005;			// Default port for OLSR plugin 
	
	public static final int MTU = 1500;							// Ethernet frame max payload (bytes)
	public static final int MTU_HEADER = 8;						// UDP Header (bytes)
	public static final int MTU_PAYLOAD = 1500-8;				// MTU - header (bytes)
	
	public static final long MPS = 5;							// Max bitrate (Mps)
	public static final long BPS = MPS*1000*1000;				// Max Bitrate (bps) 
	
	public static final long TEMPO = Math.round((1000*(8*MTU))/MPS); 	// Packet time (ns)

	public final static String	CRLF = "\r\n";
	public final static String	EOF = "\rEOF";
	/*
	 * IP Addresses
	 * 
	 * */
		public static final String BROADCAST_ADDR = "10.0.0.255"; // Broadcast IP
	public static String LOCALHOST_ADDR = "localhost";	// Localhost IP representation
	public static String MY_ADDR = "localhost";			// MY address IP representation

	public static String SERVER_ADDR = "10.0.0.1";		// IP address where the server app runs
	public static String CLIENT_ADDR = "10.0.0.2";		// IP where the client app runs
	
	

//	/*	OT Messages */
//	public static final int OTMESSAGE_HEADER_LENGTH = 12;
//	

	/*
	 * Message Types
	 * 
	 * */
	public final static int MT_MAX = 10;			
	public final static int MT_ACK = 1;
	public final static int MT_OVERLAY = 2;
	public final static int MT_CONTROL = 3;
	public final static int MT_RTP = 4;
	public static final int MT_RTCP = 5;
	
	public static final int MT_RTCP_SR = 51;
	public static final int MT_RTCP_RR = 52;

	public static final int MT_OVERLAY_ROUTING_REQUEST = 6;
	public static final int MT_OVERLAY_ROUTING_RESPONSE = 7;


	
		/*
		 *  FRAME Types
		 * */
		public static final int MPEG_FRAME_I = 3;
		public static final int MPEG_FRAME_P = 2;
		public static final int MPEG_FRAME_B = 1;
		public static final int MPEG_FRAME_UNKNOWN = 0; // default
		
		public static final int FPS_MIN = 10;
		public static final int FPS_SOURCE = 30;
			
	
//	/*
//	 * 	Priority
//	 * 
//	 * */
//	public final static int PRI_MAX = 16;		
//	public final static int PRI_ACK = PRI_MAX-1;
//	public final static int PRI_CONTROL = PRI_MAX-3;
//	public final static int PRI_RTPH = PRI_MAX-4;
//	public final static int PRI_RTPM = PRI_MAX-5;
//	public final static int PRI_RTPL = PRI_MAX-6;
//	public final static int PRI_OVERLAY = 2;
//	public final static int PRI_OVERLAY_ROUTING = 2;
//	public final static int PRI_BROADCAST = 1;
//	
//	/*
//	 * 	Reliability
//	 * 
//	 * */
//	public final static int REL_MAX = 16;		
//		
//	public final static int REL_NONE = 0;
//	public final static int REL_ACK_TO_PARENT = 4;
//	public final static int REL_ACK_TO_SOURCE = 6;
//	public final static int REL_ACK_TO_PARENT_AND_SOURCE = 10;
//	
//	public final static int REL_BROADCAST = REL_NONE;
//	public final static int REL_ACK = REL_NONE;
//	
//	public final static int REL_OVERLAY_ROUTING = REL_NONE;

	
	
	/*
	 * BUFFER
	 * */
	public static int BUFFER_SIZE = 0; // messages, 0 = unlimited
	
		
	/*
	 * TIMEOUTS
	 * */
	public final static int ACK_TIMEOUT = 2000;	// milliseconds

	public static final long STREAM_SLEEP = 30000; // 30 seconds between streaming updates
	public static final long ROUTING_REQUEST_PERIOD = 20000; // 20 seconds

	/*
	 * RTP+ / RTCP+
	 * */
	public static int RTP_CREDIT_MAX = 50;	// it has to be a even value
//	public static int RTP_WINDOW_SIZE = 7500;	// it has to be a even value

	public static int RTCP_CREDIT_TIMEOUT = 1000;
	public static int RTP_ORDERING_MODE = Stream.FIFO;
	
	
	/**
	 *  Node Types
	 * */
	public static final int NODE_CAMERA = 1;
	public static final int NODE_FIREMAN = 1;
	public static final int NODE_CAR = 5;
	public static final int NODE_CONTROLCENTER = 10;

	/**
	 * 
	 * Node profile
	 * 
	 * */
	
	public static int TEAM_ID = 0;
	public static int NODE_TYPE = NODE_FIREMAN;
	public static String TEAM_CAR = null;		
	
	
	/**
	 * Overlay routing types
	 * */
	public static final int RT_ERROUTING = 1;
	public static final int RT_PROPHET = 2;
	public static final int RT_DTSFixed = 3;
	public static final int RT_MultihopFixed = 30;
	public static final int RT_Hiearchical = 40;


	public static final int RT_NextHopTimer= 500;
	
	public static final int RTP_NOACKED_MAX = 1;
	
	
	public static final int RTP_RRTimeout_MAX = 3;
	public static final int RTCP_RR_Timeout = 1000;
	

	public static int ROUTING_TYPE = RT_ERROUTING;	// Default is ERRouting		


	/**
	 * 
	 * Gateway
	 * */
	public static String FRAME_INDEX_FILE = "";	// "" no index file		
	public static int NUMBER_OF_PACKETS = 0;	// 0 = "Unknown"

	
	public static final int TS_MIN_WINDOW_SIZE = 0;
	
	/**
	 * 
	 * Setters
	 * 
	 * **/
	public static void setLocalhostAddress(String _ip)
	{
		LOCALHOST_ADDR = _ip;
	}
	
	public static void setMyAddr(String _ip)
	{
		MY_ADDR = _ip;
	}
	
	public static void setServerAddress(String _ip)
	{
		SERVER_ADDR = _ip;
	}
	
	public static void setClientAddress(String _ip)
	{
		CLIENT_ADDR = _ip;
	}
	
	public static void setBufferSize(int _size)
	{
		BUFFER_SIZE = _size;
		System.out.println("Seting buffer size "+BUFFER_SIZE);
	}
	
	public static void setNodeType(int _type)
	{
		switch(_type)
		{
			case 0:
				NODE_TYPE = Constants.NODE_CAMERA;
				break;
			case 1:
				NODE_TYPE = Constants.NODE_FIREMAN;
				break;
			case 2:
				NODE_TYPE = Constants.NODE_CAR;
				break;
			case 3:
				NODE_TYPE = Constants.NODE_CONTROLCENTER;
				break;
			default:
				NODE_TYPE = Constants.NODE_FIREMAN;
		}
	}
	
	public static void setRoutingType(int _rtype)
	{
		ROUTING_TYPE = _rtype;
//		switch(_type)
//		{
//			case 2:
//				ROUTING_TYPE = Constants.RT_PROPHET;
//				Debugger.dumpMsg(null, " Setting PROPHET", Debugger.MOMENTUM);
//
//				break;
//			case 3:
//				ROUTING_TYPE = Constants.RT_DTSFixed;
//				Debugger.dumpMsg(null, " Setting Fixed Routing", Debugger.MOMENTUM);
//
//				break;
//			default:
//				ROUTING_TYPE = Constants.RT_ERROUTING;
//				Debugger.dumpMsg(null, " Setting EOR", Debugger.MOMENTUM);
//		}
	}
	
	public static void setTeam(int _team)
	{
		TEAM_ID = _team;
		Debugger.dumpMsg(null, " Setting team "+_team, Debugger.MOMENTUM);
	}
	
	public static void setCar(String _carIp)
	{
		TEAM_CAR = _carIp;
		Debugger.dumpMsg(null, " Setting Car "+_carIp, Debugger.MOMENTUM);
	}
	
	public static void setRTCPTimeout(int _to)
	{
		RTCP_CREDIT_TIMEOUT = _to;
		Debugger.dumpMsg(null, " Setting RTCP Timeout "+_to,Debugger.MOMENTUM);
	}
	
	public static void setRTCPCredit(int _credit)
	{
		RTP_CREDIT_MAX = _credit;
		Debugger.dumpMsg(null, " Setting Max credit (Window Size) "+_credit,Debugger.MOMENTUM);
	}
	
	public static void setOrderingMode(int _ordering)
	{
		RTP_ORDERING_MODE = _ordering;
		Debugger.dumpMsg(null, " Setting Ordering Mode to "+_ordering,Debugger.MOMENTUM);
	}
	
	public static void setFrameIndexFile(String _FrameIndexFile)
	{
		FRAME_INDEX_FILE = _FrameIndexFile;
		Debugger.dumpMsg(null, " Setting frame index file to: "+_FrameIndexFile,Debugger.MOMENTUM);
	}
	
	public static void setNumberOfPackets(int _nop)
	{
		NUMBER_OF_PACKETS = _nop;
		Debugger.dumpMsg(null, " Setting the number of packets to receive from GW in "+_nop,Debugger.MOMENTUM);
	}

}
