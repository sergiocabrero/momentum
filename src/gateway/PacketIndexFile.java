package gateway;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import utils.Constants;
import utils.Debugger;
import utils.ExceptionManager;

public class PacketIndexFile {
	private long[][] packets;
	
	public PacketIndexFile(String _fileName, int _numberOfPackets)
	{
		packets = new long[_numberOfPackets][3];
		
		// Read file
		File file = new File(_fileName);
		 
		BufferedReader bufRdr;
		try {
			bufRdr = new BufferedReader(new FileReader(file));
	
		String line = null;
		int row = 0;
		
		// Read line
		while((line = bufRdr.readLine()) != null && row < _numberOfPackets)
		{	
				String[] fields = new String[3];
				fields = line.split(" ");
				
				// sequence number
				packets[row][0] = Long.parseLong(fields[0]);
				// frame number
				packets[row][1] = Long.parseLong(fields[1]);

				// frame type  
				if(fields[2].equals("I"))
						packets[row][2] = Constants.MPEG_FRAME_I;
				else if(fields[2].equals("P"))
						packets[row][2] = Constants.MPEG_FRAME_P;
				else if(fields[2].equals("B"))
						packets[row][2] = Constants.MPEG_FRAME_B;
				else
				{
						packets[row][2] = Constants.MPEG_FRAME_UNKNOWN;
						Debugger.dumpErr(this, "Error reading line "+row+" of index file. Type of frame "+packets[row][1]+" in packet "+packets[row][0]+" is of unknown "+fields[2]);
				}		
			
				row++;
			}
		Debugger.dumpMsg(this, "Frame index file ("+_fileName+") read. "+_numberOfPackets+" packets read.", Debugger.GATEWAY);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public long getSeqNumber(int i)
	{
		if(i < packets.length)
		{
			return packets[i][0];
		}
		else
		{
			Debugger.dumpErr(this, "Packet requested by gateway ("+i+") is out of range ("+packets.length+") of the index file");
			return -1;
		}
	}
	public long getFrameNumber(int i)
	{
		if(i < packets.length)
		{
			return packets[i][1];
		}
		else
		{
			Debugger.dumpErr(this, "Packet requested by gateway ("+i+") is out of range ("+packets.length+") of the index file");
			return -1;
		}
	}
	
	public long getFrameType(int i)
	{
		if(i < packets.length)
		{
			return packets[i][2];
		}
		else
		{
			Debugger.dumpErr(this, "Packet requested by gateway ("+i+") is out of range ("+packets.length+") of the index file");
			return -1;
		}	}

	public boolean isLast(int i) {
		return (i == packets.length-1);
	}
	
}
