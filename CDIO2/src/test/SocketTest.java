package test;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Scanner;

import controller.IMainController;
import controller.MainController;
import socket.ISocketController;
import socket.SocketController;
import weight.IWeightInterfaceController;
import weight.gui.WeightInterfaceControllerGUI;

public class SocketTest{

	public static void main(String[] args)
	{
		ISocketController socketHandler = new SocketController();
		IWeightInterfaceController weightController = new WeightInterfaceControllerGUI();
		IMainController mainCtrl = new MainController(socketHandler, weightController);

		mainCtrl.start();
		
		try {
			Socket clientSock = new Socket("127.0.0.1",8000);
			BufferedReader in = new BufferedReader(new InputStreamReader(clientSock.getInputStream()));
			DataOutputStream out = new DataOutputStream(clientSock.getOutputStream());
			Socket sock2 = new Socket("127.0.0.1",8000);
			Socket sock3 = new Socket("127.0.0.1",8000);
			Socket sock4 = new Socket("127.0.0.1",8000);
			Scanner keyb = new Scanner(System.in);
			new InStream(clientSock).start();
			while(!clientSock.isClosed())
			{
				System.out.println("Is Connected: "+clientSock.isConnected());
				out.writeBytes(keyb.nextLine()+'\r'+'\n');
				
			}
		} catch (IOException e) 
		{
			e.printStackTrace();
		}

	}
	

}
