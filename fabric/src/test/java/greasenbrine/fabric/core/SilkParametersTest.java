package greasenbrine.fabric.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class SilkParametersTest {

    @Test
    void defaultsAreValid() {
        SilkParameters p = SilkParameters.defaults();
        assertEquals(0.2, p.expectationRate());
        assertEquals(0.1, p.leakRate());
    }

    @Test
    void rejectsRatesOutsideUnitInterval() {
        assertThrows(IllegalArgumentException.class, () -> SilkParameters.defaults().withExpectationRate(1.5));
        assertThrows(IllegalArgumentException.class, () -> SilkParameters.defaults().withLeakRate(-0.1));
        assertThrows(IllegalArgumentException.class, () -> SilkParameters.defaults().withBaselineRate(2.0));
    }

    @Test
    void rejectsNegativeGains() {
        assertThrows(IllegalArgumentException.class, () -> SilkParameters.defaults().withSurpriseGain(-1.0));
        assertThrows(IllegalArgumentException.class, () -> SilkParameters.defaults().withFiringThreshold(-1.0));
    }

    @Test
    void rejectsNonPositiveMaxExcitation() {
        assertThrows(IllegalArgumentException.class,
            () -> new SilkParameters(0.1, 0.1, 0.0, 0.1, 0.1, 0.1, 0.1, 0.0));
    }

    @Test
    void withersPreserveOtherFields() {
        SilkParameters p = SilkParameters.defaults().withFiringThreshold(0.9);
        assertEquals(0.9, p.firingThreshold());
        assertEquals(SilkParameters.defaults().leakRate(), p.leakRate());
        assertEquals(SilkParameters.defaults().maxExcitation(), p.maxExcitation());
    }
}
