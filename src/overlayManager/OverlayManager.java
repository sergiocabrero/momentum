package overlayManager;

import java.util.TimerTask;

import overlayTransport.OverlayTransport;
import routingInterface.RoutingInterface;
import utils.Constants;
import utils.Debugger;
import utils.MessageReceivedEventListener;
import utils.OverlayRoutingEventListener;
import utils.TopologyUpdateEventListener;

/*
 * Tasks of this Component:
 * 
 *  - Set the state of the sessions in the Session Manager
 * 
 * */
public class OverlayManager implements TopologyUpdateEventListener, MessageReceivedEventListener {
	
	private static OverlayManager _instance = null;
	
	// only one session at the moment
	private SessionManager sessionManager; 
	private Session session; 
	
	
	public static OverlayManager instance()
	{
		if(_instance == null)
		{
			_instance = new OverlayManager();
		}
		return _instance;
	}
	
	private OverlayManager()
	{
		session = new Session(Constants.SERVER_ADDR, Constants.CLIENT_ADDR, "TheSession", 1);
		sessionManager = new SessionManager(session);
		// Add listener to Topology changes
		RoutingInterface.instance().addEventListener(this);
		OverlayTransport.instance().addEventListener(this);
		
	}
	
	/**
	 * This should be called by the Routing Interface, and set the state of the sessions
	 * */
	public void handleTopologyUpdateEvent() {
		
		if(!Constants.MY_ADDR.equals(Constants.CLIENT_ADDR))
		{
			if(RoutingInterface.instance().isConnected(session.getDestination()))
			{
				// Disable EOR
				sessionManager.setLive();
			}
			else
			{
				// Enable EOR and put it into DT...
				sessionManager.setDT();
			}
		}
	}
	

	public void resetSession() // TODO: many sessions
	{
		// reset session state...
		handleTopologyUpdateEvent();
		
	}
	

	
	
	public void receiveMessage(int _messageType, String _parentAddr,
			byte[] _message) {
		
		if(_messageType == Constants.MT_OVERLAY)
		{
			// process overlay message
		}
		
	}

	
	
}
