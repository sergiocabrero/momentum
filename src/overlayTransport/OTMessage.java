package overlayTransport;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import utils.Constants;
import utils.ExceptionManager;

/**
 * OTMessage class encapsulates the message format used in the Overlay Transport Layer.
 * It also provides a set of functions to easily read or modify it.
 * 
 * 
 * @author		Sergio Cabrero
 * @version     %I%, %G%
 * @since       1.0
 */

public class OTMessage {
		private static String headerFieldSeparator = "\n";
		private String parentAddr;				// who sent us the message

			
		private int messageType;
//		private byte FR;
		
		private byte[] data;
	
		/**
		 * 
		 * Creates a OTMessage received from _parentAddr with the content in _data
		 * 
		 * @param _parentAddr
		 * @param _data
		 */
		public OTMessage(String _parentAddr, byte[] _data)
		{
			parentAddr = _parentAddr;
			this.parseMessage(_data);
		}
		
		
		/**
		 * 
		 * Creates a message where the source is this node
		 * 
		 * @param _sequenceNumber
		 * @param _routeID
		 * @param _messageType
		 * @param _priority
		 * @param _reliability
		 * @param _data
		 */
		public OTMessage(int _messageType, byte[] _data)
		{
			this.messageType = _messageType;
			this.data = _data;
			this.parentAddr = Constants.MY_ADDR;
		}
		
		
		
		
		/**
		 * 
		 * Parses the raw bytes of a message to extract the header fields and the content
		 * 
		 * @param _data
		 */
		private void parseMessage(byte[] _data)
		{
			String message = new String(_data);
			String[] fields = message.split(headerFieldSeparator, 3);
			messageType = Integer.parseInt(fields[0]);
			int dataLength = Integer.parseInt(fields[1]);
		
			/*
			String sData = "";
			for(int i=7; i<fields.length; i++)
			{
				sData += fields[i]+this.headerFieldSeparator;
			}*/
			data = new byte[dataLength];
			System.arraycopy(_data, getHeaderBytes().length, data, 0, dataLength);
			//System.out.println("Message from "+sourceAddr+": "+data.length+" "+_data.length);
//			for(int i=0; i<dataLength; i++)
//			{
//				data[dataLength-1-i] = _data[_data.length-1-i];
//			}
			//System.out.println("Data of "+getKey()+": "+sourceAddr+" "+dataLength+"\n"+new String(getData())+"\n");
		}
		
		/**
		 * 
		 * Returns the header of a message in bytes
		 * 
		 * @return
		 */
		private byte[] getHeaderBytes()
		{
			
			String header = "";
			header += messageType+headerFieldSeparator;
			header += data.length+headerFieldSeparator;
			return header.getBytes();
		}
		
	
				
		/**
		 * 
		 * Gets the byte representation of this OTMessage
		 * 
		 * @return
		 */
		public byte[] getMessageBytes()
		{
			byte[] header = getHeaderBytes();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				
				baos.write(header);
				baos.write(data);
		
			} catch (IOException e) {
				ExceptionManager.catchException(e, "overlayTransport", "OTMessage", "getMessageBytes");
			}
			
				return baos.toByteArray();
		}
	
		/*	Getters */
		/**
		 * @return
		 */
		public byte[] getData() {
			return data;
		}


			
		public String getParentAddr() {
			return parentAddr;
		}
		
		public int getType()
		{
			return messageType;
		}

				
	
			
}
