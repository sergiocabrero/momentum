package overlayRouting.fixedRouting;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;


import overlayRouting.OverlayRouting;
import routingInterface.RoutingInterface;
import utils.Constants;

public class FixedRouting extends OverlayRouting{
	
	private Set<String> carriers;
	
	public FixedRouting() {
		super();
		carriers = Collections.synchronizedSet(new HashSet<String>());
		
		carriers.add("10.0.0.3"); 
		carriers.add("10.0.0.4");
		carriers.add("10.0.0.5");
		carriers.add("10.0.0.6"); 

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

		if(RoutingInterface.instance().isConnected(_destination))
		{
			return _destination;
		}
		else if(Constants.MY_ADDR.equals(Constants.SERVER_ADDR)) // Carriers
		{
			String nextHop = Constants.MY_ADDR;
			int hopsToNext = Integer.MAX_VALUE;
			
			Iterator<String> i = carriers.iterator();
			while(i.hasNext())
			{
				String candidate = i.next();
				int hops = RoutingInterface.instance().hopsTo(candidate);
				if(hops > 0 && hops < hopsToNext)
				{
					nextHop = candidate;
					hopsToNext = hops;
				}
			}
			
			return nextHop;
		}
		else
		{
			return Constants.MY_ADDR;
		}
	}
	
	

	
	
	

	
	
}
