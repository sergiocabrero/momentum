package routingInterface.data;



import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * TopologySetEntry encapsulates and organizes information declared by the OLSR Topology messages from a node. 
 * It stores a Set of destinations that can be reached (neighbors) from a node
 *  
 * @author		Sergio Cabrero
 * @version     %I%, %G%
 * @since       1.0
 * @see			TopologySet
 */

public class TopologySetEntry {
	private Set destinations; // Advertaised Neighbors
	
	/*	Constructor */
	/**
	 * Construcs a new TopologySetEntry
	 * 
	 */
	protected TopologySetEntry()
	{

		this.destinations = new HashSet();
	}
	
	/*	Getters */
	/**
	 * 
	 * @return the destinations declared by the node.
	 */
	protected String[] getDestinations()
	{
		/* Return all the elements in the destinations list */
		String[] result = new String[this.destinations.size()];
		int index = 0;
		Iterator iterator = this.destinations.iterator();
	    while (iterator.hasNext()) {
	    	 result[index] = (String) iterator.next();
	    	 index++;
	     }

	    return result;
	}
	
	/*	Setters */
	/**
	 * Adds a node to the destination set
	 * 
	 * @param _destination
	 */
	protected void addDestination(String _destination)
	{
		this.destinations.add(_destination);
	}
	
	/**
	 *	Clear the set of destinations stored 
	 */
	protected void reset()
	{
		this.destinations.clear();
	}
}
