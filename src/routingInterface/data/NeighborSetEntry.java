package routingInterface.data;


/**
 * NeighborSetEntry encapsulates neighbor information (address, willingness).
 * Also, it provides functions to get and set this information.
 * 
 * @author		Sergio Cabrero
 * @version     %I%, %G%
 * @since       1.0
 * @see			NeighborSetEntry
 */
public class NeighborSetEntry {
	private String address;
	private int willingness;
	private boolean MPRs;

	/*	Constructor */
	/**
	 * Constructor function. Sets the address variable.
	 * 
	 * @param _ip
	 */
	protected NeighborSetEntry(String _address)
	{
		this.address = _address;
		MPRs = false;
	}
	
	/*	Setters */

	/**
	 * Setter for the willingness variable.
	 * 
	 * @param _willingness
	 */
	protected void setMPRs(boolean MPRs)
	{
		this.MPRs = true;
	}
	
	/**
	 * Setter for the willingness variable.
	 * 
	 * @param _willingness
	 */
	protected void setMPRs(int _willingness)
	{
		this.willingness = _willingness;
	}
	/*	Getters */
	/**
	 * Getter for the willingness variable.
	 * @return willingness variable
	 */
	protected int getWillingness()
	{
		return this.willingness;
	}
	
	/**
	 * Getter for the address variable.
	 * @return	address variable
	 */
	protected String getAddress()
	{
		return this.address;
	}
	
	/**
	 * Getter for the address variable.
	 * @return	MPRs variable
	 */
	protected boolean isMPRs()
	{
		return MPRs;
	}
}
