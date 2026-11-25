package greasenbrine.fabric.experiment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import greasenbrine.fabric.core.SilkParameters;
import greasenbrine.fabric.core.SilkTick;
import java.util.List;
import org.junit.jupiter.api.Test;

class SingleUnitExperimentTest {

    @Test
    void traceHasThreeSegments() {
        List<SilkTick> trace = SingleUnitExperiment.run(SilkParameters.defaults());
        assertEquals(3 * SingleUnitExperiment.SEGMENT_LENGTH, trace.size());
        assertEquals(1.0, trace.get(0).input());
        assertEquals(3.0, trace.get(SingleUnitExperiment.SEGMENT_LENGTH).input());
        assertEquals(1.0, trace.get(2 * SingleUnitExperiment.SEGMENT_LENGTH).input());
    }

    @Test
    void summaryReflectsSpikeAndRecovery() {
        SingleUnitExperiment.Summary s =
            SingleUnitExperiment.summarize(SingleUnitExperiment.run(SilkParameters.defaults()));
        assertTrue(s.spikeSurprise() > s.settledSurprise() * 10);
        assertTrue(s.ticksToRecoverTenPercent() > 0);
        assertTrue(s.ticksToRecoverTenPercent() < SingleUnitExperiment.SEGMENT_LENGTH);
        assertTrue(s.peakExcitation() >= s.settledExcitation());
    }

    @Test
    void experimentIsDeterministic() {
        List<SilkTick> a = SingleUnitExperiment.run(SilkParameters.defaults());
        List<SilkTick> b = SingleUnitExperiment.run(SilkParameters.defaults());
        for (int i = 0; i < a.size(); i++) {
            assertEquals(a.get(i).excitation(), b.get(i).excitation());
            assertEquals(a.get(i).rawSurprise(), b.get(i).rawSurprise());
        }
    }
}
