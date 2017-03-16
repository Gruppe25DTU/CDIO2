package socket;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class SocketController implements ISocketController, ISocketObserver {
	private Set<ISocketObserver> observers = new HashSet<>();
	private Set<ClientSocket> socketSet = new HashSet<>();

	@Override
	public void registerObserver(ISocketObserver observer) {
		observers.add(observer);
	}

	@Override
	public void unRegisterObserver(ISocketObserver observer) {
		observers.remove(observer);
	}

	//TODO: Warn MainController if no connection?
	@Override
	public void sendMessage(SocketOutMessage message) throws CDIOException {
		for (ClientSocket socket : socketSet) {
			socket.sendMessage(message);
		}
	}

	@Override
	public void run() {
		//TODO some logic for listening to a socket //(Using try with resources for auto-close of socket)
		try (ServerSocket listeningSocket = new ServerSocket(Port))
		{ 
			while (true)
			{
				Socket newSocket = listeningSocket.accept();
				ClientSocket socket = new ClientSocket(newSocket, this); //Blocking call
				socketSet.add(socket);
				new Thread(socket).start();
			}		
		} 
		catch (IOException e1) 
		{
			// TODO Maybe notify MainController?
			e1.printStackTrace();
		} 


	}

	@Override
	public void unregisterClientSocket(IClientSocket socket) {
		socketSet.remove(socket);
	}


	private void notifyObservers(SocketInMessage message) {
		for (ISocketObserver socketObserver : observers) {
			socketObserver.notify(message);
		}
	}

	@Override
	synchronized public void notify(SocketInMessage message) {
		notifyObservers(message);
	}
}

