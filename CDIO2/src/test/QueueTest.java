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
	}

}
