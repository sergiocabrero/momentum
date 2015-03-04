package overlayVideo;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import utils.Constants;
import utils.Debugger;

public class Stream {
	private SortedMap<Long, RTPPPacket> packetStore; // packets stored 
	private SortedSet<Long> packetsToSchedule; // packets stored not scheduled
	private LinkedList<Long> packetsScheduled;
	private SortedMap<Long, SortedSet<Long>> frames; // frames and numbers of the packets in which they are stored
	private SortedMap<Long, Long> framesIByGOP; // I-frames grouped GOP 
	private SortedMap<Long, SortedSet<Long>> framesPByGOP; // P-frames grouped GOP 
	private SortedMap<Long, SortedSet<Long>> framesBByGOP; // B-frames grouped GOP 

	private String id;
	private String source;
	private String destination;
	private long bytesStored;
	
	private SortedMap<Integer, TSWindow> windows;
	private Object packetsStored;

	
	/**
	 * COMMON Class Functions
	 * 
	 * */
	
	
	public Stream(String _id, String _source, String _destination)
	{
	
		packetStore = new TreeMap<Long, RTPPPacket>();
		packetsToSchedule = new TreeSet<Long>();
		packetsScheduled = new LinkedList<Long>();
		frames = new TreeMap<Long, SortedSet<Long>>();

		framesIByGOP = new TreeMap<Long, Long>();
		framesPByGOP = new TreeMap<Long, SortedSet<Long>>();
		framesBByGOP = new TreeMap<Long, SortedSet<Long>>();

		id = _id;
		source = _source;
		destination = _destination;
		bytesStored = 0;
		
		windows = new TreeMap<Integer, TSWindow>();

	}


	public String getDestination() {
		return destination;
	}

	public String getSource() {
		return source;
	}


	public String getId() {
		return id;
	}
	
	public RTPPPacket getPacket(long _sn)
	{
		synchronized (packetStore) {
			return packetStore.get(_sn);
		}
	}
	
	/**
	 * 
	 * Adds a packet to the store. Updates metadata for scheduling.
	 * 
	 * @param _packet
	 */
	public void addPacket(RTPPPacket _packet)
	{
		synchronized (packetStore) {
			long sn = _packet.getSequenceNumber();
			long frameSN = _packet.getFrameSN();
			int priority = _packet.getPriority();
			long gop = _packet.getGOP();
			
			//Debugger.dumpMsg(this, "Adding packet "+sn+" with frame "+frameSN+" (is first? "+frames.containsKey(frameSN)+"), gop: "+gop+"priority: "+priority, Debugger.STORE);
			// Add it to the store
			packetStore.put(sn, _packet);

			//Add it to the scheduling structures
			packetsToSchedule.add(sn);

			// it's the first packet of this frame, add to frame structures
			if(!frames.containsKey(frameSN))
			{
				frames.put(frameSN, new TreeSet<Long>());
			
				switch(priority){
					case Constants.MPEG_FRAME_I:
						//	 it's the first frame of this gop
						if(!framesIByGOP.containsKey(gop))
						{
							framesIByGOP.put(gop, frameSN);
						}
						
						break;
					case Constants.MPEG_FRAME_P:
						// it's the first frame of this gop
						if(!framesPByGOP.containsKey(gop))
						{
							framesPByGOP.put(gop, new TreeSet<Long>());
						}
						
						framesPByGOP.get(gop).add(frameSN);
						break;
					default:	// add to B
						//  it's the first frame of this gop
						if(!framesBByGOP.containsKey(gop))
						{
							framesBByGOP.put(gop, new TreeSet<Long>());
						}
						
						framesBByGOP.get(gop).add(frameSN);
						
						break;
				}

			}
			frames.get(frameSN).add(sn);						
			
			bytesStored += _packet.getMessage().length;
			Debugger.dumpMsg(this, "Video buffer size: "+bytesStored+" bytes", Debugger.STORE);
		}
		
	}
	
	
	/**
	 * Removes a packet from the store
	 * 
	 * @param _sn
	 */
	public void removePacket(long _sn)
	{
		synchronized(packetStore)
		{
			RTPPPacket p = packetStore.remove(_sn);
			bytesStored -= p.getMessage().length;
			Debugger.dumpMsg(this, "Video buffer size "+bytesStored+" bytes", Debugger.STORE);

			// this shouldn't happen
			if(packetsToSchedule.contains(_sn))
			{
				Debugger.dumpErr(this, "Removing a packet that was not scheduled. This shouldn't happen");
				packetScheduled(_sn);
			}
		}
	}
	
	/**
	 * 
	 * Updates metadata after scheduling a frame
	 * 
	 * @param _fn
	 */
	private void frameScheduled(long _fn)
	{
		synchronized (packetStore) {
			
			if(frames.containsKey(_fn))
			{
				// Remove from frames and get packets
				SortedSet<Long> packets = frames.remove(_fn);
				
				// get priority and gop
				int priority = packetStore.get(packets.first()).getPriority();
				long gop = packetStore.get(packets.first()).getGOP();
	
				// remove packets
				Iterator<Long> it = packets.iterator();
				while (it.hasNext()) {
					packetsToSchedule.remove(it.next());
				}
				
				// Remove from gop/priority structures
				switch(priority){
				case Constants.MPEG_FRAME_I:
					
					if(framesIByGOP.containsKey(gop))
					{
						framesIByGOP.remove(gop);
					}
					
					break;
				case Constants.MPEG_FRAME_P:
				
					if(framesPByGOP.containsKey(gop))
					{
						framesPByGOP.get(gop).remove(_fn);
						if(framesPByGOP.get(gop).isEmpty())
						{
							framesPByGOP.remove(gop);
						}
					}
					
					break;
				default:	
					if(framesBByGOP.containsKey(gop))
					{
						framesBByGOP.get(gop).remove(_fn);
						if(framesBByGOP.get(gop).isEmpty())
						{
							framesBByGOP.remove(gop);
						}
					}
					
					break;
			}
				
			}
		}
	}
	
	/**
	 * 
	 * Updates metadata after scheduling a packet
	 * 
	 * @param _sn
	 */
	public void packetScheduled(long _sn)
	{
		synchronized (packetStore) {
			RTPPPacket p = packetStore.get(_sn);
			int priority = p.getPriority();
			long gop = p.getGOP();
			
			packetsToSchedule.remove(_sn);
			
			// remove in frames also
			long frameSN = p.getFrameSN();
			
			// it's the first packet of this frame
			if(frames.containsKey(frameSN))
			{
				frames.get(frameSN).remove(_sn);
				
					
				// if no packets of these frames, remove the entry
				if(frames.get(frameSN).isEmpty())
				{
					frames.remove(frameSN);
							
					// Fixed: Only remove when there are no more packets of the frame...	
					switch(priority){
						case Constants.MPEG_FRAME_I:
							
							if(framesIByGOP.containsKey(gop))
							{
								framesIByGOP.remove(gop);
							}
							
							break;
						case Constants.MPEG_FRAME_P:
						
							if(framesPByGOP.containsKey(gop))
							{
								framesPByGOP.get(gop).remove(frameSN);
								if(framesPByGOP.get(gop).isEmpty())
								{
									framesPByGOP.remove(gop);
								}
							}
							
							break;
						default:	
							if(framesBByGOP.containsKey(gop))
							{
								framesBByGOP.get(gop).remove(frameSN);
								if(framesBByGOP.get(gop).isEmpty())
								{
									framesBByGOP.remove(gop);
									
								}
							}
							
							break;
					}
				}
			}
				

			
			bytesStored -= p.getMessage().length;
			p = null;
			
			
			
		}

	}
	
	/**
	 * 
	 * Checks if a packet is in the store
	 * 
	 * @param _sn
	 * @return
	 */
	public boolean isPacket(long _sn)
	{
		synchronized (packetStore)
		{
			return packetStore.containsKey(_sn);
		}
	}
	
	/**
	 * 
	 * Checks if a packet is scheduled for forwarding
	 * 
	 * @param _sn
	 * @return
	 */
	public boolean isPacketScheduled(long _sn)
	{
		synchronized (packetStore)
		{
			return !packetsToSchedule.contains(_sn);
		}
	}
	
	/**
	 * 
	 * Returns the size of the video buffer in bytes
	 * 
	 * @return
	 */
	public long getBytesStored()
	{
		return bytesStored;
	}
	
	/**
	 * 
	 * Returns the number of packets in the video buffer
	 * 
	 * @return
	 */
	public int getNumberOfPacketsStored()
	{
		synchronized(packetStore)
		{
			return packetStore.size();
		}
	}
	
	/**
	 * 
	 * Returns the number of packets in the store not yet scheduled
	 * 
	 * @return
	 */
	public int getNumberOfPacketsToSchedule()
	{
		synchronized(packetStore)
		{
			return packetsToSchedule.size();
		}
	}
	
	/**
	 * 
	 * Returns all the stored frames
	 * 
	 * @return
	 */
	public long[] getStoredFrames()
	{
		synchronized(frames)
		{
			Set<Long> k = frames.keySet();
			long[] l = new long[k.size()];
			int i =0;
			for (Iterator<Long> iterator = k.iterator(); iterator.hasNext();) {
				l[i] = ((Long) iterator.next()).longValue();
				i++;
			}
			
			return l;
		}
	}
	

	
	
	/**
	 * 
	 * Returns all the stored frames of a Group of Pictures (GOP)
	 * 
	 * @param _GOP
	 * @return
	 */
	public SortedSet<Long> getFramesByGOP(long _GOP)
	{
		synchronized(packetStore)
		{
			SortedSet<Long> ret = new TreeSet<Long>();
			ret.add(framesIByGOP.get(_GOP));
			ret.addAll(framesPByGOP.get(_GOP));
			ret.addAll(framesBByGOP.get(_GOP));
			return ret;
		}
	}
	
	
	/**
	 * 
	 * Returns the sequence numbers of all the packets in the video buffer
	 * 
	 * @return
	 */
	public long[] getStoredPackets()
	{
		synchronized(packetStore)
		{
			Set<Long> k = packetStore.keySet();
			long[] l = new long[k.size()];
			int i =0;
			for (Iterator<Long> iterator = k.iterator(); iterator.hasNext();) {
				l[i] = ((Long) iterator.next()).longValue();
				i++;
			}
			
			return l;
		}
	}
	
	
	
	/**
	 * THESE Functions implement the forwarding policies
	 * 
	 * */
	
	public static final int FIFO = 0;					//	sent packets from the beginning (as always before)
	public static final int FIFOPriority = 2;		// sent before high priority packets and do not split frames (TS)
	public static final int DTS = 24;

	
	/**
	 * 
	 * Schedule _min packets using 
	 * 
	 * @param _min
	 * @param _mode
	 * @return
	 */
	public SortedSet<Long> schedulePackets(int _min, int _mode)
	{
		switch(_mode)
		{
			case FIFO: 
					return getFIFOPackets(_min);
			case FIFOPriority: 
				return getFIFOPriorityPackets(_min);
			case DTS:
					return getDTS(_min);
			default:
					return getFIFOPackets(_min);
		}
	}
	

		
	/**
	 * Returns a maximum of _min packets using FIFO ordering
	 * @param _min
	 * @return
	 */
	private SortedSet<Long> getFIFOPackets(int _min)
	{
		synchronized(packetStore)
		{
			Set<Long> k = packetStore.keySet();
			SortedSet<Long> ret = new TreeSet<Long>();
			
			
			Iterator<Long> iterator = k.iterator();
			int i =0;
			while(iterator.hasNext() && i < _min) {
				long sn = iterator.next();
				
				if(packetsToSchedule.contains(sn))
				{
					ret.add(sn);
					packetScheduled(sn);
					i++;
				}
			}
			
			
			return ret;
		}
	}
	
	
	/**
	 * 
	 * Schedules packets using FIFO but considering the type of frame (I then P then B)
	 * 
	 * @param _min
	 * @return
	 */
	private SortedSet<Long> getFIFOPriorityPackets(int _min)
	{
		synchronized(packetStore)
		{
			SortedSet<Long> sentPackets = new TreeSet<Long>();
			long frame, gop;
			while(sentPackets.size() < _min && !packetsToSchedule.isEmpty())
			{
				// Select first frame stored with highest priority
				// I
				if(!framesIByGOP.isEmpty())
				{
					gop = framesIByGOP.firstKey();
					frame = framesIByGOP.get(gop);
					
				}else if(!framesPByGOP.isEmpty())
				// P
				{
					gop = framesPByGOP.firstKey();
					frame = framesPByGOP.get(gop).first();
				}
				//B
				else if(!framesPByGOP.isEmpty())
				{
					gop = framesBByGOP.firstKey();
					frame = framesBByGOP.get(gop).first();
					
				}
				else
				{
					frame = frames.firstKey();
					Debugger.dumpErr(this, "All GOP frame structures empty but still packets to schedule?");
				}
				
				// add packets from this frame to sentPackets
				Iterator<Long> packets = frames.get(frame).iterator();
				while(packets.hasNext() && sentPackets.size() < _min)
				{
					Long sn = packets.next();
					sentPackets.add(sn);
					
				}

				// mark packets as scheduled (you can not do it in the previous one...
				Iterator<Long> packets2 = sentPackets.iterator();
				while(packets2.hasNext())
				{
					packetScheduled(packets2.next());
				}

			}
		
			return sentPackets;
	
		}
	}
	
	/**
	 * 
	 * Return packets selected with DTS, a minimum of _min
	 * 
	 * @param _min
	 * @return
	 */
	
	private SortedSet<Long> getDTS(int _min)
	{
		return getDTSSelection(_min);
//		synchronized(packetStore)
//		{
//		SortedSet<Long> sentPackets = new TreeSet<Long>();
//		
//		
//	
//		while(sentPackets.size() < _min && (!packetsToSchedule.isEmpty() || !packetsScheduled.isEmpty()))
//		{
//			// move previously Scheduled
//			while(!packetsScheduled.isEmpty() && sentPackets.size() < _min)
//			{
//				sentPackets.add(packetsScheduled.removeFirst());
//			}
//		
//			// Schedule more if necessary
//			if(sentPackets.size() < _min)
//			{
//				int psN = packetsScheduled.size();
//				packetsScheduled.addAll(getDTSSelection(_min-psN));
//			}
//		}	
//		return sentPackets;
//		}
	}
	
	
	
	/**
	 * 
	 * Auxiliary function to carry out DTS selection
	 * 
	 * @param _min
	 * @return
	 */
	private SortedSet<Long> getDTSSelection(int _min)
	{		
		synchronized(packetStore)
		{
			SortedSet<Long> sentPackets = new TreeSet<Long>();
			if(packetsToSchedule.size() <= _min)
			{
				return getFIFOPackets(_min);
			}
			else if(!frames.isEmpty())	// is there any frame (or packet)
			{
				
				//Define a new window or take the last one
				int window = newWindow();
				
				while(windows.containsKey(window) && sentPackets.size() < _min)
				{
										
					long firstFrame = windows.get(window).firstFrame;
					int size = windows.get(window).size;
					long nFrames;
					// if more than one window, overlap
					if(window > 1 && windows.containsKey(window-1))
					{
						nFrames = Math.round(windows.get(window-1).framesSent*size/windows.get(window-1).size);
					}
					else
					{
						nFrames = _min; // sent the maximum packets... TODO: more than one frame in a packet
					}
						
					
					
					// get the sequence
					int[] seq = getDTSSequence(size);  
					
					
					//	Sent packets according to hopping sequence
					int i=0;
					
					// in not the first window, avoid the first frame, to get a nicer spanning
					if(window > 1)
					{
						i++;
					}
					
					while(i<seq.length && sentPackets.size() < _min && nFrames > 0)
					{
						long selectedFrame = hdvFrame(firstFrame+seq[i]);

						if(frames.containsKey(selectedFrame))
						{
							
		

							//Debugger.dumpMsg(this, "Scheduling frame "+selectedFrame+" packets: "+frames.get(selectedFrame).size() , Debugger.RTP);

							sentPackets.addAll(frames.get(selectedFrame));
							frameScheduled(selectedFrame);
							
							nFrames--;
							windows.get(window).increaseFramesSent();
						}
						i++;
						
						
					}
						
					// if frame rate for the last window, merge
					// Sometimes it happens that a window in the middle could be merged,
					// because we might not have had enough frames to merge the previous ones...
					if(nFrames == 0  && window > 1 && window == windows.lastKey())
					{
						windows.get(window-1).merge(windows.get(window));
						windows.remove(window);
					//	Debugger.dumpMsg(this, "Remove window: "+window, Debugger.RTP);
					}
					
					window--;
				}
					
				
			}
			return sentPackets;
		}
		
	}

	/**
	 * 
	 * Generates the DTS sequence of a given size
	 * 
	 * @param _size
	 * @return
	 */
	private  int[] getDTSSequence(int _size)
	{
		int[] ret = new int[_size];
		boolean[] status = new boolean[_size];
		
		// select two first elements
		
		if(_size > 0)
		{
			ret[0] = 0;
			status[0] = true;
		}
		
		if(_size > 1)
		{
			ret[1] = _size-1;
			status[_size-1] = true;
		}
		
		// select the rest with the sequence formula
		int j = 1;
		int selected = 2;
		int calc;
			
		while(selected < _size)
		{
			for(int i=0;(2*i+1)<j;i++)
			{
				calc = Math.round((2*i+1)*(_size-1)/j);
				if(selected < _size && status[calc] == false)
				{
				
					ret[selected] = calc;
					status[calc] = true;
					//System.out.println("Index: "+selected+" Number: "+ret[selected]);
					selected++;
				}
			
				
			}
			
			j = 2*j;

		}
		return ret;
	}
	
	
	/**
	 * 
	 * Returns the frame in the same GOP of a given frame that is needed to decode this one
	 * 
	 * @param _frame
	 * @return
	 */
	private long hdvFrame(long _frame)
	{

		long selected = _frame;
		if(frames.containsKey(_frame))
		{
			// first packet of the frame
			long sn = frames.get(_frame).iterator().next();
	
			// get gop
			long gop = packetStore.get(sn).getGOP();
			
			// priority
			int priority = packetStore.get(sn).getPriority();
			
			if(priority == Constants.MPEG_FRAME_I)
			{
				selected=_frame;
			}
			
			
			if(priority == Constants.MPEG_FRAME_P)
			{
				
				if(!framesIByGOP.containsKey(gop)) // the first P Frame
				{
					selected=framesPByGOP.get(gop).iterator().next();
				}
				else // the I Frame
				{
					selected=framesIByGOP.get(gop);
				}
			}
			else if(priority == Constants.MPEG_FRAME_B)
			{
				if(!framesIByGOP.containsKey(gop)) // the first P Frame
				{
					if(!framesPByGOP.containsKey(gop))	// B frame
					{
						selected = _frame;
					}
					else	// first P frame
					{
						selected=framesPByGOP.get(gop).iterator().next();
					}
				}
				else // the I Frame
				{
					selected=framesIByGOP.get(gop);
				}
			}
		}	
		return selected;
	}
	
	
	/**
	 * 
	 * Creates a new DTS window
	 * 
	 * @return
	 */
	private int newWindow()
	{
		synchronized (packetStore) {
			if(frames.isEmpty())
			{
				return 0;
			}
			else
			{
				int lastWindow;
				long lastFrame, firstFrame;
				if(windows.isEmpty())
				{
					lastWindow = 0;
					firstFrame = frames.firstKey();
				}
				else
				{
					lastWindow = windows.lastKey();
					firstFrame = windows.get(lastWindow).lastFrame+1;
				}
				
				
				lastFrame = frames.lastKey();


				
				if(firstFrame <= lastFrame - Constants.TS_MIN_WINDOW_SIZE) // minimum TS Window size 
				{
					lastWindow++;
					windows.put(lastWindow, new TSWindow(lastFrame, firstFrame));
				//	Debugger.dumpMsg(this, "Creating window: "+lastWindow+" "+firstFrame+"/"+lastFrame, Debugger.RTP);
				}
				return lastWindow;
			}
		}
	}
	
	
	/**
	 * Structure for DTS Windows
	 * @author sergio
	 *
	 */
	private class TSWindow
	{
		protected int framesSent;
		protected int size;
		protected long lastFrame;
		protected long firstFrame;
		
		protected TSWindow(long _last, long _first)
		{
			framesSent = 0;
			lastFrame = _last;
			firstFrame = _first;
			size = (int) (1+lastFrame-firstFrame);
		}
		
//		protected double getFrameRate()
//		{
//			return framesSent/size;
//		}
		
		protected void increaseFramesSent()
		{
			framesSent++;
		}
		
		protected void merge(TSWindow _ts)
		{
			// edges
			if(_ts.lastFrame > lastFrame)
			{
				lastFrame = _ts.lastFrame;
			}
	
			if(_ts.firstFrame < firstFrame)
			{
				firstFrame = _ts.firstFrame;
			}
			
			framesSent += _ts.framesSent;
			size = (int) (1+lastFrame-firstFrame);
		}
		
	}
	

//	/**
//	 * Returns a maximum of _min packets using FIFO ordering "ensuring" a minimum FPS 
//	 * @param _min
//	 * @return
//	 */
//	
//	private SortedSet<Long> getFIFOMinFPS(int _min)
//	{
//		synchronized(packetsStored)
//		{
//			SortedSet<Long> sentPackets = new TreeSet<Long>();
//
//			// Calculate the increment in frames for the minimum fps
//			int hop = Math.round(Constants.FPS_SOURCE/Constants.FPS_MIN);
//
//			// get starting point
//			long[] allFrames = getStoredFrames();
//			if(allFrames.length > 0)	// if any packets
//			{
//				if(allFrames[allFrames.length-1] <= highestFrameSent) // minimum FPS accomplished, fallback to FIFO by priorities...
//				{
//					return getFIFOPriority(_min);
//				}
//				else
//				{
//					// send the next frame
//					long frameSelected = highestFrameSent + hop;
//					while(frameSelected <= allFrames[allFrames.length-1] && sentPackets.size() < _min)
//					{
//						if(frames.containsKey(frameSelected) && frames.get(frameSelected).size() > 0)	// if the frame exists!
//						{
//							sentPackets.addAll(frames.get(frameSelected));
//							highestFrameSent = frameSelected;
//						}
//						frameSelected +=hop;
//					}
//					
//					
//					// last frame reached... go to FIFOPriority to complete
//					if(sentPackets.size() < _min)
//					{
//						sentPackets.addAll(getFIFOPriority(_min - sentPackets.size()));
//					}
//					return sentPackets;
//					
//				}
//			}
//			else
//			{
//				return sentPackets; // empty
//			}
//		}
//		
//	}



}

