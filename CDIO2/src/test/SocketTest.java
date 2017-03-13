package test;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Scanner;

import socket.ISocketController;
import socket.SocketController;

public class SocketTest {

	public static void main(String[] args)
	{
		ISocketController socketHandler = new SocketController();
		Thread ts = new Thread(socketHandler);
		System.out.println(ts.getName()+" "+Thread.currentThread().getName()+" "+Thread.activeCount());
		ts.start();
		
		try {
			Socket clientSock = new Socket("127.0.0.1",8000);
			BufferedReader in = new BufferedReader(new InputStreamReader(clientSock.getInputStream()));
			DataOutputStream out = new DataOutputStream(clientSock.getOutputStream());
			Scanner keyb = new Scanner(System.in);
			while(true)
			{
				System.out.println("Is Connected: "+clientSock.isConnected());
				out.writeBytes(keyb.nextLine()+'\r'+'\n');
				System.out.println(in.readLine());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
