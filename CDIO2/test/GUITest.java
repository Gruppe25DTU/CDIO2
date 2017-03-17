import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Created by ymuslu on 17-03-2017.
 */
public class GUITest extends WeightTest {

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
