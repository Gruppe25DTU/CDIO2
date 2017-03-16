package socket;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

import socket.SocketInMessage.SocketMessageType;

public class ClientSocket implements ISocketController {

	private Set<ISocketObserver> obsv = new HashSet<ISocketObserver>();
	private Socket inConn;
	private DataOutputStream outStream;
	private BufferedReader inStream;
	private boolean active;

	public ClientSocket(Socket inConn, ISocketObserver observer) {
		this.inConn = inConn;
		try 
		{
			outStream = new DataOutputStream(inConn.getOutputStream());
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
			while(!inConn.isClosed())
			{
				while(active && !inConn.isInputShutdown())
				{
					String inLine;
					inLine = inStream.readLine();
					System.out.println(inLine);
					if (inLine==null) continue;
					handleInput(inLine);

				}
				try 
				{
					Thread.currentThread().sleep(100);
				} 
				catch (InterruptedException e) {
					e.printStackTrace();
				}
				inStream.readLine();
			}
			SocketQueue.getInstance().remove(this);
		}
		catch(IOException e)
		{

		}




	}

	private void handleInput(String inLine)
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
				active = false;
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
			try {
				outStream.writeBytes("ES"+'\r'+'\n');
			} catch (IOException e1) {
				e1.printStackTrace();
			}
        }

    }

	@Override
	public void registerObserver(ISocketObserver observer) {
		obsv.add(observer);

	}

	@Override
	public void unRegisterObserver(ISocketObserver observer) {
		obsv.remove(observer);

	}

	@Override
	public void sendMessage(SocketOutMessage message)
	{
		try 
		{
			outStream.writeBytes(message.getMessage()+'\r'+'\n');

		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}


	private void notifyObservers(SocketInMessage message) {
		for(ISocketObserver ob : obsv)
		{
			ob.notify(message);
		}

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

}
