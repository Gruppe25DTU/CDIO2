package socket;

import java.net.Socket;

public class SocketQueue {

	private Element firstElement;
	private Element lastElement;
	private int numOfElem  = 0;

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

	public boolean isEmpty()
	{
		return firstElement==null;
	}

}
