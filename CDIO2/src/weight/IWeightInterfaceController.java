package weight;

public interface IWeightInterfaceController extends Runnable {
	
	void registerObserver(IWeightInterfaceObserver uiObserver);
	void unRegisterObserver(IWeightInterfaceObserver uiObserver);
	
	void showMessagePrimaryDisplay(String string);
	void showMessageSecondaryDisplay(String string);
	void changeInputType(InputType type);
	void setSoftButtonTexts(String[] texts);

	double getBruttoWeight();
	void setBruttoWeight(double bruttoWeight);
	double getNettoWeight();
	double getTaraWeight();
	void setTaraWeight(double taraWeight);
	String getRM20_MSG();
	void setRM20_MSG(String RM20_MSG);
	boolean isRM20_EXPECTING();
	void setRM20_EXPECTING(boolean RM20_EXPECTING);
	
	public enum InputType {
		UPPER, LOWER, NUMBERS
	}

}
