package socket;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

import socket.SocketInMessage.SocketMessageType;

public class SocketController implements ISocketController, ISocketObserver {
	Set<ISocketObserver> observers = new HashSet<ISocketObserver>();
	//TODO Maybe add some way to keep track of multiple connections?
	private BufferedReader inStream;
	private DataOutputStream outStream;


	@Override
	public void registerObserver(ISocketObserver observer) {
		observers.add(observer);
	}

	@Override
	public void unRegisterObserver(ISocketObserver observer) {
		observers.remove(observer);
	}

	@Override
	public void sendMessage(SocketOutMessage message) throws CONNException {
		if (outStream!=null){
			try {
				outStream.writeBytes(message.getMessage());
			} catch (IOException e) {
				throw new CONNException("Problem with connection");
			}
		} else {
			throw new CONNException("Connection is closed");
		}
	}

	@Override
	public void run() {
		//TODO some logic for listening to a socket //(Using try with resources for auto-close of socket)
		try (ServerSocket listeningSocket = new ServerSocket(Port)){ 
			while (true){
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
			new Thread(newConn).start();
			String inLine;
			//.readLine is a blocking call 
			//TODO How do you handle simultaneous input and output on socket?
			//TODO this only allows for one open connection - how would you handle multiple connections?
		}
		
		
		catch (ArrayIndexOutOfBoundsException e)
		{
			try {
				outStream.writeBytes("ES"+'\r'+'\n');
			} catch (IOException e1) {

			}
		}
		catch (IOException e) {
			//TODO maybe notify mainController?
			e.printStackTrace();
		} catch(Exception e)
		{

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

}

