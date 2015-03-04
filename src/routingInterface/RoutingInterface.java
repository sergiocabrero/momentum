package routingInterface;

//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.Set;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import routingInterface.data.NeighborSet;
import routingInterface.data.RoutingTable;
//import routingInterface.data.TopologySet;
import utils.Constants;
import utils.Debugger;
import utils.TopologyUpdateEventListener;


/**
 * RoutingInterface class provides a group of functions to access the information provided by the routing daemon. 
 * Although in this version it is coupled with OLSRDataReader, its aim is to make the system independent from the routing protocol used.
 * Therefore, when changing the routing protocol only this interface definition shouldn't be modified. However, the implementation might be. 
 * 
 *  Modified 16 Sept: 2-hop neighbors and topology sets disabled due to a new pluging version
 * 
 * @author		Sergio Cabrero
 * @version     %I%, %G%
 * @since       1.0
 */

public class RoutingInterface {
	private OLSRDataReader datareader;
	private RoutingTable routingtable;
//	private TopologySet topologyset;
	private NeighborSet neighborset;
//	private NeighborSet neighbor2hopset;
	
	static private RoutingInterface _instance = null;
	
	
	/**
	 * instance function for SINGLETON pattern
	 *  
	 * @return The unique instance of this class.
	 */
	static synchronized public RoutingInterface instance() {
	      if(_instance == null) {
	    	 _instance = new RoutingInterface();
	      }
	      return _instance;
	   }
	/**
	 * Constructor functions.
	 * Creates all the necessary structures to store Routing data and instanciates
	 * the OLSRDataReader object, which will read from the default port.
	 * 
	 */
	protected RoutingInterface()
	{
		this(Constants.ROUTINGPROT_PORT);
		
	}
	
	/**
	 * 
	 * Constructor functions.
	 * Creates all the necessary structures to store Routing data and instanciates
	 * the OLSRDataReader object, which will read from the specified port.  
	 * 
	 * @param _port
	 */
	protected RoutingInterface(int _port)
	{
		/*	Data structure init */
		routingtable = new RoutingTable();
//		topologyset = new TopologySet();
		neighborset = new NeighborSet();
//		neighbor2hopset = new NeighborSet();
		/*	Data reader from OLSR Plugin IPC */
//		datareader = new OLSRDataReader(_port, routingtable, topologyset, neighborset, neighbor2hopset);
		datareader = new OLSRDataReader(_port, routingtable, neighborset);

	}
	
	/*	Interface functions */
	
	/*	RoutingTable Object*/
	/**
	 * Retrieves and returns the number of hops to the provided node.
	 * 
	 * @param IP address of the node. 
	 * @return number of hops, 0 if it's localhost, or -1 if it is an unknown node.
	 */
	public int hopsTo(String _IP)
	{
		if(_IP.equals(Constants.MY_ADDR)) return 0;
		else return routingtable.hopsTo(_IP);
	}
	
	/**
	 * Returns connectivity status of the provided node.
	 * 
	 * @param IP address of the node. 
	 * @return true if the node is connected, false otherwise.
	 */
	public boolean isConnected(String _IP)
	{
			return routingtable.isConnected(_IP);
		
		//	return routingtable.hopsTo(_IP) == 1;
	}
	
	
	/**
	 * Returns the next hop node (gateway) in the route to a given node.
	 * 
	 * @param IP address of the node
	 * @return IP address of the next hop node
	 */
	public String getNextHop(String _IP)
	{
		return routingtable.getNextHop(_IP);
	}

	/**
	 * Returns the elapsed time from the last encounter with a node
	 * 
	 * @param IP address of the node
	 * @return IP address of the next hop node
	 */
	public long milliSecondsSinceLastEncounter(String _IP)
	{
		if(_IP.equals(Constants.MY_ADDR) || this.isConnected(_IP))
		{
			return 0;
		}
		else
		{
//			return (System.currentTimeMillis() - routingtable.getLastEncounter(_IP));
			long ts = routingtable.getLastEncounter(_IP);
			if(ts == -1)
			{
				return ts;
			}
			else
			{
				return System.currentTimeMillis()-ts;				
			}
		}
		
	}

	
	/*	Neighbors */
	/**
	 * Returns the 1-hop neighbors of the node.
	 * 
	 * @return 1-hop neighbor IP list
	 */
	public String[] get1hopNeighbors()
	{
	//	return routingtable.getNodesByHops(1);
		return neighborset.getNeighbors();

	}
	
	/**
	 * Returns true if this node is an MPR of _addr
	 * 
	 * @return 1-hop neighbor IP list
	 */
	public boolean isMPRs(String _addr)
	{
		return neighborset.isMPRs(_addr);

	}
	
	/**
	 * Returns the 2-hop neighbors of the node.
	 * 
	 * @return 2-hop neighbor IP list
	 */
	public String[] get2hopNeighbors()
	{
		return routingtable.getNodesByHops(2);
	}
	
	/**
	 * Returns the nodes in the same partitions (with routes
	 * 
	 * @return partition IP list
	 */
	public String[] getPartitionNodes()
	{
		return routingtable.getPartitionNodes();
	}
	
//	/* Topology */
//	/**
//	 * Returns the known neighbors of a node in the MANET
//	 *  
//	 * @param IP address of the node
//	 * @return Neighbor IP list
//	 */
//	public String[] getRemoteNeighbors(String _IP)
//	{
//		return topologyset.getDestinations(_IP);
//	}
//	
	
//	/*	Neighbors */
//	/**
//	 * Returns the 1-hop neighbors of the node.
//	 * 
//	 * @return 1-hop neighbor IP list
//	 */
//	public String[] get1hopNeighbors()
//	{
//		return neighborset.getNeighbors();
//	}
//	
//	/**
//	 * Returns the 2-hop neighbors of the node.
//	 * 
//	 * @return 2-hop neighbor IP list
//	 */
//	public String[] get2hopNeighbors()
//	{
//		return neighbor2hopset.getNeighbors();
//	}
	
//	/*	Returns a Set[] with the nodes between the current and "node".
//	 *  Nodes at distance i are in List[i]
//	 *  
//	 *  */
//	
//	/**
//	 * Returns a group of nodes in the path between the source node (the one executing the algorithm) and the destination node specified by the function parameter.
//	 * This nodes are sorted by hop distance from the source node.
//	 * Also, it is an estimation based on the available information and might not be an exact result.
//	 * 
//	 * @param destination node
//	 * @return Set[] where each member Set[i] contains nodes at distance i.   
//	 */
//	public Set[] getIntermediateNodes(String _IP)
//	{
//		Set[]		nodes;	// Final result
//		String[]	remoteNeighbors;
//		int maxHops, hops;
//		
//		/*	hops to the node */
//		maxHops = this.hopsTo(_IP);
//		
//		if(maxHops == 0)	// not connected node
//		{
//			nodes = new Set[1];
//			nodes[0] = new HashSet();
//			nodes[0].add(_IP);
//		}
//		else
//		{
//			// Create node list
//			nodes = new Set[maxHops+1];
//			for(int i=0;i<maxHops+1;i++)
//			{
//				nodes[i] = new HashSet();
//			}
//			
//			//	Add node to the his level
//			nodes[maxHops-1].add(_IP);
//			
//			/*	Add destination known neighbors */
//			remoteNeighbors = this.getRemoteNeighbors(_IP);
//			for(int i=0;i<remoteNeighbors.length;i++)
//			{
//				hops = this.hopsTo(remoteNeighbors[i]);
//				if(hops > maxHops)
//				{
//					nodes[maxHops].add(remoteNeighbors[i]);
//				}
//				else if(hops > 0)
//				{
//					nodes[hops-1].add(remoteNeighbors[i]);
//				}
//				
//			}
//			
//			
//			if(maxHops > 1)
//			{
//				
//				for(int i=maxHops-3;i>=0;i--)
//				{
//					
//					if(nodes[i+1].isEmpty())
//					{
//						/*	No nodes in previous level, return*/
//						System.out.println("Empty Level "+(i+1));
//						return nodes;
//					}
//					else
//					{
//						/* Iterate the elements in the previous level */
//						String element;
//						Iterator iterator = nodes[i+1].iterator();
//					    while (iterator.hasNext()) {
//					    	 element = (String) iterator.next();
//					    	 
//					    	 //	get the declared neighbors of the node at distance i+1
//					    	 remoteNeighbors = this.getRemoteNeighbors(element);
//					    	 
//					    	 //	Add only the neighbors at "i" hops
//					    	 for(int j=0;j<remoteNeighbors.length;j++)
//					    	 {
//					    		 hops = this.hopsTo(remoteNeighbors[j]);
//					    		 if( hops <= i+1 && hops > 0)
//					    		 {
//					    			 nodes[hops-1].add(remoteNeighbors[j]);
//					    		 }
//					    	 }
//					    }
//					 }
//				}
//			}
//		}
//		/*	Add source neighbors */
//		String[] neighbors = this.get1hopNeighbors();
//		for(int i=0;i<neighbors.length;i++)
//		{
//			nodes[0].add(neighbors[i]);
//		}
//			
//		return nodes; 
//	}
	
	/**
	 * Closes the RoutingInterface
	 */
	public void close()
	{
		// close reader
		datareader.close();
		
		// close data
		routingtable.close();
//		topologyset.close();
		neighborset.close();
//		neighbor2hopset.close();
	}
	
	/*
	 * Event Listeners
	 * 
	 * */
	
	private List<TopologyUpdateEventListener> _listeners = new ArrayList<TopologyUpdateEventListener>();
	 public synchronized void addEventListener(TopologyUpdateEventListener listener)  {
	     _listeners.add(listener);
	  }
	  public synchronized void removeEventListener(TopologyUpdateEventListener listener)   {
	     _listeners.remove(listener);
	  }
	 	 
	  // call this method whenever you want to notify
	 	  //the event listeners of the particular event
	 	  protected synchronized void fireTopologyUpdateEvent() {
	 	    Debugger.dumpMsg(this, "Firing Topology Update Event", Debugger.ROUTING_INTERFACE);
	 	    Iterator<TopologyUpdateEventListener> i = _listeners.iterator();
	 	    while(i.hasNext())  {
	 	      i.next().handleTopologyUpdateEvent();
	 	    }
	   }
	

}
