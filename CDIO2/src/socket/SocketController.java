package socket;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import socket.SocketInMessage.SocketMessageType;

public class SocketController implements ISocketController, ISocketObserver {
	private Set<ISocketObserver> observers = new HashSet<ISocketObserver>();
	private SocketQueue queue;
	private ClientSocket activeSocket;
	private Thread qA;

	@Override
	public void registerObserver(ISocketObserver observer) {
		observers.add(observer);
	}

	@Override
	public void unRegisterObserver(ISocketObserver observer) {
		observers.remove(observer);
	}
	
	public SocketController()
	{
		queue = SocketQueue.getInstance();
		qA = new QueueAgent(this);
		qA.start();
	}

	@Override
	public void sendMessage(SocketOutMessage message) throws CONNException {
		if(activeSocket!=null)
			activeSocket.sendMessage(message);
		queue.multiCast(message);
	}

	@Override
	public void run() {
		//TODO some logic for listening to a socket //(Using try with resources for auto-close of socket)
		try (ServerSocket listeningSocket = new ServerSocket(Port))
		{ 
			while (true)
			{
				waitForConnections(listeningSocket); 	
			}		
		} 
		catch (IOException e1) 
		{
			// TODO Maybe notify MainController?
			e1.printStackTrace();
		} 


	}

	private void waitForConnections(ServerSocket listeningSocket) {
		try {
			Socket activeSocket = listeningSocket.accept(); //Blocking call
			ClientSocket newConn = new ClientSocket(activeSocket);
			newConn.registerObserver(this);
			queue.enQueue(newConn);
			new Thread(newConn).start();

		}
		catch (IOException e) {
			//TODO maybe notify mainController?
			e.printStackTrace();
		}
	}




	private void notifyObservers(SocketInMessage message) {
		for (ISocketObserver socketObserver : observers) {
			socketObserver.notify(message);
		}
	}

	@Override
	synchronized public void notify(SocketInMessage message) {
		qA.interrupt();
		notifyObservers(message);
	}

	public ClientSocket getActiveSocket()
	{
		return activeSocket;
	}
	
	public void setActiveSocket(ClientSocket newSocket)
	{
		this.activeSocket = newSocket;
	}
}

