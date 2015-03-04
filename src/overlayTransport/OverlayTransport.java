package overlayTransport;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.sound.midi.Receiver;

import routingInterface.RoutingInterface;



import utils.Constants;
import utils.Debugger;
import utils.ExceptionManager;
import utils.MessageReceivedEventListener;

import utils.TopologyUpdateEventListener;
/**
* Transport class provides an interface to  send an receive UDP packets
*
* @author		Sergio Cabrero
* @version     %I%, %G%
* @since       1.0
* 
* @see			TransportSender, TransportReceiver
* 
**/
public class OverlayTransport implements Runnable{
	
	private DatagramSocket serviceSocket;
//	private DatagramSocket sendingSocket;

	private int servicePort;
	
	static private OverlayTransport _instance = null;
	private boolean alive;
	
	 
	/**
	 * Singleton
	 * 
	* @return The unique instance of this class.
	*/
	static synchronized public OverlayTransport instance() {
	      if(null == _instance) {
	    	  _instance = new OverlayTransport();
	      }
	      return _instance;
	   }
	
	
	/**
	 *
	 *Constructor
	 *
	 **/

	protected OverlayTransport()
	{  
		// Setup socket
		servicePort = Constants.DEFAULT_SERVICE_PORT;
		
		try {
			serviceSocket = new DatagramSocket(new InetSocketAddress(servicePort));
			serviceSocket.setBroadcast(true);

		//	sendingSocket = new DatagramSocket(new InetSocketAddress(servicePort+1));

		} catch (SocketException e) {
				ExceptionManager.catchException(e,
						"overlayTransport",
						"Transport",
						"init");
		}
		alive = false;
		this.start();
	//	sender = new UDPSender(serviceSocket);
		
	}
	
	public void sendBroadcast(int _messageType, byte[] _message) {
		this.send(_messageType, _message, Constants.BROADCAST_ADDR);
		
	}
	
	public void send(int _messageType, byte[] _message, String _dest)
	{
		OTMessage otm = new OTMessage(_messageType, _message);
		this.send(_dest, otm.getMessageBytes());
	}
	
	/**
	 * 
	 * Instantiates the DatagramPacket object, send it and sleep
	 * @param _dest
	 * @param _data
	 */
	private void send(String _dest, byte[] _data)
	{
		if(_dest.equals(Constants.LOCALHOST_ADDR) || _dest.equals(Constants.MY_ADDR))
		{
			Debugger.dumpMsg(this, "It makes no sense to send a message to myself", Debugger.OVERLAY_TRANSPORT);
		}
		else
		{
	//	long lastSent = 0;
		DatagramPacket packet;
	//	System.out.println(_dest+"\n"+new String(_data));
		try {
			packet = new DatagramPacket(_data,
										_data.length,
										InetAddress.getByName(_dest), servicePort);
			//sleep(lastSent);
			serviceSocket.send(packet);
			
		//	lastSent = System.nanoTime();
			
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
	}
	

	/*	Receive and enqueue network packets	*/
	public void run() {

		while(serviceSocket != null && !serviceSocket.isClosed() && alive) {
			
			DatagramPacket packet = new DatagramPacket(new byte[Constants.MTU], Constants.MTU);
			
			
			try {

				serviceSocket.receive(packet);
								
				// Receive something
				OTMessage otm = new OTMessage(packet.getAddress().getHostName(), packet.getData());
				notifyReceivers(otm);
												
			} catch (IOException e) {
				ExceptionManager.catchException(e,
						"overlayTransport",
						"Transport",
						"run");
						
			} 
			
			
			
		}
		
		
	}
	
	public void start()
	{
		if(alive == false)
		{
			alive = true;
			(new Thread(this)).start();
		}
	}
	
	public void close()
	{
		alive = false;
		serviceSocket.close();
		serviceSocket = null;
		
	}
	
	/*
	 * Message Receive Event Listeners
	 * 
	 * */
	
	 private List<MessageReceivedEventListener> _listeners = new ArrayList<MessageReceivedEventListener>();
	 public synchronized void addEventListener(MessageReceivedEventListener listener)  {
	     _listeners.add(listener);
	  }
	  public synchronized void removeEventListener(TopologyUpdateEventListener listener)   {
	     _listeners.remove(listener);
	  }
	 	 
	  // call this method whenever you want to notify
	 	  //the event listeners of the particular event
	 protected synchronized void notifyReceivers(OTMessage _otm) {
	 	    if(_otm != null)
	 	    {
		 	    Debugger.dumpMsg(this, "Passing message "+_otm.getType()+" from "+_otm.getParentAddr()+" to receivers (event)", Debugger.OVERLAY_TRANSPORT);
	 	    	Iterator<MessageReceivedEventListener> i = _listeners.iterator();
		 	    while(i.hasNext())  {
		 	      i.next().receiveMessage(_otm.getType(), _otm.getParentAddr(), _otm.getData());
		 	    }
		 	    
	 	    }
	   }


	
}