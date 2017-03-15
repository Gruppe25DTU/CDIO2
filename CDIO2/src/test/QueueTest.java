package test;

import java.net.Socket;

import socket.ClientSocket;
import socket.SocketQueue;

public class QueueTest {

	public static void main(String[] args)
	{
		SocketQueue q = SocketQueue.getInstance();
		System.out.println(q);
		q.enQueue(new ClientSocket(new Socket()));
		System.out.println(q);
		q.enQueue(new ClientSocket(new Socket()));
		System.out.println(q);
		ClientSocket cl = new ClientSocket(new Socket());
		q.enQueue(cl);
		q.enQueue(new ClientSocket(new Socket()));
		System.out.println(q);
		q.remove(cl);
		System.out.println(q);
		cl = new ClientSocket(new Socket());
		q.enQueue(cl);
		System.out.println(q);
		System.out.println(q.deQueue());
		q.remove(cl);
		System.out.println(q);
		q.deQueue();
		q.deQueue();
		System.out.println(q);
		
	}

}
