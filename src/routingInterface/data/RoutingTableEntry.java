package routingInterface.data;



/**
 * RoutingTableEntry encapsulates the information about a route in the OLSR routing table.
 * It contains three basic values: destination node, next hop node or gateway node and the distance in number of hops 
 * 
 * @author		Sergio Cabrero
 * @version     %I%, %G%
 * @since       1.0
 */
public class RoutingTableEntry{
	private String destination; // Key field
	private String nextHop;
	private int hops;
	private long lastEncounter; // with this node
	
	/*	Constructors	*/
	
	/**
	 * Constructs a new RoutingTableEntry
	 * 
	 * @param destination node
	 * @param gateway node
	 * @param distance in hops
	 */
	protected RoutingTableEntry(String _destination, String _nextHop, int _hops)
	{
		this.destination = _destination;
		this.nextHop = _nextHop;
		this.lastEncounter = 0;
		setHops(_hops);
		
	}
	
	/*	Setters */
	/**
	 * Sets the address of the gateway node
	 * 
	 * @param gateway node
	 */
	protected void setNextHop(String _nextHop)
	{
		this.nextHop = _nextHop;
	}
	
	/**
	 * Sets the distance in hops 
	 * @param hops
	 */
	protected void setHops(int _hops)
	{
		this.hops = _hops;
		if(_hops > 0)
		{
			this.lastEncounter = System.currentTimeMillis();
		}
		
	}
	
	/**
	 *	Reset the entry setting hops to 0 a gateway to destination node 
	 */
	protected void reset()
	{
		this.setHops(-1);
		this.setNextHop(this.destination);
	}
	
	/*	Getters */
	/**
	 * @return destination of this route
	 */
	protected String getDestination()
	{
		return this.destination;
	}
	
	
	/**
	 * @return gateway node of this route
	 */
	protected String getNextHop()
	{
		return this.nextHop;
	}
	
	/**
	 * @return distance of this route in hops
	 */
	protected int getHops()
	{
		return this.hops;
	}
	
	protected long getLastEncounter()
	{
		return this.lastEncounter;
	}
	
}
