package utils;

public interface MessageReceivedEventListener {
	public void receiveMessage(int _messageType, String _parentAddr, byte[] _message);
}
