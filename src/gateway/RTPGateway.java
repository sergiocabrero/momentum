package gateway;

import java.util.LinkedList;

import overlayVideo.RTPPPacket;
import overlayVideo.StreamManager;
import utils.Constants;
import utils.Debugger;

public class RTPGateway extends UDPGateway {

	private String id;
	private int packetCounter = 0;
	private LinkedList<RTPPPacket> storedPackets;
	private PacketIndexFile packetIndex;
	private long GOPCounter=0;
	
	private StreamManager stream;


	
	/**
	 * Constructor
	 * 
	 * @param _owner
	 * @param _daddr
	 * @param _sport
	 * @param _dport
	 */
	public RTPGateway(String _id, String _daddr, int _sport, int _dport, StreamManager _stream) {
		
			super(_daddr, _sport, _dport);
			id = _id;
			storedPackets = new LinkedList<RTPPPacket>();
			stream = _stream;
			if(!Constants.FRAME_INDEX_FILE.isEmpty())
			{
				packetIndex = new PacketIndexFile(Constants.FRAME_INDEX_FILE, Constants.NUMBER_OF_PACKETS);
			}
	}
	
	// with a packet index file;
	public RTPGateway(String _id, String _daddr, int _sport, int _dport, StreamManager _stream, String _fileIndex, int _numberOfPackets)
	{
		this(_id, _daddr, _sport, _dport, _stream);
		packetIndex = new PacketIndexFile(_fileIndex, _numberOfPackets);

		
	}
	
	
	public void toStreamManager(byte[] _data) {
		
			RTPPPacket p;
			
			// get packet properties TODO: this dynamic
			long seqNumber = packetIndex.getSeqNumber(packetCounter);
			int frameType = (int) packetIndex.getFrameType(packetCounter);
			long frameNumber = packetIndex.getFrameNumber(packetCounter);
			
			// count GOPs
			if(packetCounter>0)
			{
				long previousFrameType = packetIndex.getFrameType(packetCounter-1);
				if(frameType == Constants.MPEG_FRAME_I && frameType != previousFrameType)
				{
					GOPCounter++;
				}
			}
			// TODO: Get the number of packets per frame... difficult to do online (what happens with the last frame?)
		
			
			p = new RTPPPacket(id, seqNumber, GOPCounter, frameNumber, frameType, _data);
			storedPackets.add(p);
			Debugger.dumpMsg(this, "RTP packet "+p.getSequenceNumber()+" RTP (Seq TS): "+p.getRTPSequenceNumber()+" "+p.getRTPTimeStamp()+" Frame SN: "+p.getFrameSN()+" Size: "+p.getRTPPacket().length, Debugger.GATEWAY);

			// TODO: do it with marker bit, otherwise we discard last frame
			if((packetCounter > 0 && frameNumber != packetIndex.getFrameNumber(packetCounter-1)) || packetIndex.isLast(packetCounter))
			{	
					stream.receive(storedPackets);
					storedPackets.clear();
			}


			packetCounter++;
	}
	

}
