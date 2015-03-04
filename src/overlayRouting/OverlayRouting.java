package overlayRouting;

//import overlayRouting.erRouting.ERRankingRouting;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import overlayRouting.erRouting.ERRankingRouting;
import overlayRouting.fixedRouting.FixedRouting;
import overlayRouting.fixedRouting.HierarchicalRouting;
import overlayRouting.fixedRouting.MultihopFixedRouting;
//import overlayRouting.prophetRouting.ProphetRouting;
import utils.Constants;
import utils.Debugger;
import utils.OverlayRoutingEventListener;


public abstract class OverlayRouting {
	private static OverlayRouting _instance = null;
	
	
	public synchronized static OverlayRouting instance()
	{
		if(_instance == null)
		{
			switch(Constants.ROUTING_TYPE)
			{
				case Constants.RT_ERROUTING:
						Debugger.dumpMsg(null, "Using EOR Routing", Debugger.MOMENTUM);
						_instance = new ERRankingRouting();
						break;
				case Constants.RT_DTSFixed:
					Debugger.dumpMsg(null, "Using Fixed Routing", Debugger.MOMENTUM);
					_instance = new FixedRouting();
					break;
				case Constants.RT_MultihopFixed:
					Debugger.dumpMsg(null, "Using Multihop Fixed Routing", Debugger.MOMENTUM);
					_instance = new MultihopFixedRouting();
					break;
				case Constants.RT_Hiearchical:
					Debugger.dumpMsg(null, "Using Hierarchica Fixed Routing", Debugger.MOMENTUM);
					_instance = new HierarchicalRouting();
					break;
//   			case Constants.RT_PROPHET:
//					_instance = new ProphetRouting();
//				break;
				default:
					Debugger.dumpMsg(null, "Using Hierarchica Fixed Routing", Debugger.MOMENTUM);
					_instance = new HierarchicalRouting();
				//				default:
//					_instance = new ERRankingRouting();
			}
		}
		return _instance;
	}
	
	/**
	 * 
	 */
	protected OverlayRouting()
	{

	}

	
	public abstract String nextHop(String _destination);

	public abstract void start();
	public abstract void close();
	
	
	/*
	 * Event Listeners
	 * 
	 * */
	
	private SortedMap<String, List<OverlayRoutingEventListener>> _listeners = new TreeMap<String, List<OverlayRoutingEventListener>>();
	 public synchronized void addEventListener(String destination, OverlayRoutingEventListener listener)  {
	     if(!_listeners.containsKey(destination))
	     {
	    	 _listeners.put(destination, new ArrayList<OverlayRoutingEventListener>());
	     }
		 _listeners.get(destination).add(listener);
	  }
	  public synchronized void removeEventListener(String destination, OverlayRoutingEventListener listener)   {
	    if(_listeners.containsKey(destination))
	    {
	    	_listeners.get(destination).remove(listener);
	    }
	  }
	 	 
	  // call this method whenever you want to notify
	 	  //the event listeners of the particular event
	 	  public synchronized void fireOverlayRoutingUpdateEvent(String destination) {
	 	    Debugger.dumpMsg(this, "Firing Overlay Routing Update Event for destination "+destination, Debugger.OVERLAY_ROUTING);
	 		    Iterator<OverlayRoutingEventListener> i = _listeners.get(destination).iterator();
	 	    while(i.hasNext())  {
	 	      i.next().overlayRoutingUpdate();
	 	    }
	   }
	
}
