//import control.Control;
//import rtpplus.RTPPlus;
//import gateway.Gateway;
//import overlayManager.OverlayManager;
//import overlayTransport.OverlayTransport;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

import overlayManager.OverlayManager;
import overlayVideo.RTPPPacket;
import overlayVideo.RTPPacket;
import overlayVideo.Stream;
import utils.Constants;
import utils.Debugger;

/**
 * 
 * The MOMENTUM class is the main class of the system. It can receive the localhost IP as argument.
 * Use: java MOMENTUM [localhost_ip]  
 * 
 * @author sergio
 *
 */
public class MOMENTUM {

	/**
	 * The <i>main</i> function starts the necessary components.
	 * 
	 * @param args [localhost_ip]
	 */
	public static void main(String[] args)
	{

		
		if(args.length > 0 && args[0] != null)
		{
					// Set node localhost IP manually
					Constants.setMyAddr(args[0]);
		}
		if(args.length > 1 && args[1] != null)
		{
					// Set node type
					Constants.setNodeType(Integer.parseInt(args[1]));
		}
		if(args.length > 2 && args[2] != null)
		{
			// Set routing 
			Constants.setRoutingType(Integer.parseInt(args[2]));
		}
		
		if(args.length > 3 && args[3] != null)
		{
			// Set Credit
			Constants.setRTCPCredit(Integer.parseInt(args[3]));
		}

		if(args.length > 4 && args[4] != null)
		{
			// Set RTP timeout
			Constants.setRTCPTimeout(Integer.parseInt(args[4]));
		}
		if(args.length > 5 && args[5] != null)
		{
			// Set Packet ordering type
			Constants.setOrderingMode(Integer.parseInt(args[5]));
		}
		
		if(args.length > 6 && args[6] != null)
		{
			// Set Packet ordering type
			Constants.setFrameIndexFile(args[6]);
		}
		
		if(args.length > 7 && args[7] != null)
		{
			// Set Packet ordering type
			Constants.setNumberOfPackets(Integer.parseInt(args[7]));
		}
	
		Debugger.activate(Integer.parseInt("10011001", 2)); 
		OverlayManager.instance();
		Debugger.dumpMsg(null, " Running MOMENTUM v2.0", Debugger.MOMENTUM);

		

	}
	
	
	
}