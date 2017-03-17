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

	private ISocketController socketController;
	private IWeightInterfaceController weightController;
	private KeyState keyState = KeyState.K1;
	private DecimalFormat d;

	public MainController(ISocketController socketController, IWeightInterfaceController weightInterfaceController) {
		this.init(socketController, weightInterfaceController);
        d = new DecimalFormat();
        DecimalFormatSymbols dec = new DecimalFormatSymbols();
        dec.setDecimalSeparator('.');
        d.setDecimalFormatSymbols(dec);
        d.applyPattern("0.000");
	}

	@Override
	public void init(ISocketController socketHandler, IWeightInterfaceController weightInterfaceController) {
		this.socketController = socketHandler;
		this.weightController=weightInterfaceController;
	}

	@Override
	public void start() {
		if (socketController !=null && weightController!=null){
			socketController.registerObserver(this);
			weightController.registerObserver(this);
			new Thread(weightController).start();
			new Thread(socketController).start();
		} else {
			System.err.println("No controllers injected!");
		}
	}

	//Listening for socket input
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
				socketController.sendMessage(new SocketOutMessage("D A"));
			}
			break;
		case Q:
			close();
			break;
		case RM208:
			if (keyState.equals(KeyState.K1) || keyState.equals(KeyState.K4)) {
				weightController.showMessageSecondaryDisplay(message.getMessage());
				weightController.setRM20_EXPECTING(true);
			}
			if (keyState.equals(KeyState.K3) || keyState.equals(KeyState.K4)) {
				socketController.sendMessage(new SocketOutMessage("RM20 B"));
			}
			break;
		case S:
			if (keyState.equals(KeyState.K3) || keyState.equals(KeyState.K4)) {
				String msg = "S S      "+d.format(weightController.getNettoWeight())+" kg";
				socketController.sendMessage(new SocketOutMessage(msg));
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
				socketController.sendMessage(new SocketOutMessage("DW A"));
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
				socketController.sendMessage(new SocketOutMessage("P111 A"));
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
			socketController.sendMessage(new SocketOutMessage("ES"));
			return;
		}
		if (keyState.equals(KeyState.K3) || keyState.equals(KeyState.K4)) {
			socketController.sendMessage(new SocketOutMessage("K A"));
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
				if (weightController.isRM20_EXPECTING()) {
					if (weightController.getRM20_MSG().length() < 31) {
						weightController.setRM20_MSG(weightController.getRM20_MSG() + keyPress.getCharacter());
					}
					weightController.showMessageSecondaryDisplay(weightController.getRM20_MSG());
				}
			}
			break;
		case ZERO:
			updateWeight(0.0);
			performTara();
			break;
		case C:
			break;
		case EXIT:
			close();
			break;
		case SEND:
			if(weightController.isRM20_EXPECTING()) {
				if (keyState.equals(KeyState.K3) || keyState.equals(KeyState.K4)) {
					String msg = "RM20 A \"" + weightController.getRM20_MSG()+'\"';
					socketController.sendMessage(new SocketOutMessage(msg));
				}
				if (keyState.equals(KeyState.K1) || keyState.equals(KeyState.K4)) {
					weightController.setRM20_EXPECTING(false);
					weightController.setRM20_MSG("");
					weightController.showMessageSecondaryDisplay("");
				}
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
			socketController.sendMessage(new SocketOutMessage(tMsg));
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
			socketController.sendMessage(new SocketOutMessage("DB"));
		}
	}

	@Override
	public void close() {
		if (keyState.equals(KeyState.K3) || keyState.equals(KeyState.K4)) {
			socketController.sendMessage(new SocketOutMessage("Q"));
		}
		if (keyState.equals(KeyState.K1) || keyState.equals(KeyState.K4)) {
			socketController.closeAllClients();
			System.exit(0);
		}
	}

	@Override
	public void notifyWeightChange(double newWeight) {
		updateWeight(newWeight);
	}

}
