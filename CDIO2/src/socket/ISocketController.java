package socket;

public interface ISocketController extends Runnable{
	public final static int Port = 8000;

	void closeAllClients();
	void registerObserver(ISocketObserver observer);
	void unRegisterObserver(ISocketObserver observer);
	void sendMessage(SocketOutMessage message);

}
