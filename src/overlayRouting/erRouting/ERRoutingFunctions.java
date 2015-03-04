package overlayRouting.erRouting;


public abstract class ERRoutingFunctions {

	/**
	 * 
	 * Returns the ranking of a node according to fixed reliability (type and hops) and the time elapsed since the last encounter with that node
	 * 
	 * @param _hops
	 * @param _lastEncounter
	 * @param _type
	 * @return
	 */
	protected static double calculateRanking(int _hops, long _lastEncounter, int _type)
	{
		double ranking;
		if(_hops == 0)
		{
			// This shouldn't happen, so ranking = 0
			ranking = 0; 
		}
		else if(_lastEncounter == 0)
		{
			// he is connected, so should I be... lets wait... or rank top?
//			ranking = Double.MAX_VALUE;
			ranking = 0;

		}
		else if (_lastEncounter == -1)	// he was never connected
		{
			ranking=0;
		}
		else
		{
			ranking = (_lastEncounter/1000)*(_type/_hops);	// last encounter in seconds
		}
		
		if(ranking > 0)
			return ranking;
		else
			return 0;
	}
}
