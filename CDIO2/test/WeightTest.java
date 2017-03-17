import controller.MainController;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import socket.ClientSocket;
import socket.SocketController;
import socket.SocketInMessage;
import weight.gui.WeightInterfaceControllerGUI;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.Socket;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by ymuslu on 17-03-2017.
 */
public class WeightTest {


    static SocketController socketController;
    static WeightInterfaceControllerGUI weightController;
    static MainController mainCtrl;
    static boolean initiated = false;
    static ClientSocket client;

    @Before
    public void init() {
        if (!initiated) {
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
        }
        try {
            client = new ClientSocket(new Socket("127.0.0.1", 8000), socketController);
            client.notifyObservers(new SocketInMessage(SocketInMessage.SocketMessageType.K, "4"));
            assertEquals("Response after K 4", "K A", SocketController.output.trim());
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    @After
    public void close() throws InterruptedException {
        socketController.closeAllClients();
    }
}
