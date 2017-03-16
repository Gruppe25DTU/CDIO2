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
	private DecimalFormat d;

	public MainController(ISocketController socketHandler, IWeightInterfaceController weightInterfaceController) {
		this.init(socketHandler, weightInterfaceController);
	}

	@Override
	public void init(ISocketController socketHandler, IWeightInterfaceController weightInterfaceController) {
		this.socketHandler = socketHandler;
		this.weightController=weightInterfaceController;
		d = new DecimalFormat();
		DecimalFormatSymbols dec = new DecimalFormatSymbols();
		dec.setDecimalSeparator('.');
		d.setDecimalFormatSymbols(dec);
		d.applyPattern("0.000");
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
			switch (message.getType()) {
			case B:
				double weight = Double.valueOf(message.getMessage());
				updateWeight(weight);
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
				weightController.setRM20_EXPECTING(true);
				break;
			case S:
				String msg = "S S      "+d.format(weightController.getNettoWeight())+"kg";
				socketHandler.sendMessage(new SocketOutMessage(msg));
				break;
			case T:
				weight = weightController.getBruttoWeight();
				weightController.setTaraWeight(weight);
				updateWeight(weight);
				String tMsg = "T S      "+d.format(weightController.getTaraWeight())+" kg";
				socketHandler.sendMessage(new SocketOutMessage(tMsg));
				break;
			case DW:
				weight = weightController.getBruttoWeight();
				updateWeight(weight);
				socketHandler.sendMessage(new SocketOutMessage("DW A"));
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

	private void updateWeight(double weight) {
		weightController.setBruttoWeight(weight);
		weightController.showMessagePrimaryDisplay(d.format(weightController.getNettoWeight()) + "kg");
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
		try {
			switch (keyPress.getType()) {
			case SOFTBUTTON:
				break;
			case TARA:
				double weight = weightController.getBruttoWeight();
				weightController.setTaraWeight(weight);
				updateWeight(weight);
				break;
			case TEXT:
				if(weightController.isRM20_EXPECTING() && weightController.getRM20_MSG().length()<31)
					weightController.setRM20_MSG(weightController.getRM20_MSG()+keyPress.getCharacter());
				weightController.showMessageSecondaryDisplay(weightController.getRM20_MSG());
				break;
			case ZERO:
				weightController.setTaraWeight(0.0);
				updateWeight(0.0);
				break;
			case C:
				break;
			case EXIT:
				socketHandler.sendMessage(new SocketOutMessage("Q"));
				close();
				System.exit(0);
				break;
			case SEND:
				socketHandler.sendMessage(new SocketOutMessage("K A 3"));
				if(weightController.isRM20_EXPECTING())
				{
					String msg = "RM20 A \"" + weightController.getRM20_MSG()+'\"';
					socketHandler.sendMessage(new SocketOutMessage(msg));
					weightController.setRM20_EXPECTING(false);
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
		updateWeight(newWeight);
	}

}
