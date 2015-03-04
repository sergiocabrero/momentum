package overlayRouting.erRouting;


import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;


import overlayRouting.OverlayRouting;
import overlayTransport.OverlayTransport;
import routingInterface.RoutingInterface;
import utils.Constants;
import utils.Debugger;


public class ERRankingRoutingClient  {

	private SortedMap<Double, String>  Ranking;
	
	private String nextHop;
	private String destination;
	private NextHopCalculationTimer timerTask;
	private Timer timer;
	
	public ERRankingRoutingClient(String _destination)
	{
		Ranking = (SortedMap<Double, String>) Collections.synchronizedSortedMap(new TreeMap<Double, String>());
		destination = _destination;
		nextHop = Constants.MY_ADDR;	// default
		
		timerTask = null;
		timer = new Timer();
	}

		
	
	protected void notifyPartitionChanges()
	{
		if(!RoutingInterface.instance().isConnected(destination))	// only has sense if the destination is not in our partition
		{
			// if the current next hop is not available
			if(!RoutingInterface.instance().isConnected(nextHop))
			{
				setNextHop(Constants.MY_ADDR);
			}
			
			ERRoutingMessage routeRequest = new ERRoutingMessage(this.destination);
			OverlayTransport.instance().sendBroadcast(Constants.MT_OVERLAY_ROUTING_REQUEST, routeRequest.getMessageBytes());
			Debugger.dumpMsg(this, "Sending Broadcast Next Hop Request to"+destination, Debugger.OVERLAY_ROUTING);
			
			
		}
		else
		{
			setNextHop(destination);
		}
	}
	
	/**
	 * 
	 * Shortcut function to calculate the ranking of this node
	 * 
	 * @return
	 */
	private double myRanking()
	{
		return ERRoutingFunctions.calculateRanking(1 ,	//	Establish 1 hop distance for this node (although not real) for calculation issues
								RoutingInterface.instance().milliSecondsSinceLastEncounter(destination),
								Constants.NODE_TYPE);
		
		
	}
	/**
	 * Selects the next hop from the ranking
	 * 
	 * @return
	 */
	public String nextHop()
	{
		return nextHop;
	}
	
	private void setNextHop(String _nh)
	{
		
		// if there is a change in the Overlay Routing, fire the event
		if(_nh != nextHop)
		{
			OverlayRouting.instance().fireOverlayRoutingUpdateEvent(destination);
		}
		nextHop = _nh;
	}
	
	private void calculateNextHop()
	{

		
		double rank, bestValue;
		HashSet<Double> toRemove = new HashSet<Double>();
		String nh = nextHop;
		if(RoutingInterface.instance().isConnected(destination))
		{
			// the destination is connected, forward to him
			nh = destination;
		}
		
		
		else 	// look for nextHop			
		{
			
				bestValue = myRanking();
				
				Iterator<Double> it = Ranking.keySet().iterator();
				
	
				while(it.hasNext())
				{
					rank = it.next();
					if(rank > bestValue)
					{
						// is it connected?
						if(RoutingInterface.instance().isConnected(Ranking.get(rank)))
						{
							nh = Ranking.get(rank);
	
						}
						else						
						{
							// it is not connected, remove from the rank
							toRemove.add(rank);
						}
					}
					
				
			}
		
		}
		// Remove not connected
		Iterator<Double> it2 = toRemove.iterator();
		while(it2.hasNext())
		{
			Ranking.remove(it2.next());
		}
		
		setNextHop(nh);
	}
	
	
	protected void receiveResponse(ERRoutingMessage _erm) {
				if(_erm.getDestination().equals(destination))	// Is it for me?
				{
					
					Ranking.put(ERRoutingFunctions.calculateRanking(RoutingInterface.instance().hopsTo(_erm.getSource()),_erm.getLastEncounter(), _erm.getNodeType()), _erm.getSource());
					
					Debugger.dumpMsg(this, "Node ranked: "+_erm.getSource()+" "+ERRoutingFunctions.calculateRanking(RoutingInterface.instance().hopsTo(_erm.getSource()),_erm.getLastEncounter(), _erm.getNodeType())+" ("+RoutingInterface.instance().hopsTo(_erm.getSource())+","+_erm.getLastEncounter()+","+_erm.getNodeType()+")", Debugger.OVERLAY_ROUTING);
					
					
					// Set/renew timer, if no message is received in 500 ms, update next-hop
					timer.cancel();
					if(timerTask != null)
					{
						timerTask.cancel();	
					}
					timerTask = new NextHopCalculationTimer();
					timer = new Timer();
					timer.schedule(timerTask, Constants.RT_NextHopTimer);
					
				}
	}
	
	
	class NextHopCalculationTimer extends TimerTask
	{

		private NextHopCalculationTimer()
		{
		
		}
		

		public void run() {
				// Send a SR with the same request Id
				calculateNextHop();
		}
	}
}
