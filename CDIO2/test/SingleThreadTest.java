import controller.MainController;
import org.junit.Before;
import org.junit.Test;
import socket.ClientSocket;
import socket.SocketController;
import socket.SocketInMessage;
import weight.gui.WeightInterfaceControllerGUI;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by ymuslu on 17-03-2017.
 */
public class SingleThreadTest {

    private static SocketController socketController;
    private static WeightInterfaceControllerGUI weightController;
    private static MainController mainCtrl;
    private static boolean initiated = false;
    private static ClientSocket client;

    @Before
    public void init() {
        if (initiated) {
            return;
        }
        initiated = true;
        socketController = new SocketController();
        weightController = new WeightInterfaceControllerGUI();
        //Injecting socket and uiController into mainController - Replace with improved versions...
        mainCtrl = new MainController(socketController, weightController);
        //.init and .start could be merged
        mainCtrl.start();
        while(weightController.fxApp == null) {
            try {
                sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            client = new ClientSocket(new Socket("127.0.0.1", 8000), socketController);
            client.notifyObservers(new SocketInMessage(SocketInMessage.SocketMessageType.K, "4"));
            assertEquals("Response after K 4", "K A", ClientSocket.output.trim());
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testCommands() {
        String[] commands = {"B ", "S", "T", "P111 ", "D ", "DW", "RM20 8 "};
        String quit = "Q";
        double weight = Math.random() * 2;
        String primDisplay = "\"Test\"";
        String secDisplay = "\"Second Test\"";
        String pRequest = "\"Indtast nr\"";
        double pResponse = 755;


    }

    @Test
    public void testB() {
        double weight = Math.random() * 2;
        String weightStr = weight + "";
        while (weightStr.length() < 5) {
            weightStr += "0";
        }
        client.notifyObservers(new SocketInMessage(SocketInMessage.SocketMessageType.B, weightStr));
        assertEquals("Response after B", "DB", ClientSocket.output.trim());
        assertEquals(weight, weightController.getBruttoWeight(), 0.001);
    }

    @Test
    public void testS() {
        double weight = weightController.getNettoWeight();
        client.notifyObservers(new SocketInMessage(SocketInMessage.SocketMessageType.S, ""));
        String response = ClientSocket.output.trim();
        String[] split = response.split(" ");
        double replyWeight = Double.valueOf(split[7]);
        assertEquals("Pre-Weight", "S S", response.substring(0, 3));
        assertEquals(weight, replyWeight, 0.001);
        assertEquals("Post-Weight", "kg", split[8]);
    }
}
