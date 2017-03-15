package socket;

import java.net.Socket;

public class SocketQueue {

	private static SocketQueue INSTANCE;
	private Element firstElement;
	private Element lastElement;
	private int numOfElem  = 0;

	private SocketQueue()
	{
		
	}
	private class Element
	{
		ClientSocket socket;
		Element next;

	}

	public void enQueue(ClientSocket newSocket)
	{
		numOfElem++;
		Element newEl = new Element();
		newEl.socket = newSocket;
		if(isEmpty())
		{
			firstElement = lastElement = newEl;
		}
		else
		{
			lastElement.next = newEl;
			lastElement = newEl;
		}
		
	}

	public ClientSocket deQueue()
	{
		if(!isEmpty())
		{
			numOfElem--;
			Element first = firstElement;
			firstElement = firstElement.next;
			first.socket.setActive(true);
			return first.socket;
		}
		return null;
	}

	public void remove(Socket socket)
	{
		if(!isEmpty())
		{
			numOfElem--;
			Element last = firstElement;
			if(last.next==null)
			{
				if(last.socket.equals(socket))
					firstElement=null;
				return;
			}
			Element current = last.next;
			while(current!=null)
			{
				if(current.socket.equals(socket))
				{
					if(current.equals(lastElement))
					{
						lastElement = null;
						lastElement = last;
					}		
					last.next = current.next;
					break;
				}
				else
				{
					last = current;
					current = current.next;
				}
			}

		}
	}

	public void multiCast(SocketOutMessage message)
	{
		Element current = firstElement;
		while(current!=null)
		{
			current.socket.sendMessage(message);
			current = current.next;
		}
	}
	public boolean isEmpty()
	{
		return firstElement==null;
	}
	
	public static SocketQueue getInstance()
	{
		if(INSTANCE==null)
			INSTANCE = new SocketQueue();
		return INSTANCE;
	}
	
	public String toString()
	{
		Element current = firstElement;
		String result = "";
		while(current!=null)
		{
			result += current.socket.toString()+'\r';
			current = current.next;
		}
		return result;
	}

}
