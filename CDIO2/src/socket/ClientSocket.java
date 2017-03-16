package socket;

import java.io.*;
import java.net.Socket;

import exception.*;
import socket.SocketInMessage.SocketMessageType;

public class ClientSocket implements IClientSocket {

	private ISocketController controller;
	private Socket inConn;
	private PrintWriter outStream;
	private BufferedReader inStream;
	private boolean active;

	public ClientSocket(Socket inConn, ISocketController controller) {
		this.controller = controller;
		this.inConn = inConn;
		try 
		{
			outStream = new PrintWriter(inConn.getOutputStream(), true);
			inStream = new BufferedReader(new InputStreamReader(inConn.getInputStream()));
			active = false;
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	@Override
	public void run() 
	{
		try
		{
			String inLine = "";
			while(!inConn.isClosed() && !inConn.isInputShutdown())
			{
				inLine = inStream.readLine();
				//Remove leading chars which are not a-zA-Z
				while (inLine.charAt(0) < 65 || inLine.charAt(0) > 122) {
					inLine = inLine.substring(1);
				}
				System.out.println(inLine);
				if (inLine==null) break;
				if (inLine.trim().equals("")) continue;
				handleInput(inLine);
				try
				{
					Thread.currentThread().sleep(100);
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
			controller.unregisterClientSocket(this);
			inConn.close();
		}
		catch(IOException e)
		{

		}
	}

	public void handleInput(String inLine)
	{
		try
		{
			//TODO: Validate input
			switch (inLine.split(" ")[0])
			{
				case "RM20": // Display a message in the secondary display and wait for response
					String rMsg = "";
					boolean correct = false;
					if(inLine.charAt(7)!='\"')
						throw new IllegalCommandException();
					for(int i = 8; i<38;i++)
						if(inLine.charAt(i)=='\"')
						{
							correct = true;
							rMsg = inLine.substring(8,i);
							break;
						}
					if(correct)
						notifyObservers(new SocketInMessage(SocketMessageType.RM208,rMsg));
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
					controller.unregisterClientSocket(this);
					inConn.close();
					break;
				default: //Something went wrong?
					throw new IllegalCommandException();
			}
		}
		catch(IOException e)
		{

		}
		catch(ArrayIndexOutOfBoundsException | StringIndexOutOfBoundsException | IllegalCommandException e)
		{
			outStream.write("ES\r\n");
			outStream.flush();
		}

	}

	private void notifyObservers(SocketInMessage message) {
		controller.notify(message);
	}

	public Socket getInConn() {
		return inConn;
	}

	public void setInConn(Socket inConn) {
		this.inConn = inConn;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String toString()
	{
		String result = "ClientSocket active: "+active+" Socket: "+inConn;
		return result;
	}

	@Override
	public void sendMessage(SocketOutMessage message)
	{
		outStream.write(message.getMessage() + "\r\n");
		outStream.flush();
	}

}
