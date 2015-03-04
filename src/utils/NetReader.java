 package utils;


import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * NetReader is the class that provides a easy interface to easily read information from a 
 * Socket object (TCP socket).
 * 
 * @author		Sergio Cabrero
 * @version     %I%, %G%
 * @since       1.0
 * @see			Socket, BufferedInputStream
 */

public class NetReader {
	public final static String	CRLF = "\r\n";
	public final static String	EOF = "\rEOF";
	private BufferedInputStream in;
		
	/**
	 * Constructor function of the NetReader class. 
	 * Creates a BufferedInputStream by getting the input stream of the socket passed as parameter. 
	 * 
	 * @param _socket
	 */
	public NetReader(Socket _socket)
	{
		try
		{
			BufferedInputStream _in = new BufferedInputStream(_socket.getInputStream());
			in = _in;
		}
		catch(Exception e)
		{
			ExceptionManager.catchException(e,
					"Utils",
					"NetReader",
					"NetReader");
		}
		
	}
	
	/**
	 * readLine function reads one line of the input buffer and returns as a String.
	 * It reads characters until an <code>EOF</code> character is read, then returns. 
	 * 
	 * @return An string with the last line read
	 * 
	 * @throws An IO Exception throw by <code>BufferedInputStream.read()</code>
	 */
	
	public String readLine()
	{
		
		try
		{
			String res ="";
			while(true)
			{ 
				int c = in.read();
				if(c == '\n')	return res;
				if(c == -1) return EOF;
				if(c != '\r')
				{
					res += (char) c;
					
				}
			}
		}
		catch (IOException e) {
			ExceptionManager.catchException(e,
					"olsrDataInterface",
					"NetReader",
					"readLine");
			
		}
		return EOF;
	}
	
	
//	public String readAll() throws IOException
//	{
//		
//		String line;
//		String data ="";
//		do
//		{ 
//			line = readLine();
//			data += line;
//			if(line != EOF)
//			{
//				data += CRLF;
//			}
//		}while(line != EOF && in.available() > 0);
//		
//		return data;
//	}
	
	public String readAll() throws IOException
	{
		int first;
		
		int available;
		byte[] buffer;
		
		
		// Call read to block
		first =	in.read();
		if(first != -1)
		{
			available = in.available();
			// Read the next bytes
		
			buffer = new byte[available];
			in.read(buffer, 0, available);
		
			return (((char) first)+new String(buffer));
		}
		else
		{
			// EOF
			return EOF;
		}
	}
	
	public int read() throws IOException
	{
		return in.read();
		
	}
	
	public int read(byte[] b, int off, int len) throws IOException
	{
		return in.read(b, off, len);
	}
	/**
	 * Closes the NetReader closing the BufferedInputStream
	 * 
	 * @trows An IOException if there is any problem closing the buffer.  
	 */
	public void close()
	{
		try {
			in.close();
		} catch (IOException e) {
			ExceptionManager.catchException(e,
					"olsrDataInterface",
					"NetReader",
					"close");
		}
		
	}
}
