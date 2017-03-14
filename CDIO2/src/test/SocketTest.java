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

public class SocketTest {

	public static void main(String[] args)
	{
		ISocketController socketHandler = new SocketController();
		IWeightInterfaceController weightController = new WeightInterfaceControllerGUI();
		//Injecting socket and uiController into mainController - Replace with improved versions...
		IMainController mainCtrl = new MainController(socketHandler, weightController);
		//.init and .start could be merged
		mainCtrl.start();
		
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
