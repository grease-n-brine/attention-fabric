package greasenbrine.fabric.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import greasenbrine.fabric.structures.Point;
import org.junit.jupiter.api.Test;

class SilkTest {

    private static final Point ORIGIN = new Point(new double[]{1.0, 0.0, 0.0});

    private static Silk silk(SilkParameters parameters) {
        return new Silk(1, ORIGIN, parameters);
    }

    @Test
    void surpriseDecreasesUnderConstantInput() {
        Silk unit = silk(SilkParameters.defaults());
        double previous = Double.MAX_VALUE;
        for (int i = 0; i < 20; i++) {
            SilkTick tick = unit.step(1.0);
            assertTrue(tick.rawSurprise() < previous, "surprise must strictly decrease");
            previous = tick.rawSurprise();
        }
        assertTrue(previous < 0.02);
    }

    @Test
    void expectationConvergesToConstantInput() {
        Silk unit = silk(SilkParameters.defaults());
        for (int i = 0; i < 100; i++) {
            unit.step(2.5);
        }
        assertEquals(2.5, unit.getExpectedInput(), 1e-6);
    }

    @Test
    void inputShiftProducesSurpriseSpikeThatDecays() {
        Silk unit = silk(SilkParameters.defaults());
        for (int i = 0; i < 50; i++) {
            unit.step(1.0);
        }
        double settled = unit.step(1.0).rawSurprise();
        double spike = unit.step(3.0).rawSurprise();
        assertTrue(spike > 10 * settled);

        double after = spike;
        for (int i = 0; i < 50; i++) {
            after = unit.step(3.0).rawSurprise();
        }
        assertTrue(after < settled * 2 + 1e-9);
    }

    @Test
    void excitationLeaksWithoutDrive() {
        SilkParameters p = SilkParameters.defaults();
        Silk unit = silk(p);
        unit.step(5.0);
        double peak = unit.getExcitation();
        assertTrue(peak > 0.0);

        unit.seedExpectation(0.0);
        double previous = peak;
        for (int i = 0; i < 10; i++) {
            unit.step(0.0);
            assertTrue(unit.getExcitation() < previous);
            previous = unit.getExcitation();
        }
        assertEquals(peak * Math.pow(1.0 - p.leakRate(), 10), unit.getExcitation(), 1e-9);
    }

    @Test
    void excitationIsClampedToMaximum() {
        SilkParameters p = SilkParameters.defaults().withSurpriseGain(100.0);
        Silk unit = silk(p);
        for (int i = 0; i < 10; i++) {
            unit.step(i % 2 == 0 ? 50.0 : -50.0);
            assertTrue(unit.getExcitation() <= p.maxExcitation());
        }
    }

    @Test
    void baselineTracksExcitationMoreSlowly() {
        Silk unit = silk(SilkParameters.defaults());
        SilkTick tick = unit.step(5.0);
        assertTrue(tick.baseline() < tick.excitation());
        assertTrue(tick.baseline() > 0.0);
    }

    @Test
    void repeatedExposureReducesFiring() {
        SilkParameters p = SilkParameters.defaults().withBaselineRate(0.2).withFiringThreshold(0.3);
        Silk unit = silk(p);
        int earlyFires = 0;
        int lateFires = 0;
        for (int i = 0; i < 200; i++) {
            double input = (i % 10 == 0) ? 4.0 : 0.0;
            boolean fired = unit.step(input).fired();
            if (i < 50 && fired) {
                earlyFires++;
            }
            if (i >= 150 && fired) {
                lateFires++;
            }
        }
        assertTrue(lateFires <= earlyFires, "late=" + lateFires + " early=" + earlyFires);
    }

    @Test
    void outputIsCoordinateConditioned() {
        Silk a = new Silk(1, new Point(new double[]{1.0, 0.0, 0.0}), SilkParameters.defaults());
        Silk b = new Silk(2, new Point(new double[]{-1.0, 0.5, 0.0}), SilkParameters.defaults());
        double ya = a.step(0.7).output();
        double yb = b.step(0.7).output();
        assertEquals(Math.tanh(0.7), ya, 1e-12);
        assertEquals(Math.tanh(-0.7 + 0.5), yb, 1e-12);
        assertTrue(Math.abs(ya) <= 1.0 && Math.abs(yb) <= 1.0);
    }

    @Test
    void outputIsNotSurprise() {
        Silk unit = silk(SilkParameters.defaults());
        SilkTick tick = unit.step(0.3);
        assertEquals(Math.tanh(0.3), tick.output(), 1e-12);
        assertEquals(0.3, tick.rawSurprise(), 1e-12);
    }

    @Test
    void teethAreDirectedAndNotSelfReferential() {
        Silk a = silk(SilkParameters.defaults());
        Silk b = new Silk(2, ORIGIN, SilkParameters.defaults());
        assertTrue(a.attachTooth(b));
        assertFalse(a.attachTooth(b));
        assertFalse(a.attachTooth(a));
        assertEquals(1, a.getTeeth().size());
        assertEquals(0, b.getTeeth().size());
        assertTrue(a.detachTooth(b));
        assertEquals(0, a.getTeeth().size());
    }

    @Test
    void rejectsNonThreeDimensionalPosition() {
        assertThrows(IllegalArgumentException.class,
            () -> new Silk(1, new Point(new double[]{0.0, 0.0}), SilkParameters.defaults()));
    }

    @Test
    void resetRestoresInitialState() {
        Silk unit = silk(SilkParameters.defaults());
        unit.step(3.0);
        unit.step(3.0);
        unit.reset();
        assertEquals(0.0, unit.getExcitation());
        assertEquals(0.0, unit.getBaseline());
        assertEquals(0.0, unit.getExpectedInput());
        assertEquals(0, unit.getTickCount());
        assertTrue(unit.getHistory().isEmpty());
    }
}
