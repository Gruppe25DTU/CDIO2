package socket;

import java.net.Socket;

public class SocketQueue {

	private Element firstElement;
	private Element lastElement;

	private class Element
	{
		Socket socket;
		Element next;

	}

	public void enQueue(Socket newSocket)
	{

	}

	public Socket deQueue()
	{

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
						lastElement = null;
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
