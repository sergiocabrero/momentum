package routingInterface.data;



import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * TopologySet stores information about the OLSR topology messages of all the network.
 * It stores a HashMap of TopologySetEntry objects and provides functions to easily read/write them.
 * 
 * @author		Sergio Cabrero
 * @version     %I%, %G%
 * @since       1.0
 * 
 * @see			TopologySetEntry
 */

public class TopologySet {
	private Map table;
	
	/*	Constructors */
	/**
	 * Constructs a new, empty ToplogySet. Instantiates a HashMap 
	 */
	public TopologySet()
	{

	     table = Collections.synchronizedMap(new HashMap());
	}
	
	/*	Readers */
	/**
	 * @param node address
	 * @return declared destinations that can be reached through a node (remote neighbors).
	 */
	public String[] getDestinations(String _IP)
	{
		synchronized(table)
		{
			if(table.containsKey(_IP))
			{
				TopologySetEntry tcentry = (TopologySetEntry) table.get(_IP);
				return tcentry.getDestinations();
			}
			else
			{
				// return the node itself
				return (new String[]{_IP});
			}
		}
	}
	
		
	
	/*	Writers	*/
	/**
	 * Adds a new destination to a TopologySetEntry, determined by a node address
	 * 
	 * @param node owner of the TopologySetEntry
	 * @param destination (neighbor) to add
	 */
	public void addDestination(String _last, String _destination)
	{
		synchronized(table)
		{
			TopologySetEntry tcentry;
			if(table.containsKey(_last))
			{
				// Update
				tcentry = (TopologySetEntry) table.get(_last);
				tcentry.addDestination(_destination);
			}
			else
			{
				// Insert new
				tcentry = new TopologySetEntry();
				tcentry.addDestination(_destination);
				table.put(_last, tcentry);
			}
		}
	}
	
	/**
	 * Reset the TopologySet by reseting all the entries
	 */
	public void reset()
	{
		synchronized(table)
		{	
			TopologySetEntry tcentry;
			Set entries = table.entrySet();
		    Iterator iterator = entries.iterator();
		    
		    
		     while (iterator.hasNext()) {
		       Map.Entry entry = (Map.Entry)iterator.next();
		       tcentry = (TopologySetEntry) entry.getValue();
		       tcentry.reset();
		     }
		}
	}
	
	/**
	 * Closes the TopologySet
	 */
	public void close()
	{	
		synchronized(table)
		{
			table.clear();
		}
	}
}
