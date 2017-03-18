package socket;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class SocketController implements ISocketController, IClientSocketController {
	private Set<ISocketObserver> observers = Collections.synchronizedSet(new HashSet<>());
	private Set<IClientSocket> clientSockets = Collections.synchronizedSet(new HashSet<>());
	public static String output;
	private static int port = 8000;

	
	public SocketController(int port)
	{
		SocketController.port = port;
	}
	
	@Override
	public void registerObserver(ISocketObserver observer) {
		observers.add(observer);
	}

	@Override
	public void unRegisterObserver(ISocketObserver observer) {
		observers.remove(observer);
	}

	@Override
	public void sendMessage(SocketOutMessage message) {
		output = message.getMessage().trim();
		for (IClientSocket socket : clientSockets) {
			socket.sendMessage(message);
		}
	}

	@Override
	public void run() {
		try (ServerSocket listeningSocket = new ServerSocket(port))
		{
			while (true)
			{
				Socket newSocket = listeningSocket.accept();
				ClientSocket socket = new ClientSocket(newSocket, this); //Blocking call
				registerClientSocket(socket);
				new Thread(socket).start();
			}		
		} 
		catch (IOException e1) 
		{
			e1.printStackTrace();
			if (clientSockets.size() == 0) {
				//Unable to listen and no children exists - might as well die
				for (ISocketObserver observer : observers) {
					observer.close();
				}
			}
		} 


	}

	@Override
	public void closeAllClients() {
		if (clientSockets.size() > 0) {
			Iterator<IClientSocket> it = clientSockets.iterator();
			while(it.hasNext()) {
				IClientSocket client = it.next();
				it.remove();
				client.close();
			}
		}
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

	@Override
	synchronized public void registerClientSocket(IClientSocket socket) {
		clientSockets.add(socket);
	}

	@Override
	synchronized public void unRegisterClientSocket(IClientSocket socket) {
		clientSockets.remove(socket);
	}




}

