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
			throw new CONNException("Connections is closed");
		}
	}

	@Override
	public void run() {
		//TODO some logic for listening to a socket //(Using try with resources for auto-close of socket)
		try (ServerSocket listeningSocket = new ServerSocket(Port)){ 
			while (true){
				waitForConnections(listeningSocket); 	
			}		
		} catch (IOException e1) {
			// TODO Maybe notify MainController?
			e1.printStackTrace();
		} 


	}

	private void waitForConnections(ServerSocket listeningSocket) {
		try {
			Socket activeSocket = listeningSocket.accept(); //Blocking call
			inStream = new BufferedReader(new InputStreamReader(activeSocket.getInputStream()));
			outStream = new DataOutputStream(activeSocket.getOutputStream());
			String inLine;
			//.readLine is a blocking call 
			//TODO How do you handle simultaneous input and output on socket?
			//TODO this only allows for one open connection - how would you handle multiple connections?
			while (true){
				System.out.println("Server is connected: "+activeSocket.isConnected());
				inLine = inStream.readLine();
				System.out.println(inLine);
				if (inLine==null) break;
				switch (inLine.split(" ")[0]) {
				case "RM20": // Display a message in the secondary display and wait for response
					//TODO implement logic for RM command
					break;
				case "D":// Display a message in the primary display
					notifyObservers(new SocketInMessage(SocketMessageType.D, inLine.split(" ")[1])); 			
					break;
				case "DW": //Clear primary display
					notifyObservers(new SocketInMessage(SocketMessageType.DW,""));
					break;
				case "P111": //Show something in secondary display
					String msg = null;
					if(inLine.indexOf('\"')==5)
					{
						for(int i = 6; i<inLine.length();i++)
						{
							if(inLine.charAt(i)=='\"')
							{
								msg = inLine.substring(5, i);
								break;
							}
						}
						if(msg == null)
							outStream.writeBytes("ES"+'\r'+'\n');
						else
							notifyObservers(new SocketInMessage(SocketMessageType.P111,msg));
					}
					
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

}

