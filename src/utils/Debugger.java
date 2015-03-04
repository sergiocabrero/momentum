/**
 * 
 */
package utils;

import java.io.PrintStream;

/**
 * @author Francisco Javier Campa Lus
 *
 */
public final class Debugger {
	
	/**
	 * Default output
	 */
	static PrintStream out = System.out;
	static PrintStream err = System.err;
	
	/**
	 * On or off
	 */
	static boolean on = false;
	
	/**
	 * Debugging level
	 */
	static int level = 0;
	
	/**
	 * Activates debugging
	 */
	public static void activate(int lvl){
		on = true;
		level = lvl;
	}
	
	/**
	 * Deactivates debugging
	 */
	public static void deactivate(){
		on = false;
		level = 0;
	}
	
	/**
	 * Shows the message to stdout
	 * 
	 * @param text The text to show
	 */
	public static void dumpMsg(Object o, String text, int lvl){
		if(on && ((Debugger.level&lvl)==lvl))
			if(o != null)
				out.println(System.currentTimeMillis()+"\t"+" Level "+lvl+"\t"+Constants.MY_ADDR+"\t"+o.toString()+"\t"+text);
			else
				out.println(System.currentTimeMillis()+"\t"+" Level "+lvl+"\t"+Constants.MY_ADDR+"\t MOMEMTUM \t"+text);
	}
	
	/**
	 * Dumps the message to sterr
	 * 
	 * @param text The text to dump
	 */
	public static void dumpErr(Object o, String text){
		if(on)
			if(o != null)
				out.println("Error\t"+System.currentTimeMillis()+"\t"+Constants.MY_ADDR+"\t"+o.toString()+"\t"+text);
			else
				out.println("Error\t"+System.currentTimeMillis()+"\t"+Constants.MY_ADDR+"\t MOMEMTUM \t"+text);	}
	
	/**
	 * Debugging levels

	 */

	public static final int MOMENTUM = 1;
	public static final int OVERLAY_TRANSPORT = 2;
	public static final int OVERLAY_ROUTING = 4;
	public static final int RTP = 8;
	public static final int GATEWAY = 16;
	public static final int STORE = 32;
//	public static final int ACK_MANAGER = 64;
	public static final int ROUTING_INTERFACE = 64;
	public static final int OVERLAY_MANAGER = 128;



}
