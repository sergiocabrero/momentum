package overlayRouting.fixedRouting;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import overlayRouting.OverlayRouting;
import routingInterface.RoutingInterface;
import utils.Constants;

public class HierarchicalRouting extends OverlayRouting{
	
	private Set<String> carriers;
	
	public HierarchicalRouting() {
		super();
		carriers = Collections.synchronizedSet(new HashSet<String>());
		if(Constants.MY_ADDR.equals(Constants.SERVER_ADDR))
			// all first hops as carriers
		{
			String[] ipn = Constants.MY_ADDR.split("\\.");
			String carrier = ipn[0]+"."+ipn[1]+".0.3"; // if there is only one path 1 - 3 - ... - 2
			carriers.add(carrier);
			
			// For other paths, with hierarchical IPs, e.g. 10.0.path.hop
			for (int i = 1; i < 255; i++)
			{
				carrier = ipn[0]+"."+ipn[1]+"."+i+".1";
				carriers.add(carrier);
			}
		}
		else	// only next hop in my path as carrier
		{
			String[] ipn = Constants.MY_ADDR.split("\\.");
			String carrier = ipn[0]+"."+ipn[1]+"."+ipn[2]+"."+(Integer.getInteger(ipn[3])+1);
			carriers.add(carrier);
		}
		
		
	}
	
	public void start()
	{
		
	}
	public void close()
	{
	
	}

	
	/* (non-Javadoc)
	 * Next hop selection
	 * @see overlayRouting.OverlayRouting#nextHop(java.lang.String)
	 */
	public String nextHop(String _destination)
	{
		
		String nextHop = Constants.MY_ADDR;

		if(RoutingInterface.instance().isConnected(_destination))
		{
			return _destination;
		}
		else 
		{
			Iterator<String> i = carriers.iterator();
			while(i.hasNext() && nextHop.equals(Constants.MY_ADDR))
			{
				String candidate = i.next();
				int hops = RoutingInterface.instance().hopsTo(candidate);
				if(hops > 0)
				{
					nextHop = candidate;
				}
			}
									
			return nextHop;
		}
	}
}
