package greasenbrine.fabric.learning;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import org.junit.jupiter.api.Test;

class LinearReadoutTest {

    @Test
    void initialPredictionIsZero() {
        LinearReadout r = new LinearReadout(3, 0.1);
        assertEquals(0.0, r.predict(new double[]{1.0, 2.0, 3.0}));
    }

    @Test
    void learnsLinearTarget() {
        Random rng = new Random(1L);
        LinearReadout r = new LinearReadout(2, 0.05);
        for (int i = 0; i < 5000; i++) {
            double a = rng.nextDouble() * 2 - 1;
            double b = rng.nextDouble() * 2 - 1;
            r.update(new double[]{a, b}, 3.0 * a - 2.0 * b + 0.5);
        }
        assertEquals(3.0, r.getWeights()[0], 1e-3);
        assertEquals(-2.0, r.getWeights()[1], 1e-3);
        assertEquals(0.5, r.getBias(), 1e-3);
    }

    @Test
    void updateReturnsPredictionBeforeAdjustment() {
        LinearReadout r = new LinearReadout(1, 0.5);
        double before = r.update(new double[]{1.0}, 1.0);
        assertEquals(0.0, before);
        assertTrue(r.predict(new double[]{1.0}) > 0.0);
    }

    @Test
    void rejectsMismatchedFeatureLength() {
        LinearReadout r = new LinearReadout(2, 0.1);
        assertThrows(IllegalArgumentException.class, () -> r.predict(new double[]{1.0}));
    }

    @Test
    void rejectsInvalidConstruction() {
        assertThrows(IllegalArgumentException.class, () -> new LinearReadout(0, 0.1));
        assertThrows(IllegalArgumentException.class, () -> new LinearReadout(1, 0.0));
    }
}
