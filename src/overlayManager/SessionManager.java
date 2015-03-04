package overlayManager;

import overlayRouting.OverlayRouting;
import overlayTransport.OverlayTransport;
import overlayVideo.OverlayVideo;
import overlayVideo.StreamManager;
import utils.Constants;
import utils.Debugger;
import utils.OverlayRoutingEventListener;


public class SessionManager implements OverlayRoutingEventListener{
	private int state; // 0 iddle, 1 live, 2 DT, 3 storing
	private Session session;
	private StreamManager stream;
	private int streamCounter = 0;
	
	public SessionManager(Session _session)
	{
		state = 0;
		session = _session;
		
		// create one stream for the session
		stream = OverlayVideo.instance().newStream(session.getSessionID()+"_"+(streamCounter++));
		OverlayRouting.instance().addEventListener(session.getDestination(), this);
	}

	public void setIddle() {
		Debugger.dumpMsg(this, "Session set to Iddle", Debugger.OVERLAY_MANAGER);
		this.state = 0;
		stream.stop();

	}
	public void setLive() {
		Debugger.dumpMsg(this, "Session set to Live", Debugger.OVERLAY_MANAGER);
		this.state = 1;
		stream.play(session.getDestination());
	}
	public void setDT() {
		Debugger.dumpMsg(this, "Session set to DT", Debugger.OVERLAY_MANAGER);
		this.state = 2;
		playDT();
	}
	
	private void playDT(){
		// Look for the nexthop in the OverlayRouting
		String nh = OverlayRouting.instance().nextHop(session.getDestination());
		if(nh.equals(Constants.MY_ADDR))
		{
			this.setIddle();
		}
		else
		{
			stream.play(nh);
		}
	}

//	public void setStoring() {
//		this.state = 3;
//		stream.stop();
//
//	}

	
	public int getState() {
		return state;
	}

	public void overlayRoutingUpdate() {
		if(this.state != 1) // Not Live
		{
			playDT();
		}
	}
}
