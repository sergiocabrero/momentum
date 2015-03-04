package overlayRouting.fixedRouting;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import overlayRouting.OverlayRouting;
import routingInterface.RoutingInterface;
import utils.Constants;

public class MultihopFixedRouting extends OverlayRouting{
	
	private Set<String> carriers;
	
	public MultihopFixedRouting() {
		super();
		carriers = Collections.synchronizedSet(new HashSet<String>());
		
		for(int i=3; i<13;i++)
		{
			carriers.add("10.0.0."+i); 
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
			// the last number of the IP
			int last = Integer.parseInt(Constants.MY_ADDR.split("\\.")[3]);

			// forward to higher ips only
			Iterator<String> i = carriers.iterator();
			while(i.hasNext() && nextHop.equals(Constants.MY_ADDR))
			{
				String candidate = i.next();
				int lastCandidate = Integer.parseInt(candidate.split("\\.")[3]);

				int hops = RoutingInterface.instance().hopsTo(candidate);
				if(hops > 0 && lastCandidate > last)
				{
					nextHop = candidate;
				}
			}
									
			return nextHop;
		}
	}
}
