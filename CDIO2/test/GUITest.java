import controller.MainController;
import org.junit.Before;
import org.junit.Test;
import socket.SocketController;
import weight.gui.WeightInterfaceControllerGUI;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;

/**
 * Created by ymuslu on 17-03-2017.
 */
public class GUITest {

    private static SocketController socketHandler;
    private static WeightInterfaceControllerGUI weightController;
    private static MainController mainCtrl;
    private static boolean initiated = false;

    @Before
    public void init() {
        if (initiated) {
            return;
        }
        initiated = true;
        socketHandler = new SocketController();
        weightController = new WeightInterfaceControllerGUI();
        //Injecting socket and uiController into mainController - Replace with improved versions...
        mainCtrl = new MainController(socketHandler, weightController);
        //.init and .start could be merged
        mainCtrl.start();
        while(weightController.fxApp == null) {
            try {
                sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testSetWeight() {
        double value = Math.random()*2000;
        double delta = 0.001; //Error allowed due to rounding
        weightController.onSliderValueChange(value);
        assertEquals(value / 1000, weightController.getBruttoWeight(), delta);
    }

    @Test
    public void testTara() {
        double value = Math.random()*2;
        double delta = 0.001; //Error allowed due to rounding
        weightController.setBruttoWeight(value); //Set an initial weight
        weightController.onTaraButtonPressed(); //Push Tara on GUI
        assertEquals(value, weightController.getBruttoWeight(), delta);
        assertEquals(value, weightController.getTaraWeight(), 0.0);
        assertEquals(0.0, weightController.getNettoWeight(), 0.0);
    }

    @Test
    public void testZero() {
        weightController.onZeroButtonPressed();
        assertEquals(0.0, weightController.getBruttoWeight(), 0.0);
        assertEquals(0.0, weightController.getNettoWeight(), 0.0);
        assertEquals(0.0, weightController.getTaraWeight(), 0.0);
    }
}
