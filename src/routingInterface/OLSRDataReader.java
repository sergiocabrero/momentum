package routingInterface;




import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;


import routingInterface.data.NeighborSet;
import routingInterface.data.RoutingTable;
//import routingInterface.data.TopologySet;

import utils.Constants;
import utils.Debugger;
import utils.ExceptionManager;
import utils.NetReader;

/**
 * OLSRDataReader class reads, parses and stores the information provided by olsrd using a tcp socket.
 * 
 *  Modified 16 Sept: 2-hop neighbors and topology sets disabled due to a new plugin version
 * 
 * @author		Sergio Cabrero
 * @version     %I%, %G%
 * @since       2.0
 * @see			RoutingTable, TopologySet, NeighborSet
 */
public class OLSRDataReader implements Runnable{
	
	private Socket reader;
	private int port;
	
	private Thread myThread;
	private boolean alive;
	private RoutingTable routingtable;
//	private TopologySet topologyset;
	private NeighborSet neighborset;
//	private NeighborSet neighbor2hopset;
	
	
	/*	Auxiliar vars */
//	private String _lastTC;
	
	/*	Constructor	*/
	
	/**
	 * Constructor function.
	 * Sets the class variables and starts a worker thread.
	 * 
	 * @param _port
	 * @param _routingtable
	 * @param _topologyset
	 * @param _neighborset
	 * @param _neighbor2hopset
	 */
	protected OLSRDataReader(int _port, RoutingTable _routingtable, NeighborSet _neighborset)

//	protected OLSRDataReader(int _port, RoutingTable _routingtable, TopologySet _topologyset, NeighborSet _neighborset, NeighborSet _neighbor2hopset)
	{
		
		this.port = _port;
		
		// data
		this.routingtable = _routingtable;
//		this.topologyset = _topologyset;
		this.neighborset = _neighborset;
//		this.neighbor2hopset = _neighbor2hopset;
		
		
		
		
		// worker thread
		alive = true;
		myThread = new Thread(this);
		myThread.start();
		
		
	}
	
	/**
	 * Closes the object.
	 * Sets alive variable to false and closes the reader socket.
	 * This actions will force the thread to end.
	 * 
	 * @trows IOException throw by <code>Socket.close</code>
	 *
	 */
	protected void close()
	{
		alive = false;
		// close the socket to unblock receive
		try {
			if(reader != null)
				reader.close();
		} catch (IOException e) {
			ExceptionManager.catchException(e,
					"olsrDataInterface",
					"OLSRDataReader",
					"close");
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		runDataReader();
		//System.out.println("OLSRDataReader ended");
	}

	/**
	 * Worker thread function.
	 * While alive is true tryes to open and read the reader socket.
	 * It reads line by line and call <code>processLine</code> function
	 * 
	 * @throws UnknownHostException if socket stablishment fails.
	 * @throws IOException if problems with socket opening, reading of closing.
	 * 
	 */
	private void runDataReader()
	{
		NetReader inReader = null;
		String inData;
		
		
		do 
		{
			try {
				/*	
				 * Reset all the structures before connecting 
				 * 	and they are also reset if there has been a disconnection
				 * */
				this.resetAll();
				// Create socket
				reader = new Socket();
				/*	Create/bind socket and reader	*/
				reader.connect(new InetSocketAddress(Constants.LOCALHOST_ADDR,port));
				
				inReader = new NetReader(reader);
					
				/*	Read socket line by line	*/
				while(((inData = inReader.readLine()) != NetReader.EOF) && alive && reader.isConnected() && !reader.isClosed())
				{
						processLine(inData);			
				}
					
					/*	Close reader and socket	*/
					inReader.close();
					reader.close();
			
			} catch (UnknownHostException e) {
				ExceptionManager.catchException(e,
						"olsrDataInterface",
						"OLSRDataReader",
						"runDataReader");
			
			} catch (IOException e) {
				ExceptionManager.catchException(e,
						"olsrDataInterface",
						"OLSRDataReader",
						"runDataReader");
			}
		
			reader = null;
			// wait a second
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} while(alive); //Try to Reconnect if connection lost
		inReader.close();

	}
	
	/**
	 * 
	 * Determines which type of line are we reading and, either, calls the parser or reset the stored information 
	 * 
	 * @param _receivedstring is the line read in the socket
	 */
	private void processLine(String _receivedstring)
	{
		
	
		/*	Parse lines */
		
		if(_receivedstring.equals("data"))
		{
			//	data begins...
		}
		
		// Route processing
		
		if(_receivedstring.startsWith("routing_table"))
		{
			// Reset routes
			routingtable.reset();
		}
		
		if(_receivedstring.startsWith("route"))
		{
			processRouteLine(_receivedstring); 
		}
			
		// TC processing
//		if(_receivedstring.startsWith("tc_set"))
//		{
//			// Reset all sets
//			topologyset.reset();
//		}
//		else if(_receivedstring.startsWith("set"))
//		{
//			processTCSet(_receivedstring);
//		}	
//		
//		else if(_receivedstring.startsWith("destination"))
//		{
//			processTCDestination(_receivedstring);
//		}
//		
//		// 2 hop Neighbor processing
//		
//		else if(_receivedstring.startsWith("neighbors_2hop"))
//		{
//			// Reset all sets
//			neighbor2hopset.reset();
//			
//		}
//		
//		else if(_receivedstring.startsWith("neighbor_2hop"))
//		{
//			processNeighbor2Hop(_receivedstring);
//			
//		}
//		
//		// Neighbor processing
//		
		else if(_receivedstring.startsWith("neighbors"))
		{
			// Reset all sets
			neighborset.reset();
		}
//		
		else if(_receivedstring.startsWith("neighbor"))
		{
			processNeighbor(_receivedstring);

		}	
//				
		
		/*	End of data */
		else if(_receivedstring.startsWith("/data"))
		{
			//	data ends...
			if(routingtable.wasChanged())
			{
				notifyChanges();
			}
		}
	}
	
	private void notifyChanges()
	{
		/*	EXPERIMENT MONITORING */
//		String nodes[] = RoutingInterface.instance().getPartitionNodes();
//		
//		System.out.print(System.currentTimeMillis()+"\t"+
//				Constants.MY_ADDR+"\t"+			
//				"OLSR (Partition)"+"\t");
//		for(int i=0;i<nodes.length;i++)
//		{
//			System.out.print(nodes[i]+" "+RoutingInterface.instance().hopsTo(nodes[i])+"\t");
//		}
//		System.out.println();
		/*	END EXPERIMENT MONITORING */
		
		
	  RoutingInterface.instance().fireTopologyUpdateEvent();

		
		
		
	}
	
	/**
	 * Reset the information of all sets. 
	 */
	private void resetAll()
	{
		neighborset.reset();
	//	neighbor2hopset.reset();
	//	topologyset.reset();
		routingtable.reset();
	}

	
	/**
	 * Process a route type line.
	 * 
	 * @param _receivedstring
	 */
	private void processRouteLine(String _receivedstring)
	{
		// Split string
		// route	10.0.0.0	10.0.0.1	1
		String[] values = _receivedstring.split("\t");
			
		// Add route
		this.routingtable.addRoute(values[1], values[2], Integer.parseInt(values[3]));
		Debugger.dumpMsg(this, "Route added to "+values[1]+"\t"+values[2]+"\t"+Integer.parseInt(values[3]), Debugger.ROUTING_INTERFACE);
	}
	
//	/**
//	 * Process a topology set line.
//	 * 
//	 * @param _receivedstring
//	 */
//	private void processTCSet(String _receivedstring)
//	{
//		// Split string
//		// set	10.0.0.0
//		String[] values = _receivedstring.split("\t");
//		
//		// save last value
//		this._lastTC = values[1];
//	}
//	
//	/**
//	 * Process a topology destination line.
//	 * 
//	 * @param _receivedstring
//	 */
//	private void processTCDestination(String _receivedstring)
//	{
//		// Split string
//		// destination	10.0.0.0
//		String[] values = _receivedstring.split("\t");
//		
//		// add Destination
//		this.topologyset.addDestination(this._lastTC, values[1]);
//		
//		// And the inverse relationship
//		this.topologyset.addDestination(values[1], this._lastTC);
//	}
	
	/**
	 * Process a neighbor line.
	 * 
	 * @param _receivedstring
	 */
	private void processNeighbor(String _receivedstring)
	{
		// Split string
		// neighbor	10.0.0.0
		String[] values = _receivedstring.split("\t");
		
		// add Destination
		this.neighborset.addNeighbor(values[1], (values[2].equals("YES")));
		Debugger.dumpMsg(this, "Neighbour added "+values[1]+"\t"+values[2], Debugger.ROUTING_INTERFACE);

	}
	
	 
//	/**
//	 * Process a 2-hop neihbor line.
//	 * 
//	 * @param _receivedstring
//	 */
//	private void processNeighbor2Hop(String _receivedstring)
//	{
//		// Split string
//		// neighbor_2hop	10.0.0.0
//		String[] values = _receivedstring.split("\t");
//		
//		// add Destination
//		this.neighbor2hopset.addNeighbor(values[1]);
//	}
	
	
	
	
	 	 
}
