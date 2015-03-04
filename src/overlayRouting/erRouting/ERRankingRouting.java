package overlayRouting.erRouting;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


import overlayRouting.OverlayRouting;
import overlayTransport.OverlayTransport;
import routingInterface.RoutingInterface;
import utils.Constants;
import utils.MessageReceivedEventListener;

import utils.TopologyUpdateEventListener;

public class ERRankingRouting extends OverlayRouting implements MessageReceivedEventListener, TopologyUpdateEventListener{
	
	Map<String, ERRankingRoutingClient> clients;
	
	public ERRankingRouting()
	{
		super();
		clients = Collections.synchronizedMap(new HashMap<String, ERRankingRoutingClient>());
		OverlayTransport.instance().addEventListener(this);
		RoutingInterface.instance().addEventListener(this);
		
		this.start();
		 
	}
	
	
	
	public void close()
	{
	
		Iterator<String> myVeryOwnIterator = clients.keySet().iterator();
		while(myVeryOwnIterator.hasNext()) {
			String key = myVeryOwnIterator.next();
			clients.remove(key);
		}
	}
	

	public void handleTopologyUpdateEvent() {
		// TODO Auto-generated method stub
		
		Iterator<String> myVeryOwnIterator = clients.keySet().iterator();
		while(myVeryOwnIterator.hasNext()) {
			String key = myVeryOwnIterator.next();
			clients.get(key).notifyPartitionChanges();
		
		}
	}
	
	/* (non-Javadoc)
	 * Next hop selection
	 * @see overlayRouting.OverlayRouting#nextHop(java.lang.String)
	 */
	public String nextHop(String _destination)
	{
		// I may be the destination...
		if(_destination.equals(Constants.MY_ADDR))
		{
			return Constants.MY_ADDR;
		}
		else if(RoutingInterface.instance().isConnected(_destination))
		{
			// no need for a ferry
			return _destination;
		}
		else
		{
			if(!clients.containsKey(_destination))
			{
				// if the client doesn't exist, we have to create it
				clients.put(_destination, new ERRankingRoutingClient(_destination));
			}
			return clients.get(_destination).nextHop();
		}
		
	}
	
	

	
	
	


	
	public void receiveMessage(int _messageType, String _parentAddr,
			byte[] _message) {
		ERRoutingMessage request, response;
		double itsRank, myRank;
		if(_messageType == Constants.MT_OVERLAY_ROUTING_REQUEST)
		{
			request = new ERRoutingMessage(_parentAddr, _message);
	
			// the rank the other node will calculate for him
			itsRank = ERRoutingFunctions.calculateRanking(1, request.getLastEncounter(), request.getNodeType());
			
			// The rank the other node will calculate for me
			myRank = ERRoutingFunctions.calculateRanking(RoutingInterface.instance().hopsTo(request.getSource()), RoutingInterface.instance().milliSecondsSinceLastEncounter(request.getDestination()), Constants.NODE_TYPE);
			
			// If my rank is better, or I am the destination of the packets, send a response
			if(request.getDestination().equals(Constants.MY_ADDR) || myRank > itsRank)
			{
				response = new ERRoutingMessage(request.getRequestID(), request.getDestination());
				OverlayTransport.instance().send(Constants.MT_OVERLAY_ROUTING_RESPONSE, response.getMessageBytes(), request.getSource());
			}		
		}
		if(_messageType == Constants.MT_OVERLAY_ROUTING_RESPONSE)
		{
			response = new ERRoutingMessage(_parentAddr, _message);
			clients.get(Constants.CLIENT_ADDR).receiveResponse(response);
		}
	}



	@Override
	public void start() {
		// TODO Auto-generated method stub
		
	}



	
	
}
