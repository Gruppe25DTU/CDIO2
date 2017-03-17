import org.junit.Test;
import socket.SocketController;
import socket.SocketInMessage;
import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

/**
 * Created by ymuslu on 17-03-2017.
 */
public class SingleThreadTest extends WeightTest {

    @Test
    public void testB() {
        double weight = Math.random() * 2;
        String weightStr = weight + "";
        while (weightStr.length() < 5) {
            weightStr += "0";
        }
        client.notifyObservers(new SocketInMessage(SocketInMessage.SocketMessageType.B, weightStr));
        assertEquals("Response after B", "DB", SocketController.output.trim());
        assertEquals(weight, weightController.getBruttoWeight(), 0.001);
    }

    @Test
    public void testS() {
        double weight = weightController.getNettoWeight();
        client.notifyObservers(new SocketInMessage(SocketInMessage.SocketMessageType.S, ""));
        String response = SocketController.output.trim();
        String[] split = response.split(" ");
        double replyWeight = Double.valueOf(split[7]);
        assertEquals("Pre-Weight", "S S", response.substring(0, 3));
        assertEquals(weight, replyWeight, 0.001);
        assertEquals("Post-Weight", "kg", split[8]);
    }

    @Test
    public void testT() {
        double weight = Math.random() * 2;
        weightController.setBruttoWeight(weight);
        String weightStr = weight + "";
        while (weightStr.length() < 5) {
            weightStr += "0";
        }
        String expected = "T S      " + weightStr.substring(0, 4);
        client.notifyObservers(new SocketInMessage(SocketInMessage.SocketMessageType.T, ""));
        String response = SocketController.output.trim();
        response = response.substring(0, response.length() - 4);
        assertEquals("Reponse after T:", expected, response);
        assertEquals(weight, weightController.getTaraWeight(), 0.001);
        assertEquals(0.0, weightController.getNettoWeight(), 0.0);
    }

    @Test
    public void testD() {
        try {
            String msg = "Hello, World!";
            //Ensure display shows something different from msg
            client.notifyObservers(new SocketInMessage(SocketInMessage.SocketMessageType.B, "2.94"));
            sleep(200);
            String display = weightController.fxApp.txtload.getText().trim();
            assertNotEquals("Display should NOT show Hello World", msg, display);
            client.notifyObservers(new SocketInMessage(SocketInMessage.SocketMessageType.D, msg));
            String response = SocketController.output;
            assertEquals("Reply after D", "D A", response.trim());
            sleep(200);
            display = weightController.fxApp.txtload.getText().trim();
            msg = msg.substring(0, 7).trim();
            assertEquals("Display should now show " + msg, msg, display);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testDW() {
        try {
            double weight = Math.random() * 2;
            String weightStr = weight + "";
            while (weightStr.length() < 5) {
                weightStr += "0";
            }
            weightStr = weightStr.substring(0, 5) + " kg";
            weightController.setBruttoWeight(weight);
            client.notifyObservers(new SocketInMessage(SocketInMessage.SocketMessageType.D, "HELLO"));
            sleep(200);
            String display = weightController.fxApp.txtload.getText().trim();
            assertNotEquals("Should not should weight!", weightStr, display);
            client.notifyObservers(new SocketInMessage(SocketInMessage.SocketMessageType.DW, ""));
            String response = SocketController.output.trim();
            assertEquals("Reply after DW", "DW A", response);
            sleep(200);
            double displayDouble = Double.valueOf(weightController.fxApp.txtload.getText().split(" ")[0].trim());
            assertEquals(weight, displayDouble, 0.001);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testP111() {
        try {
            String msg = "Hello, World!";
            String display = weightController.fxApp.txtbottom.getText().trim();
            assertNotEquals("Should not display Hello World", msg, display);
            client.notifyObservers(new SocketInMessage(SocketInMessage.SocketMessageType.P111, msg));
            String response = SocketController.output.trim();
            assertEquals("Reply after P111", "P111 A", response);
            sleep(200);
            display = weightController.fxApp.txtbottom.getText().trim();
            assertEquals("Should display Hello World!", msg, display);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testRM20() {

    }
}
