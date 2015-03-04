package overlayVideo;

import java.net.InetAddress;
import java.net.UnknownHostException;
import utils.*;

/**
 * 
 * This class represents a RTP (Real Time Protocol) Packet
 * 
 * @author sergio
 *
 */
public class RTPPacket{
	
	// Packet
	private byte[] RTPpacket;
	

	/**
	 * 
	 * 
	 * @param _data
	 */
	public RTPPacket(byte[] _data)
	{
		RTPpacket = _data;
	}
	
	/**
	 * Returns the whole RTP packet
	 * 
	 * @return
	 */
	public byte[] getData()
	{
		return RTPpacket;
	}
	
	/**
	 * Gets the protocol versions from the two first bits of the packet
	 * 
	 * @return
	 */
	public int getV()
	{
		
		byte ba;
		// First byte of packet
		ba = RTPpacket[0];
		
		// Shift operation converts byte to int...
		// Bits are shifted to the right based on value of right-operand 
		return ba>>6;
	}
	
	
	/**
	 * 
	 * Gets the padding flag
	 * 
	 * @return
	 */
	public boolean getP()
	{
		byte ba;
		int X;
		// First byte of packet
		ba = RTPpacket[0];
		// Shift to the right
		X = ba>>5;
		
		// Get last bit
		X = (X & 0x01); 
		
		return (X==1);
	}

	
	/**
	 * 
	 * Gets the header extension flag
	 * 
	 * @return
	 */
	public boolean getX()
	{
		byte ba;
		int X;
		// First byte of packet
		ba = RTPpacket[0];
		// Shift to the right
		X = ba>>4;
		
		// Get last bit
		X = (X & 0x01); 
		
		return (X==1);
	}

	
	
	/**
	 * Gets the Contributing Source Number
	 * 
	 * @return
	 */
	public int getCC()
	{
		byte ba;
		int CC;
		// First byte of packet
		ba = RTPpacket[0];
		// Shift to the right
		CC = ba>>0;
		
		// Get last 4 bits
		CC = (CC & 0x0F); 
		
		return CC;
		
	}
	
	/**
	 * 
	 * Gets the special event flag (M)
	 * 
	 * @return
	 */
	public boolean getM()
	{
		byte ba;
		int M;
		// Second byte of packet
		ba = RTPpacket[1];
		// Shift to the right
		M = ba>>7;
		
		return (M==1);
	}
	
	
	/**
	 * Gets the payload type field (PT)
	 * 
	 * 
	 * @return
	 */
	public int getPT()
	{
		int PT;
		byte ba;
		ba = RTPpacket[1];
		
		PT = ba>>0;
		
		PT = (PT & 0x7F);
		
		return PT; 
	}
	
	/**
	 * Gets the RTP Sequence Number
	 * 
	 * @return
	 */
	public int getSequenceNumber()
	{
		byte[] ba = {RTPpacket[2], RTPpacket[3]};
		
		return Functions.unsignedShortToInt(ba);
	}
	
	/**
	 * 
	 * Gets the timestamp of the RTP Packet
	 * 
	 * @return
	 */
	public int getTimestamp()
	{
		byte[] ba = {RTPpacket[4], RTPpacket[5], RTPpacket[6], RTPpacket[7]};
		return Functions.byteArrayToInt(ba);
	}
	
	/**
	 * 
	 * Gets the Synchronization Source IP address
	 * 
	 * @return
	 */
	public String getSSRC()
	{
		byte[] baddr = new byte[4];
		String address = "";
		
		System.arraycopy(RTPpacket, 8, baddr, 0, 4);
		
		try {
			address = InetAddress.getByAddress(baddr).getHostAddress();
		} catch (UnknownHostException e) {
			System.out.println(e);
			//ExceptionManager.catchException(e, "RTPP", "RTPMessage", "getSSRC");
		}
		return address;
	}
	
	/**
	 * 
	 * Gets the Contributing sources IP addresses
	 * 
	 * @return
	 */
	public String[] getCSRC()
	{
		// Get the number of 
		int CC = getCC();
		int byteoffset = 12;
		
		String[] CSRC = new String[CC];
		byte[] addr = new byte[4];
		
		for(int i=0;i<CC;i++)
		{
			// get bytes of the address
			System.arraycopy(RTPpacket, byteoffset + (i*4) , addr, 0, 4);
			
			try {
				CSRC[i] = InetAddress.getByAddress(addr).getHostAddress();
			} catch (UnknownHostException e) {
				System.out.println(e);
				//ExceptionManager.catchException(e, "RTPP", "RTPMessage", "getSSRC");
			}
			
		}
		
		return CSRC;
	}
	
	
	/**
	 * Gets the payload of the RTP packet
	 * 
	 * 
	 * @return
	 */
	public byte[] getPayload()
	{
		// Calculate offset
		int CC = getCC();
		int byteoffset = 12 + CC*4;
		
		byte[] payload = new byte[RTPpacket.length - byteoffset];
		
		System.arraycopy(RTPpacket, byteoffset, payload, 0, payload.length);
		
		return payload;
	}
	
	/**
	 * Return the frame type code
	 * 
	 * @return
	 */
	public int getMPEGFrameType()
	{
		// get the 3 bits containing the type
		return (this.getPayload()[3] & 0x08);
	}
	
	//Funciones para la carga útil.
	
	public byte[] getTSPacket(int pos)
	{
		byte[] ba = new byte[188];
		System.arraycopy(this.getPayload(), pos*188, ba, 0, 188);
		/*for(int i = 0; i < 188; i++){
			ba[i] = this.getPayload()[pos*188 + i];
		}*/
		
		return ba;
	}
	
	// Javi:		******************************
	
	/*
	public byte[] getPesPacket()
	{
		// Calculate offset
		int CC = getCC();
		int byteoffset = 12 + CC*4;		
		int payloadLength = RTPpacket.length - byteoffset;
		
		byte[] ba = new byte[payloadLength];	// Tamaño del PES
		
		System.arraycopy(this.getPayload(), 0, ba, 0, payloadLength); 

		
		return ba;
	}*/
	
	public byte[] getPesPacket()
	{
		// Calculate offset
		int CC = getCC();
		int byteoffset = 12 + CC*4;		
		int payloadLength = RTPpacket.length - byteoffset;
		int srcPos = this.getStartCode();
		
		byte[] ba = new byte[payloadLength];	// Tamaño del PES
		
		if (srcPos > -1)
		{
			System.out.println("Hay PES");
			System.arraycopy(this.getPayload(), this.getStartCode(), ba, 0, payloadLength); 
		}
		
		else
		{
			System.out.println("NO hay PES     -------------------");
		}
		
		/*for(int i = 0; i < 188; i++){
			ba[i] = this.getPayload()[pos*188 + i];
		}*/
		
		return ba;
	}
	
	
	
	// *******************************************
	
	public int getNALType(byte[] pLoad)
	{
		int tipoNAL;
		byte ba;
		ba = pLoad[0];
		tipoNAL = ba & 0x1F;
		
		return tipoNAL;
	}
	
	public int getBitF(byte[] pLoad)
	{
		int F;
		byte ba;
		ba = pLoad[0];
		F = ba >> 7;
		F = (F & 0x01);
		
		return F;
	}
	
	public int getNRI(byte[] pLoad)
	{
		int NRI;
		byte ba;
		ba = pLoad[0];
		NRI = ba >> 5;
		NRI = (NRI & 0x03);
		
		return NRI;
	}
	
	/*
	 * 	Tipos de Frames
	 * 
	 */
	
	/*
	// Consigue el tipo de frame 
	// Devuelve el primer tipo de frame que encuentra en el paquete RTP. Puede haber más.
	public int getFrameType()
	{
		int aux = 0;
		byte ba = 0;
		
		for ( int i = 0 ; i < (RTPpacket.length-4) ; i ++)			//Revisar ese -4
		{
			if( 	RTPpacket[i] == 0x00 &&
					RTPpacket[i+1] == 0x00 &&
					RTPpacket[i+2] == 0x01)
			{

				// MPEG-4
				if (RTPpacket[i+3] == 0xb6)
				{
					ba = RTPpacket[i+4];
					aux = ba >> 6;
					aux = (aux & 0x03);
					
					// Indico que estoy trabajando con MPEG-4
					HM.SetMpegVersion(4);
					
					return aux;
				}	
				
				// MPEG-2
				else if (RTPpacket[i+3] == 0x00 && HM.GetMpegVersion() == 2)
				{
					ba = RTPpacket[i+5];
					aux = ba >> 3;
					aux = (aux & 0x07);
					
					return aux;
				}
			}
		}
		
		// Devuelvo por defecto un valor que no se corresponde con ningún tipo de frame
		return 9;
		
	}
	*/
	
	
	
	
	
	
	//TEMPORAL???????????????????????????????'
	
	// Indica el tipo de paquete que es
	public int getStartCode()
	{
		for ( int i = 0 ; i < (RTPpacket.length-4) ; i ++)			//Revisar ese -4
		{
			if( 	RTPpacket[i] == 0x00 &&
					RTPpacket[i+1] == 0x00 &&
					RTPpacket[i+2] == 0x01)
			{

				return RTPpacket[i+3];

			}
		}
		
		return 77;
		
	}
	
	
}