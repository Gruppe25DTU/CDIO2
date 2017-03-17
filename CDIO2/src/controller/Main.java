package controller;

import socket.SocketController;
import weight.IWeightInterfaceController;
import weight.gui.WeightInterfaceControllerGUI;
import socket.ISocketController;
/**
 * Simple class to fire up application and inject implementations
 * @author Christian
 *
 */
public class Main {
	private static boolean gui= true;

	public static void main(String[] args) {
		int port = 8000; // default værdi
		try
		{
			if(args.length>0)
			{
				port = Integer.parseInt(args[0]);
				if(port < 1 || port > 65536)
				{
					System.out.println("Invalid port number: switching to default port: 8000");
					port = 8000;
				}
			}
			
			
		}
		catch(NumberFormatException e)
		{
			//not a number
		}
		ISocketController socketHandler = new SocketController(port);
		IWeightInterfaceController weightController = new WeightInterfaceControllerGUI();
		//Injecting socket and uiController into mainController - Replace with improved versions...
		IMainController mainCtrl = new MainController(socketHandler, weightController);
		//.init and .start could be merged
		mainCtrl.start();
		
	}
}
