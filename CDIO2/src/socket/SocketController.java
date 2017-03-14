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

public class SocketController implements ISocketController {
	Set<ISocketObserver> observers = new HashSet<ISocketObserver>();
	//TODO Maybe add some way to keep track of multiple connections?
	private BufferedReader inStream;
	private DataOutputStream outStream;

	class EchoThread extends Thread{
		Socket socket;
		public EchoThread(Socket socket) {
			this.socket = socket;

		}

		@Override
		public void run() {
			try {
				inStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				outStream = new DataOutputStream(socket.getOutputStream());
				String inLine = inStream.readLine();
				handleConnections(inLine);
			}
			catch (Exception e) {

			}
		}
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
			new Thread(new EchoThread(activeSocket)).run();
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


	private void handleConnections(String inLine) {
		try {
			while (true){
				if (inLine==null) continue;
				switch (inLine.split(" ")[0]) {
				case "RM20": // Display a message in the secondary display and wait for response
					String rMsg = inLine.substring(5,inLine.length());
					notifyObservers(new SocketInMessage(SocketMessageType.RM208,rMsg));
					break;
				case "D":// Display a message in the primary display
					notifyObservers(new SocketInMessage(SocketMessageType.D, inLine.substring(3,inLine.length()))); 			
					break;
				case "DW": //Clear primary display
					notifyObservers(new SocketInMessage(SocketMessageType.DW,""));
					break;
				case "P111": //Show something in secondary display
					String pMsg = inLine.substring(5,inLine.length());
					notifyObservers(new SocketInMessage(SocketMessageType.P111,pMsg));
					break;
				case "T": // Tare the weight
					notifyObservers(new SocketInMessage(SocketMessageType.T,""));
					break;
				case "S": // Request the current load
					notifyObservers(new SocketInMessage(SocketMessageType.S,""));
					break;
				case "K":
					notifyObservers(new SocketInMessage(SocketMessageType.K, inLine.split(" ")[1]));
					break;
				case "B": // Set the load
					notifyObservers(new SocketInMessage(SocketMessageType.B,inLine.split(" ")[1]));
					break;
				case "Q": // Quit
					notifyObservers(new SocketInMessage(SocketMessageType.Q,""));
					break;
				default: //Something went wrong?

					outStream.writeBytes("ES"+'\r'+'\n');

					break;

				}
			}
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


	private void notifyObservers(SocketInMessage message) {
		for (ISocketObserver socketObserver : observers) {
			socketObserver.notify(message);
		}
	}

}

