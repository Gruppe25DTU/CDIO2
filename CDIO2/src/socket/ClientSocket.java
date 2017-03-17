package socket;

import java.io.*;
import java.net.Socket;
import java.util.ConcurrentModificationException;

import exception.*;
import socket.SocketInMessage.SocketMessageType;

public class ClientSocket implements IClientSocket {

	private IClientSocketController controller;
	private Socket inConn;
	private PrintWriter outStream;
	private BufferedReader inStream;
	public static String output;

	public ClientSocket(Socket inConn, IClientSocketController controller) {
		this.controller = controller;
		this.inConn = inConn;
		try 
		{
			outStream = new PrintWriter(inConn.getOutputStream(), true);
			inStream = new BufferedReader(new InputStreamReader(inConn.getInputStream()));
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	@Override
	public void run() 
	{
		String inLine = "";
		while(!inConn.isClosed() && !inConn.isInputShutdown())
		{
			try {
				inLine = inStream.readLine();
				if (Thread.currentThread().isInterrupted()) {
					throw new InterruptedException();
				}
				if (inLine == null) break;
				//Remove leading chars which are not a-zA-Z
				while (inLine.length() > 0 && (inLine.charAt(0) < 65 || inLine.charAt(0) > 122)) {
					inLine = inLine.substring(1);
				}
				System.out.println(inLine);
				if (inLine.trim().equals("")) continue;
				handleInput(inLine);
				Thread.sleep(100);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
				break;
			}
			catch (IOException e) {
				if (inConn.isClosed()) {
					//Connection closed while waiting for input
					break;
				}
				e.printStackTrace();
				//Try to reestablish the stream
				try {
					inStream = new BufferedReader(new InputStreamReader(inConn.getInputStream()));
				} catch (IOException e1) {
					e1.printStackTrace();
					break;
				}
			}
		}
		//Ensure proper closure
		close();
	}

	private void handleInput(String inLine)
	{
		try
		{
			switch (inLine.split(" ")[0])
			{
				case "RM20": // Display a message in the secondary display and wait for response
					String[] rSplits = inLine.split("\"");
					if (rSplits.length > 2 && rSplits[0] == "RM20 8 ") {
						String rMsg = rSplits[1];
						notifyObservers(new SocketInMessage(SocketMessageType.RM208,rMsg));
					}
					else
						throw new IllegalCommandException();
					break;
				case "D":// Display a message in the primary display
					notifyObservers(new SocketInMessage(SocketMessageType.D, inLine.substring(3,inLine.length()-1)));
					break;
				case "DW": //Clear primary display
					notifyObservers(new SocketInMessage(SocketMessageType.DW,""));
					break;
				case "P111": //Show something in secondary display
					String[] pSplits = inLine.split("\"");
					if (pSplits.length == 2 && pSplits[0].equals("P111 ")) {
						String pMsg = pSplits[1];
						notifyObservers(new SocketInMessage(SocketMessageType.P111,pMsg));
						break;
					}
					throw new IllegalCommandException();
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
					close();
					break;
				default: //Something went wrong?
					throw new IllegalCommandException();
			}
		}
		catch(ArrayIndexOutOfBoundsException | StringIndexOutOfBoundsException | IllegalCommandException e)
		{
			sendMessage(new SocketOutMessage("ES\r\n"));
		}

	}

	public void close() {
		try {
			controller.unRegisterClientSocket(this);
		} catch (ConcurrentModificationException e) {
			e.printStackTrace();
			//Something is iterating over client sockets and closing them
			//Must be controller closing all sockets
			//Controller is responsible for removing clients via its iterator
		}
		try {
			inConn.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void notifyObservers(SocketInMessage message) {
		controller.notify(message);
	}

	@Override
	public void sendMessage(SocketOutMessage message)
	{
		output = message.getMessage() + "\r\n";
		outStream.write(output);
		outStream.flush();
	}

}
