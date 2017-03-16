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
        d = new DecimalFormat();
        DecimalFormatSymbols dec = new DecimalFormatSymbols();
        dec.setDecimalSeparator('.');
        d.setDecimalFormatSymbols(dec);
        d.applyPattern("0.000");
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
    //TODO: Check KeyState!
	@Override
	public void notify(SocketInMessage message) {
		switch (message.getType()) {
		case B:
			double weight = Double.valueOf(message.getMessage());
			updateWeight(weight);
			break;
		case D:
			if (keyState.equals(KeyState.K1) || keyState.equals(KeyState.K4)) {
				weightController.showMessagePrimaryDisplay(message.getMessage());
			}
			if (keyState.equals(KeyState.K3) || keyState.equals(KeyState.K4)) {
				socketHandler.sendMessage(new SocketOutMessage("D A"));
			}
			break;
		case Q:
			if (keyState.equals(KeyState.K1) || keyState.equals(KeyState.K4)) {
				closeConnections();
			}
			break;
		case RM208:
			if (keyState.equals(KeyState.K1) || keyState.equals(KeyState.K4)) {
				weightController.showMessageSecondaryDisplay(message.getMessage());
				weightController.setRM20_EXPECTING(true);
			}
			if (keyState.equals(KeyState.K3) || keyState.equals(KeyState.K4)) {
				socketHandler.sendMessage(new SocketOutMessage("RM20 B"));
			}
			break;
		case S:
			if (keyState.equals(KeyState.K3) || keyState.equals(KeyState.K4)) {
				String msg = "S S      "+d.format(weightController.getNettoWeight())+" kg";
				socketHandler.sendMessage(new SocketOutMessage(msg));
			}
			break;
		case T:
			performTara();
			break;
		case DW:
			if (keyState.equals(KeyState.K1) || keyState.equals(KeyState.K4)) {
				weight = weightController.getBruttoWeight();
				updateWeight(weight);
			}
			if (keyState.equals(KeyState.K3) || keyState.equals(KeyState.K4)) {
				socketHandler.sendMessage(new SocketOutMessage("DW A"));
			}
			break;
		case K:
			handleKMessage(message);
			break;
		case P111:
			if (keyState.equals(KeyState.K1) || keyState.equals(KeyState.K4)) {
				weightController.showMessageSecondaryDisplay(message.getMessage());
			}
			if (keyState.equals(KeyState.K3) || keyState.equals(KeyState.K4)) {
				socketHandler.sendMessage(new SocketOutMessage("P111 A"));
			}
			break;
		}
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
			socketHandler.sendMessage(new SocketOutMessage("ES"));
			return;
		}
		if (keyState.equals(KeyState.K3) || keyState.equals(KeyState.K4)) {
			socketHandler.sendMessage(new SocketOutMessage("K A"));
		}
	}

	//Listening for UI input
	@Override
	public void notifyKeyPress(KeyPress keyPress) {
		switch (keyPress.getType()) {
		case SOFTBUTTON:
			break;
		case TARA:
			performTara();
			break;
		case TEXT:
			if (keyState.equals(KeyState.K1) || keyState.equals(KeyState.K4)) {
				if(weightController.isRM20_EXPECTING() && weightController.getRM20_MSG().length()<31)
                    weightController.setRM20_MSG(weightController.getRM20_MSG()+keyPress.getCharacter());
				weightController.showMessageSecondaryDisplay(weightController.getRM20_MSG());
			}
			break;
		case ZERO:
			updateWeight(0.0);
			performTara();
			break;
		case C:
			break;
		case EXIT:
			if (keyState.equals(KeyState.K1) || keyState.equals(KeyState.K4)) {
				closeConnections();
				System.exit(0);
			}
			if (keyState.equals(KeyState.K3) || keyState.equals(KeyState.K4)) {
				socketHandler.sendMessage(new SocketOutMessage("Q"));
			}
			break;
		case SEND:
			String msg = "RM20 A \"" + weightController.getRM20_MSG()+'\"';
			if (keyState.equals(KeyState.K1) || keyState.equals(KeyState.K4)) {
				if(weightController.isRM20_EXPECTING())
                {
                    weightController.setRM20_EXPECTING(false);
                    weightController.showMessageSecondaryDisplay("");
                }
			}
			if (keyState.equals(KeyState.K3) || keyState.equals(KeyState.K4)) {
				socketHandler.sendMessage(new SocketOutMessage("K A 3"));
				socketHandler.sendMessage(new SocketOutMessage(msg));
			}
			break;
		}
	}

	private void performTara() {
		double weight;
		if (keyState.equals(KeyState.K1) || keyState.equals(KeyState.K4)) {
			weight = weightController.getBruttoWeight();
			weightController.setTaraWeight(weight);
			updateWeight(weight);
		}
		if (keyState.equals(KeyState.K3) || keyState.equals(KeyState.K4)) {
			String tMsg = "T S      "+d.format(weightController.getTaraWeight())+" kg";
			socketHandler.sendMessage(new SocketOutMessage(tMsg));
		}
	}

	private void updateWeight(double weight) {
		String formattedWeight;
		if (keyState.equals(KeyState.K1) || keyState.equals(KeyState.K4)) {
			weightController.setBruttoWeight(weight);
			formattedWeight = d.format(weightController.getNettoWeight());
			if (formattedWeight.length() > 4) formattedWeight = formattedWeight.substring(0,5);
			weightController.showMessagePrimaryDisplay(formattedWeight + " kg");
		}
		if (keyState.equals(KeyState.K3) || keyState.equals(KeyState.K4)) {
			socketHandler.sendMessage(new SocketOutMessage("DB"));
		}
	}

	//TODO: Close all sockets (any streams?)
	private void closeConnections() {

	}

	@Override
	public void notifyWeightChange(double newWeight) {
		updateWeight(newWeight);
	}

}
