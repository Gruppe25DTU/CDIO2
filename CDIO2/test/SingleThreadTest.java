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

    private static boolean RM20s = false;
    private static final int COMMAND_TEST_N = 200;


    @Test
    public void commandsTest() {
        Runnable[] testList = {
                this::testP111,
                this::testRM20,
                this::testDW,
                this::testRM20Answer,
                this::testB,
                this::testD,
                this::testS,
                this::testT,
        };
        for (int i = 0; i < COMMAND_TEST_N; i++) {
            int rand = (int)(Math.random() * testList.length);
            testList[rand].run();
        }
    }

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


    public void testT() {
        double weight = Math.random() * 2;
        weightController.setBruttoWeight(weight);
        String weightStr = weight + "";
        while (weightStr.length() < 5) {
            weightStr += "0";
        }
        String expected = "T S      ";
        client.notifyObservers(new SocketInMessage(SocketInMessage.SocketMessageType.T, ""));
        String response = SocketController.output.trim();
        response = response.substring(0, response.length() - 8);


        assertEquals("Reponse after T:", expected, response);
        assertEquals(weight, weightController.getTaraWeight(), 0.01);
        assertEquals(0.0, weightController.getNettoWeight(), 0.0);
    }


    public void testD() {
        try {
            String msg = "Hello, World!";
            //Ensure display shows something different from msg
            weightController.showMessagePrimaryDisplay("");
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
            assertNotEquals("Should not show weight!", weightStr, display);
            client.notifyObservers(new SocketInMessage(SocketInMessage.SocketMessageType.DW, ""));
            String response = SocketController.output.trim();
            assertEquals("Reply after DW", "DW A", response);
            sleep(200);
            weight = weightController.getNettoWeight();
            double displayDouble = Double.valueOf(weightController.fxApp.txtload.getText().split(" ")[0].trim());
            assertEquals(weight, displayDouble, 0.01);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }
    }


    public void testP111() {
        try {
            String msg = "Hello, World!";
            weightController.showMessageSecondaryDisplay("");
            sleep(100);
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


    public void testRM20Answer() {
        try {
            String initialOutput = SocketController.output.trim();
            String initialMsg = weightController.fxApp.txtbottom.getText().trim();
            String msg = "TestRM20";
            for (char c : msg.toCharArray()) {
                weightController.onNumBtnPressed(c);
            }
            sleep(200);
            String expected = RM20s ? msg : initialMsg;
            String display = weightController.fxApp.txtbottom.getText().trim();
            assertEquals("Display after entering", expected, display);

            expected = RM20s ? "RM20 A \"" + display + "\"" : initialOutput;
            weightController.onSendButtonPressed();
            String outgoing = SocketController.output.trim();
            assertEquals("Reply after Send " + (RM20s ? "with" : "without") + " RM20 waiting", expected, outgoing);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        } finally {
            if (RM20s) {
                RM20s = false;
                try {
                    sleep(200);
                } catch (InterruptedException e) {
                    fail();
                }
            }
        }
    }


    public void testRM20() {
        try {
            RM20s = true;
            String msg = "GUESS A NUMBER!";
            weightController.showMessageSecondaryDisplay("");
            sleep(200);
            String display = weightController.fxApp.txtbottom.getText().trim();
            assertNotEquals("Should not display msg", msg, display);

            client.notifyObservers(new SocketInMessage(SocketInMessage.SocketMessageType.RM208, msg));
            String response = SocketController.output.trim();
            assertEquals("Reply after RM20 8", "RM20 B", response);

            sleep(100);
            display = weightController.fxApp.txtbottom.getText().trim();
            assertEquals("Should now show msg", msg, display);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }
    }
}
