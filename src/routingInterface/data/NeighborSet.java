package routingInterface.data;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * This class manages a HashSet of NeighborSetEntry. It provides functions to add and get
 * Neighbour information.
 * 
 * @author		Sergio Cabrero
 * @version     %I%, %G%
 * @since       1.0
 * @see			NeighborSetEntry, HashSet
 */

public class NeighborSet {
	private Set<NeighborSetEntry> neighbors;
	
	/**
	 * 	Constructor function. It creates a HashSet.
	 */
	public NeighborSet()
	{
		neighbors =  Collections.synchronizedSet(new HashSet<NeighborSetEntry>());
	}
	
	/*	Writers */
	/**
	 * 
	 * Adds a neighbor to the set, adding a NeighborSetEntry object to the HashSet
	 * @param _IP
	 */
	public void addNeighbor(String _IP, boolean _MPRs)
	{
		synchronized(neighbors)
		{
			NeighborSetEntry nse = new NeighborSetEntry(_IP);
			neighbors.add(nse);
			nse.setMPRs(_MPRs);
		}
	}
	
	/**
	 * Reset all elements of the set
	 * 
	 */
	public void reset()
	{
		synchronized(neighbors)
		{
			neighbors.clear();
		}
	}
	
	/*	Readers */
	/**
	 * Returns the IP addresses of the elements elements in the set.
	 * 
	 * @return  An empty string if the Set is empty.
	 */
	public String[] getNeighbors(){
		synchronized(neighbors)
		{
			if(neighbors.isEmpty())
			{
				/*	Return empty string */
				return new String[]{""};
			}
			else
			{
					/* Return all the elements in the list */
					NeighborSetEntry nse;
					String[] result = new String[this.neighbors.size()];
					int index = 0;
					Iterator<NeighborSetEntry> iterator = this.neighbors.iterator();
				    while (iterator.hasNext()) {
				    	 nse = (NeighborSetEntry) iterator.next();
				    	 result[index] = nse.getAddress();
				    	 index++;
				     }
				
			    return result;
			}
		}
	}
	
	/**
	 * Returns false i the node is clearly not an PR 
	 * 
	 * @return  An empty string if the Set is empty.
	 */
	public boolean isMPRs(String _addr){
		synchronized(neighbors)
		{
			if(neighbors.isEmpty())
			{
				/*	Return true (for forwarding) */
				return true;
			}
			else
			{
					/* Return all the elements in the list */
					NeighborSetEntry nse;
					
					Iterator<NeighborSetEntry> iterator = this.neighbors.iterator();
				    while (iterator.hasNext()) {
				    	 nse = (NeighborSetEntry) iterator.next();
				    	 if(nse.getAddress().equals(_addr))
				    	 {
				    		 if(nse.isMPRs())
				    			 return true;
				    		 else
				    			 return false;
				    	 } 
				    	 			    	 
				     }
				
			    return true;
			}
		}
	}
	
	
	/**
	 * Close the object deleting the information
	 */
	public void close()
	{	
		synchronized(neighbors)
		{
			neighbors.clear();
		}
	}
}
