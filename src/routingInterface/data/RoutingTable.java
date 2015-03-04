package routingInterface.data;



import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import utils.Constants;
/**
 * RoutingTable gives the interface to access the routing table information of the OLSR protocol.
 * It uses a Map to store the RoutingTableEntry objects. Also it provides functions to easily write/read this information.
 * 
 * @author		Sergio Cabrero
 * @version     %I%, %G%
 * @since       1.0
 */

public class RoutingTable {
	Map<String, RoutingTableEntry> table;
	Map<String, Integer> old_table;	// For change tracking

	/* Constructor */
	/**
	 * Constructs a new, empty RoutingTable. Creating a HashMap instance.
	 */
	public RoutingTable()
	{
		// MAP
	    table = Collections.synchronizedMap(new HashMap<String, RoutingTableEntry>());
	    old_table = Collections.synchronizedMap(new HashMap<String, Integer>());
	}
	
	/* Readers */
	/**
	 * @return all the nodes in the partition  
	 */
	public String[] getPartitionNodes()
	{
		synchronized(table)
		{
			if(table.isEmpty())
			{
				/*	Return empty string */
				return new String[]{""};
			}
			else
			{
					/* Return all the elements in the list */
					RoutingTableEntry rte;
					Set<String> result = new HashSet<String>();
					
					Iterator<String> iterator = this.table.keySet().iterator();
				    while (iterator.hasNext()) {
				    	 rte = this.table.get(iterator.next());
				    	 if(rte.getHops() > 0)
				    	 {
				    		 result.add(rte.getDestination());
				    	 }
				     }
				if(result.size() > 0)
				{
					String[] resultA = new String[result.size()];
					Iterator<String> it = result.iterator();
					int i=0;
					while(it.hasNext())
					{
						resultA[i] = it.next();
						i++;
					}
					return resultA;
				}
				else
				{
					return new String[]{""};
				}
				
			}
			
		}
	}
	
	/**
	 * @return routes with length _hops
	 */
	public String[] getNodesByHops(int _hops)
	{
		synchronized(table)
		{
			if(table.isEmpty())
			{
				/*	Return empty string */
				return new String[]{""};
			}
			else
			{
					// Variables
					RoutingTableEntry rte;
					String key;
					Set<String> result = Collections.synchronizedSet(new HashSet<String>());
					
					// Iteration
					int index = 0;
					Iterator<String> iterator = this.table.keySet().iterator();
				    while (iterator.hasNext()) {
				    	 key = iterator.next();
				    	 if(this.table.get(key).getHops() == _hops)
				    	 {
				    		 rte =  this.table.get(key);
				    		 result.add(rte.getDestination());
				    		 index++;
				    	 }
				     }
				    
				    // Returns
				    if(result.isEmpty())
				    {
				    	/*	Return empty string */
						return new String[]{""};
				    }
				    else
				    {				
				    	return result.toArray(new String[0]);
				    }
			}
			
		}
	}
	
	
	/**
	 * @param destination node
	 * @return the number of hops to the destination node, 0 if unknown  
	 */
	public int hopsTo(String _IP)
	{
		//synchronized(table)
		//{
			RoutingTableEntry rtentry;
			if(table.containsKey(_IP))
			{
				/*	Known IP */
				rtentry = table.get(_IP);
				return rtentry.getHops();
			}
			else
			{
				/*	Unknown IP */
				return -1;
			}
		//}
	}
	
	
	/**
	 * @param destination node
	 * @return the next hop node or gateway node to reach the destination node or the passed parameter, if unknown. 
	 */
	public String getNextHop(String _IP)
	{
		synchronized(table)
		{
			RoutingTableEntry rtentry;
			if(table.containsKey(_IP))
			{
				/*	Known IP */
				rtentry = table.get(_IP);
				return rtentry.getNextHop();
			}
			else
			{
				/*	Unknown IP */
				return _IP;
			}
		}
	}
	
	/**
	 * @param destination node
	 * @return the last encounter with this node
	 */
	public long getLastEncounter(String _IP)
	{
		synchronized(table)
		{
			RoutingTableEntry rtentry;
			if(table.containsKey(_IP))
			{
				/*	Known IP */
				rtentry = table.get(_IP);
				return rtentry.getLastEncounter();
			}
			else
			{
				/*	never encountered IP */
				return -1;
			}
		}
	}
	
	/**
	 * @param node to check
	 * @return the connectivity status of the node
	 */
	public boolean isConnected(String _IP)
	{
			return (this.hopsTo(_IP) != -1);
		//	return table.containsKey(_IP);	
	}
	
	
	/*	Writers	*/
	/**
	 * Adds a new route to the table. Creates a new RoutingTableEntry and adds it to the set.
	 * @param destination node
	 * @param gateway node
	 * @param distance in number of hops
	 */
	public void addRoute(String _destination, String _nextHop, int _hops)
	{
			synchronized(table)
			{
				RoutingTableEntry rtentry;
				if(table.containsKey(_destination))
				{
					// Update
					rtentry = table.get(_destination);
	
					
					rtentry.setHops(_hops);
					rtentry.setNextHop(_nextHop);
				}
				else
				{
					// Insert new
					rtentry = new RoutingTableEntry(_destination, _nextHop, _hops);
					table.put(_destination, rtentry);
				}
			}
	}
	
	/**
	 * Sets all the entries of the table to 0
	 */
	public void reset()
	{
		synchronized(table)
		{
			synchronized(old_table)
			{
				old_table.clear();
				
			
//			table.clear();
			/*	All distances to 0 */
			RoutingTableEntry rtentry;
			Set<String> keys = table.keySet();
		    Iterator<String> iterator = keys.iterator();
		    
		    
		     while (iterator.hasNext()) {
		    	 String key = iterator.next();
		    	 old_table.put(key, table.get(key).getHops());
		    	 table.get(key).reset();	 
		       }
			}
		}
		
	}
	
	public boolean wasChanged()
	{
		synchronized(old_table)
		{
			synchronized(table)
			{
				if(old_table.keySet().size() == table.keySet().size())	// same size?
				{
					Set<String> keys = table.keySet();
					Iterator<String> iterator = keys.iterator();
					
				    while (iterator.hasNext())
				    {
				    	//find one different, the only parameter is the number of hops.
				    	// Should we add the next hop?
				    	String key = iterator.next();
				    	if(!old_table.containsKey(key) || table.get(key).getHops() != old_table.get(key))
				    	{
				    		
				    		old_table.clear();
				    		return true;
				    	}
					       
					}
				    // same nodes and same distances
		    		old_table.clear();
				    return false;
				}
				else	// different nodes
				{
		    		old_table.clear();
					return true;
				}
			}
			
		}
	}
	
	/**
	 * Closes the object.
	 */
	public void close()
	{
		synchronized(table)
		{
			table.clear();
		}
	}

	
}
