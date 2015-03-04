package overlayManager;

public class Session {
	private String source;
	private String destination;
	private String sessionID;
	private int priority;
	
	public Session(String _source, String _destination, String _sessionID, int _priority)
	{
		setSource(_source);
		setDestination(_destination);
		setSessionID(_sessionID);
		setPriority(_priority);
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getSource() {
		return source;
	}
	public void setDestination(String destination) {
		this.destination = destination;
	}
	public String getDestination() {
		return destination;
	}
	public void setSessionID(String sessionID) {
		this.sessionID = sessionID;
	}
	public String getSessionID() {
		return sessionID;
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}
	public int getPriority() {
		return priority;
	}
}
