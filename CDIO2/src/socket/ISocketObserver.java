package socket;

public interface ISocketObserver {
	void notify(SocketInMessage message);
	void close();
}
