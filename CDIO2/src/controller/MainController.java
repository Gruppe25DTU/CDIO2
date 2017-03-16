package controller;


import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import socket.*;
import weight.IWeightInterfaceController;
import weight.IWeightInterfaceObserver;
import weight.KeyPress;
/**
 * MainController - integrating input from socket and ui. Implements ISocketObserver and IUIObserver to handle this.
 * @author Christian Budtz
 * @version 0.1 2017-01-24
 *
 */
public class MainController implements IMainController, ISocketObserver, IWeightInterfaceObserver {

	private ISocketController socketHandler;
	private IWeightInterfaceController weightController;
	private KeyState keyState = KeyState.K1;
	private double bruttoWeight;
	private double nettoWeight;
	private double taraWeight;
	private String RM20_MSG;
	private boolean RM20_EXPECTING;

	public MainController(ISocketController socketHandler, IWeightInterfaceController weightInterfaceController) {
		this.init(socketHandler, weightInterfaceController);
		RM20_EXPECTING = false;
		RM20_MSG = "";
	}

	@Override
	public void init(ISocketController socketHandler, IWeightInterfaceController weightInterfaceController) {
		this.socketHandler = socketHandler;
		this.weightController=weightInterfaceController;
	}

	@Override
	public void start() {
		if (socketHandler!=null && weightController!=null){
			socketHandler.registerObserver(this);
			weightController.registerObserver(this);
			new Thread(weightController).start();
			new Thread(socketHandler).start();
		} else {
			System.err.println("No controllers injected!");
		}
	}

	//Listening for socket input
	@Override
	public void notify(SocketInMessage message) {
		try{
			DecimalFormat d = new DecimalFormat();
			DecimalFormatSymbols dec = new DecimalFormatSymbols();
			dec.setDecimalSeparator('.');
			d.setDecimalFormatSymbols(dec);
			d.applyPattern("0.000");
			switch (message.getType()) {
			case B:
				weightController.showMessagePrimaryDisplay(message.getMessage() + " kg");
				break;
			case D:
				weightController.showMessagePrimaryDisplay(message.getMessage()); 
				break;
			case Q:
				close();
				break;
			case RM204:
				break;
			case RM208:
				weightController.showMessageSecondaryDisplay(message.getMessage());
				socketHandler.sendMessage(new SocketOutMessage("RM20 B"));
				RM20_EXPECTING = true;
				break;
			case S:
				String msg = "S S      "+d.format(nettoWeight)+" kg";
				socketHandler.sendMessage(new SocketOutMessage(msg));
				break;
			case T:
				taraWeight = bruttoWeight-taraWeight;
				nettoWeight = 0;
				weightController.showMessagePrimaryDisplay("0.00 kg");
				String tMsg = "T S      "+d.format(taraWeight)+" kg";
				socketHandler.sendMessage(new SocketOutMessage(tMsg));
				break;
			case DW:
				//TODO: Unable to retrieve and display weight
				socketHandler.sendMessage(new SocketOutMessage("DW A"));
				weightController.showMessagePrimaryDisplay("");
				break;
			case K:
				handleKMessage(message);
				break;
			case P111:
				weightController.showMessageSecondaryDisplay(message.getMessage());
				break;
			}
		}
		catch(CONNException e)
		{
			e.printStackTrace();
		}


	}

	private void close() {
		//TODO
	}

	private void handleKMessage(SocketInMessage message) {
		switch (message.getMessage()) {
		case "1" :
			this.keyState = KeyState.K1;
			break;
		case "2" :
			this.keyState = KeyState.K2;
			break;
		case "3" :
			this.keyState = KeyState.K3;
			break;
		case "4" :
			this.keyState = KeyState.K4;
			break;
		default:
			try {
				socketHandler.sendMessage(new SocketOutMessage("ES"));
			} catch (CONNException e) {
				e.printStackTrace();
			}
			break;
		}
	}
	//Listening for UI input
	@Override
	public void notifyKeyPress(KeyPress keyPress) {
		//TODO implement logic for handling input from ui
		try {
			switch (keyPress.getType()) {
			case SOFTBUTTON:
				break;
			case TARA:
				if (keyState.equals(KeyState.K4) || keyState.equals(KeyState.K3))
				{
					socketHandler.sendMessage(new SocketOutMessage("T"));
				}	
				break;
			case TEXT:
				if(RM20_EXPECTING && RM20_MSG.length()<31)
					RM20_MSG+=keyPress.getCharacter();
				weightController.showMessageSecondaryDisplay(RM20_MSG);
				break;
			case ZERO:
				if (keyState.equals(KeyState.K4) || keyState.equals(KeyState.K3))
				{
					socketHandler.sendMessage(new SocketOutMessage("B 0.000"));
					socketHandler.sendMessage(new SocketOutMessage("T"));
				}	
				break;
			case C:
				break;
			case EXIT:
				if (keyState.equals(KeyState.K4) || keyState.equals(KeyState.K3))
					socketHandler.sendMessage(new SocketOutMessage("Q"));
				close();
				System.exit(0);
				break;
			case SEND:
				if (keyState.equals(KeyState.K4) || keyState.equals(KeyState.K3)) 
					socketHandler.sendMessage(new SocketOutMessage("K A 3"));
				if(RM20_EXPECTING)
				{
					String msg = "RM20 A \"" + RM20_MSG+'\"';
					socketHandler.sendMessage(new SocketOutMessage(msg));
					RM20_EXPECTING = false;
					weightController.showMessageSecondaryDisplay("");
				}
				
				break;
			}
		} catch (CONNException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void notifyWeightChange(double newWeight) {
		bruttoWeight = newWeight;
		nettoWeight = newWeight-taraWeight;
		weightController.showMessagePrimaryDisplay(nettoWeight + " kg");

	}

}
