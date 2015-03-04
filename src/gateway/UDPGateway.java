package gateway;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;


import utils.Constants;
import utils.ExceptionManager;

/**
 * 
 * Generic Gateway 
 * 
 * @author sergio
 *
 */

public abstract class UDPGateway implements Runnable {
	
	protected DatagramSocket socket;
	protected boolean alive = true;
	
	private int dport;	// port to send
	private int sport;	// port to bind
	
	private String daddr;	// address to send
	
	
		
	/**
	 *
	 *Constructor
	 *
	 **/

	public UDPGateway(String _daddr, int _sport, int _dport)
	{  
		sport = _sport;
		dport = _dport; 
		daddr = _daddr;
		try {
		//	System.out.println("Binding UDPGateway: "+Constants.LOCALHOST_ADDR+":"+sport);
			socket = new DatagramSocket(new InetSocketAddress(Constants.LOCALHOST_ADDR, sport));
			//socket.connect(new InetSocketAddress(_daddr, dport));
		} catch (SocketException e) {
				ExceptionManager.catchException(e,
						"gateway",
						"UDPGateway",
						"UDPGateway");
		}
		//sender = new UDPSender(socket);
		(new Thread(this)).start();
	}
	
	
	/**
	 * 
	 * Instantiates the DatagramPacket object, send it and sleep
	 * @param _dest
	 * @param _data
	 */
	public void send(byte[] _data)
	{
		
		DatagramPacket packet;
		try {
			packet = new DatagramPacket(_data,
										_data.length,
										InetAddress.getByName(daddr), dport);
		  //sender.send(packet);
			socket.send(packet);
			
		} catch (UnknownHostException e) {
			ExceptionManager.catchException(e,
					"overlayTransport",
					"Transport",
					"send");
		} catch (IOException e) {
			ExceptionManager.catchException(e,
					"overlayTransport",
					"Transport",
					"send");
		} 
	}
	

	/*	Receive and enqueue network packets	*/
	public void run() {

		
		while(alive && !socket.isClosed()) {
			
			DatagramPacket packet = new DatagramPacket(new byte[Constants.MTU], Constants.MTU);
			
			try {
			//	System.out.println("Receiving at port "+sport);
		
				socket.receive(packet);
				
				//System.out.println("Receive");
				// Receive something
				byte[] data = new byte[packet.getLength()];
				System.arraycopy(packet.getData(), 0, data, 0, data.length);
				
				// Send to the stream manager
				toStreamManager(data);
						
			} catch (IOException e) {
				ExceptionManager.catchException(e,
						"overlayTransport",
						"Transport",
						"run");
						
			} 
			
			
			
		}
		
		
	}
	
	/**
	 * Creates the right type of packet and send it to the stream manager
	 * 
	 * @param _data
	 */
	public abstract void toStreamManager(byte[] _data);

	public void close()
	{
		alive = false;
		socket.close();
		socket = null;
	}
}
