package greasenbrine.fabric.experiment;

import greasenbrine.fabric.core.Silk;
import greasenbrine.fabric.core.SilkParameters;
import greasenbrine.fabric.core.SilkTick;
import greasenbrine.fabric.structures.Point;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Experiment 1: a single unit receives a piecewise-constant signal
 * (1.0 for 20 ticks, 3.0 for 20 ticks, 1.0 for 20 ticks) and the full
 * internal trajectory is recorded. The experiment is deterministic.
 */
public final class SingleUnitExperiment {

    public static final int SEGMENT_LENGTH = 20;
    public static final double LOW = 1.0;
    public static final double HIGH = 3.0;

    private SingleUnitExperiment() {
    }

    public record Summary(
        double settledSurprise,
        double spikeSurprise,
        int ticksToRecoverTenPercent,
        double settledExcitation,
        double peakExcitation,
        double finalBaseline
    ) {
    }

    public static List<SilkTick> run(SilkParameters parameters) {
        Silk unit = new Silk(0, new Point(new double[]{1.0, 0.0, 0.0}), parameters);
        List<SilkTick> trace = new ArrayList<>();
        for (int i = 0; i < SEGMENT_LENGTH; i++) {
            trace.add(unit.step(LOW));
        }
        for (int i = 0; i < SEGMENT_LENGTH; i++) {
            trace.add(unit.step(HIGH));
        }
        for (int i = 0; i < SEGMENT_LENGTH; i++) {
            trace.add(unit.step(LOW));
        }
        return trace;
    }

    public static Summary summarize(List<SilkTick> trace) {
        SilkTick settled = trace.get(SEGMENT_LENGTH - 1);
        SilkTick spike = trace.get(SEGMENT_LENGTH);
        double threshold = 0.1 * spike.rawSurprise();
        int recovery = -1;
        for (int i = SEGMENT_LENGTH; i < 2 * SEGMENT_LENGTH; i++) {
            if (trace.get(i).rawSurprise() <= threshold) {
                recovery = i - SEGMENT_LENGTH;
                break;
            }
        }
        double peak = 0.0;
        for (SilkTick t : trace) {
            peak = Math.max(peak, t.excitation());
        }
        return new Summary(
            settled.rawSurprise(),
            spike.rawSurprise(),
            recovery,
            settled.excitation(),
            peak,
            trace.get(trace.size() - 1).baseline()
        );
    }

    public static Summary writeTrace(Path output, SilkParameters parameters) throws IOException {
        List<SilkTick> trace = run(parameters);
        try (CsvWriter csv = new CsvWriter(output,
            "tick", "input", "expected", "raw_surprise", "damped_surprise",
            "excitation", "baseline", "deviation", "output", "fired")) {
            for (SilkTick t : trace) {
                csv.row(t.tick(), t.input(), t.expectedInput(), t.rawSurprise(), t.dampedSurprise(),
                    t.excitation(), t.baseline(), t.deviation(), t.output(), t.fired());
            }
        }
        return summarize(trace);
    }
}
