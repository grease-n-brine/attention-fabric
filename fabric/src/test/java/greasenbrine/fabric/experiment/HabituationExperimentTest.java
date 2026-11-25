package greasenbrine.fabric.experiment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import greasenbrine.fabric.core.SilkParameters;
import greasenbrine.fabric.experiment.HabituationExperiment.Condition;
import greasenbrine.fabric.experiment.HabituationExperiment.Stream;
import org.junit.jupiter.api.Test;

class HabituationExperimentTest {

    @Test
    void streamContainsExpectedNumberOfPulses() {
        Stream rare = HabituationExperiment.generate(Condition.RARE, 0L);
        Stream frequent = HabituationExperiment.generate(Condition.FREQUENT, 0L);
        int expectedRare = 1 + (HabituationExperiment.TOTAL_TICKS - 1 - HabituationExperiment.WARMUP_TICKS)
            / HabituationExperiment.RARE_INTERVAL;
        int expectedFrequent = 1 + (HabituationExperiment.TOTAL_TICKS - 1 - HabituationExperiment.WARMUP_TICKS)
            / HabituationExperiment.FREQUENT_INTERVAL;
        assertEquals(expectedRare, count(rare.anomaly()));
        assertEquals(expectedFrequent, count(frequent.anomaly()));
        for (int t = 0; t < HabituationExperiment.WARMUP_TICKS; t++) {
            assertTrue(!rare.anomaly()[t]);
        }
    }

    @Test
    void streamIsDeterministicForSeed() {
        Stream a = HabituationExperiment.generate(Condition.RARE, 3L);
        Stream b = HabituationExperiment.generate(Condition.RARE, 3L);
        for (int t = 0; t < a.values().length; t++) {
            assertEquals(a.values()[t], b.values()[t]);
        }
    }

    @Test
    void pulsesRaiseSignalByPulseAmplitude() {
        Stream s = HabituationExperiment.generate(Condition.RARE, 1L);
        int t = HabituationExperiment.WARMUP_TICKS;
        double expectedBase = Math.sin(2.0 * Math.PI * t / HabituationExperiment.PERIOD);
        assertTrue(s.values()[t] > expectedBase + HabituationExperiment.PULSE - 0.5);
    }

    @Test
    void unitRespondsToRarePulses() {
        var r = HabituationExperiment.runUnit(
            HabituationExperiment.generate(Condition.RARE, 0L), SilkParameters.defaults());
        assertTrue(r.earlyFireRate() > 0.5, "early=" + r.earlyFireRate());
        assertTrue(r.earlyDeviation() > 0.0);
    }

    @Test
    void networkRunCompletesWithinCapacity() {
        var r = HabituationExperiment.runNetwork(
            HabituationExperiment.generate(Condition.FREQUENT, 0L), SilkParameters.defaults(), 0L);
        assertTrue(r.finalActive() >= 1 && r.finalActive() <= HabituationExperiment.FABRIC_CAPACITY);
        assertEquals(HabituationExperiment.TOTAL_TICKS / HabituationExperiment.SAMPLE_EVERY, r.activeSamples().length);
    }

    private static int count(boolean[] flags) {
        int n = 0;
        for (boolean f : flags) {
            if (f) {
                n++;
            }
        }
        return n;
    }
}
