package weight.gui;

import java.util.ArrayList;
import java.util.Arrays;

import weight.IWeightInterfaceController;
import weight.IWeightInterfaceObserver;
import weight.KeyPress;

public class WeightInterfaceControllerGUI implements IWeightInterfaceController {
	private static WeightInterfaceControllerGUI instance;
	private ArrayList<IWeightInterfaceObserver> observers = new ArrayList<IWeightInterfaceObserver>(); 
	public FxApp fxApp;

	public double getBruttoWeight() {
		return bruttoWeight;
	}

	public void setBruttoWeight(double bruttoWeight) {
		this.bruttoWeight = bruttoWeight;
	}

	public double getNettoWeight() {
		return bruttoWeight - taraWeight;
	}

	public double getTaraWeight() {
		return taraWeight;
	}

	public void setTaraWeight(double taraWeight) {
		this.taraWeight = taraWeight;
	}

	public String getRM20_MSG() {
		return RM20_MSG;
	}

	public void setRM20_MSG(String RM20_MSG) {
		this.RM20_MSG = RM20_MSG;
	}

	public boolean isRM20_EXPECTING() {
		return RM20_EXPECTING;
	}

	public void setRM20_EXPECTING(boolean RM20_EXPECTING) {
		this.RM20_EXPECTING = RM20_EXPECTING;
	}

	private double bruttoWeight;
	private double nettoWeight;
	private double taraWeight;
	private String RM20_MSG = "";
	private boolean RM20_EXPECTING = false;

	public WeightInterfaceControllerGUI() { instance = this; }
	@Override public void run() { FxApp.go(); }
	public static WeightInterfaceControllerGUI getInstance() { return instance; }
	public void setApp(FxApp fxApp) { this.fxApp = fxApp; fxApp.setSim(this); }
	

	// System --> GUI
	// Methods required by interface
	@Override
	public void registerObserver(IWeightInterfaceObserver uiObserver) {
		this.observers.add(uiObserver);
	}
	@Override
	public void unRegisterObserver(IWeightInterfaceObserver uiObserver) {
		this.observers.remove(uiObserver);
	}
	@Override
	public void showMessagePrimaryDisplay(String string) {
		fxApp.printLoad(string);
	}
	@Override
	public void showMessageSecondaryDisplay(String string) {
		fxApp.printBottom(string);
	}
	@Override
	public void changeInputType(InputType type) {
		switch(type){
		case LOWER: fxApp.setButtonsLower(); break;
		case NUMBERS: fxApp.setButtonsNumbers(); break;
		case UPPER: fxApp.setButtonsUpper(); break;
		default: fxApp.setButtonsLower(); break;
		}
		
	}
	@Override
	public void setSoftButtonTexts(String[] texts) {
		int firstSoftkey = 0;
		if (texts == null) {
			texts = new String[0];
		}
		boolean[] sftkeysChecked = new boolean[texts.length];
		Arrays.fill(sftkeysChecked, false);
		fxApp.softkeysShow(texts, firstSoftkey, sftkeysChecked);
	}
	
	
	// GUI --> System
	public void onSliderValueChange(Double newValue) {
		for (IWeightInterfaceObserver o : observers) {
			o.notifyWeightChange(newValue / 1000);
		}
	}
	public void onExitButtonPressed() {
		for (IWeightInterfaceObserver o : observers) {
			o.notifyKeyPress(KeyPress.Exit());
		}
	}
	public void onZeroButtonPressed() {
		for (IWeightInterfaceObserver o : observers) {
			o.notifyKeyPress(KeyPress.Zero());
		}
	}
	public void onTaraButtonPressed() {
		for (IWeightInterfaceObserver o : observers) {
			o.notifyKeyPress(KeyPress.Tara());
		}
		
	}
	public void onSendButtonPressed() {
		for (IWeightInterfaceObserver o : observers) {
			o.notifyKeyPress(KeyPress.Send());
		}
	}
	public void onNumBtnPressed(char btn){
		for (IWeightInterfaceObserver o : observers) {
			o.notifyKeyPress(KeyPress.Character(btn));
		}
	}
	public void onSoftBtnPressed(int i) {
		for (IWeightInterfaceObserver o : observers) {
			o.notifyKeyPress(KeyPress.SoftButton(i));
		}
	}
	
	
	
	
	
	
	


	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
