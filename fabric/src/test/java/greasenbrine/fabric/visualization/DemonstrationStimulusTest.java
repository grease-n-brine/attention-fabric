package greasenbrine.fabric.visualization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class DemonstrationStimulusTest {

    @Test
    void pulseScheduleMatchesSpecification() {
        assertTrue(DemonstrationStimulus.isPulse(DemonstrationStimulus.ISOLATED_PULSE_TICK));
        assertTrue(DemonstrationStimulus.isPulse(DemonstrationStimulus.TRAIN_START));
        assertTrue(DemonstrationStimulus.isPulse(DemonstrationStimulus.TRAIN_END));
        assertFalse(DemonstrationStimulus.isPulse(DemonstrationStimulus.TRAIN_START + 1));
        assertFalse(DemonstrationStimulus.isPulse(0));
        assertFalse(DemonstrationStimulus.isPulse(DemonstrationStimulus.TRAIN_END + DemonstrationStimulus.TRAIN_INTERVAL));

        DemonstrationStimulus s = new DemonstrationStimulus(0L);
        int count = 0;
        for (int t = 0; t < s.length(); t++) {
            if (s.pulse(t)) {
                count++;
            }
        }
        int expected = 1 + (DemonstrationStimulus.TRAIN_END - DemonstrationStimulus.TRAIN_START)
            / DemonstrationStimulus.TRAIN_INTERVAL + 1;
        assertEquals(expected, count);
    }

    @Test
    void pulsesRaiseSignal() {
        DemonstrationStimulus s = new DemonstrationStimulus(1L);
        int t = DemonstrationStimulus.ISOLATED_PULSE_TICK;
        double base = Math.sin(2.0 * Math.PI * t / DemonstrationStimulus.PERIOD);
        assertTrue(s.value(t) > base + DemonstrationStimulus.PULSE - 0.5);
    }

    @Test
    void stimulusIsDeterministic() {
        DemonstrationStimulus a = new DemonstrationStimulus(7L);
        DemonstrationStimulus b = new DemonstrationStimulus(7L);
        for (int t = 0; t < a.length(); t++) {
            assertEquals(a.value(t), b.value(t));
        }
    }

    @Test
    void windowIsLeftPaddedAndEndsAtCurrentTick() {
        DemonstrationStimulus s = new DemonstrationStimulus(0L);
        FrameRenderer.SignalWindow w = s.window(5, 10);
        assertEquals(10, w.values().length);
        for (int i = 0; i < 4; i++) {
            assertEquals(0.0, w.values()[i]);
            assertFalse(w.pulses()[i]);
        }
        assertEquals(s.value(5), w.values()[9]);
        assertEquals(s.value(0), w.values()[4]);
    }
}
