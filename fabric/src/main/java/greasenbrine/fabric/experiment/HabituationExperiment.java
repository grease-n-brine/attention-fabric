package greasenbrine.fabric.experiment;

import greasenbrine.fabric.core.FabricParameters;
import greasenbrine.fabric.core.Silk;
import greasenbrine.fabric.core.SilkFabric;
import greasenbrine.fabric.core.SilkParameters;
import greasenbrine.fabric.core.SilkTick;
import greasenbrine.fabric.structures.Point;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Experiment 2: response to anomalous pulses superimposed on a periodic signal,
 * under a rare and a frequent pulse schedule. Measures whether the response of a
 * single unit and of a fabric declines with repetition.
 */
public final class HabituationExperiment {

    public static final int TOTAL_TICKS = 4000;
    public static final int WARMUP_TICKS = 200;
    public static final int PERIOD = 50;
    public static final double NOISE = 0.05;
    public static final double PULSE = 3.0;
    public static final int RARE_INTERVAL = 200;
    public static final int FREQUENT_INTERVAL = 10;
    public static final int FABRIC_CAPACITY = 32;
    public static final int SAMPLE_EVERY = 50;

    private HabituationExperiment() {
    }

    public enum Condition {
        RARE(RARE_INTERVAL), FREQUENT(FREQUENT_INTERVAL);

        public final int interval;

        Condition(int interval) {
            this.interval = interval;
        }

        public String label() {
            return name().toLowerCase();
        }
    }

    public record Stream(double[] values, boolean[] anomaly) {
    }

    public record UnitResult(
        double earlyFireRate, double lateFireRate,
        double earlyDeviation, double lateDeviation,
        double falseAlarmRate
    ) {
    }

    public record NetworkResult(
        double earlyCascade, double lateCascade,
        double earlyRecruits, double lateRecruits,
        int finalActive, double meanActive,
        int[] activeSamples
    ) {
    }

    public static Stream generate(Condition condition, long seed) {
        Random rng = new Random(seed);
        double[] values = new double[TOTAL_TICKS];
        boolean[] anomaly = new boolean[TOTAL_TICKS];
        for (int t = 0; t < TOTAL_TICKS; t++) {
            double v = Math.sin(2.0 * Math.PI * t / PERIOD) + NOISE * rng.nextGaussian();
            if (t >= WARMUP_TICKS && (t - WARMUP_TICKS) % condition.interval == 0) {
                v += PULSE;
                anomaly[t] = true;
            }
            values[t] = v;
        }
        return new Stream(values, anomaly);
    }

    public static UnitResult runUnit(Stream stream, SilkParameters parameters) {
        Silk unit = new Silk(0, new Point(new double[]{1.0, 0.0, 0.0}), parameters);
        List<Boolean> anomalyFired = new ArrayList<>();
        List<Double> anomalyDeviation = new ArrayList<>();
        int normalTicks = 0;
        int normalFires = 0;

        for (int t = 0; t < TOTAL_TICKS; t++) {
            SilkTick tick = unit.step(stream.values[t]);
            if (stream.anomaly[t]) {
                anomalyFired.add(tick.fired());
                anomalyDeviation.add(tick.deviation());
            } else if (t >= WARMUP_TICKS) {
                normalTicks++;
                if (tick.fired()) {
                    normalFires++;
                }
            }
        }

        int n = anomalyFired.size();
        int quarter = Math.max(1, n / 4);
        return new UnitResult(
            rate(anomalyFired, 0, quarter),
            rate(anomalyFired, n - quarter, n),
            mean(anomalyDeviation, 0, quarter),
            mean(anomalyDeviation, n - quarter, n),
            normalTicks == 0 ? 0.0 : (double) normalFires / normalTicks
        );
    }

    public static NetworkResult runNetwork(Stream stream, SilkParameters unitParameters, long seed) {
        SilkFabric fabric = new SilkFabric(FabricParameters.defaults(FABRIC_CAPACITY), unitParameters, seed);
        List<Double> cascade = new ArrayList<>();
        List<Double> recruits = new ArrayList<>();
        List<Integer> samples = new ArrayList<>();
        double activeSum = 0.0;

        for (int t = 0; t < TOTAL_TICKS; t++) {
            int before = fabric.activeCount();
            fabric.step(stream.values[t]);
            int after = fabric.activeCount();
            activeSum += after;
            if (stream.anomaly[t]) {
                cascade.add((double) fabric.getFiredThisTick().size());
                recruits.add((double) (after - before));
            }
            if (t % SAMPLE_EVERY == 0) {
                samples.add(after);
            }
        }

        int n = cascade.size();
        int quarter = Math.max(1, n / 4);
        int[] sampleArray = samples.stream().mapToInt(Integer::intValue).toArray();
        return new NetworkResult(
            mean(cascade, 0, quarter),
            mean(cascade, n - quarter, n),
            mean(recruits, 0, quarter),
            mean(recruits, n - quarter, n),
            fabric.activeCount(),
            activeSum / TOTAL_TICKS,
            sampleArray
        );
    }

    public static void writeAll(Path directory, SilkParameters base, double[] baselineRates, int seeds) throws IOException {
        try (CsvWriter unitCsv = new CsvWriter(directory.resolve("e2_unit.csv"),
                "condition", "baseline_rate", "seed", "early_fire_rate", "late_fire_rate",
                "early_deviation", "late_deviation", "false_alarm_rate");
             CsvWriter netCsv = new CsvWriter(directory.resolve("e2_network.csv"),
                "condition", "seed", "early_cascade", "late_cascade", "early_recruits", "late_recruits",
                "final_active", "mean_active");
             CsvWriter seriesCsv = new CsvWriter(directory.resolve("e2_active_timeseries.csv"),
                "condition", "tick", "mean_active");
             CsvWriter summaryCsv = new CsvWriter(directory.resolve("e2_summary.csv"),
                "level", "condition", "baseline_rate", "metric", "mean", "std")) {

            for (Condition condition : Condition.values()) {
                for (double beta : baselineRates) {
                    SilkParameters p = base.withBaselineRate(beta);
                    double[] early = new double[seeds];
                    double[] late = new double[seeds];
                    double[] earlyDev = new double[seeds];
                    double[] lateDev = new double[seeds];
                    double[] falseAlarm = new double[seeds];
                    for (int s = 0; s < seeds; s++) {
                        UnitResult r = runUnit(generate(condition, s), p);
                        early[s] = r.earlyFireRate();
                        late[s] = r.lateFireRate();
                        earlyDev[s] = r.earlyDeviation();
                        lateDev[s] = r.lateDeviation();
                        falseAlarm[s] = r.falseAlarmRate();
                        unitCsv.row(condition.label(), beta, s, r.earlyFireRate(), r.lateFireRate(),
                            r.earlyDeviation(), r.lateDeviation(), r.falseAlarmRate());
                    }
                    summary(summaryCsv, "unit", condition, beta, "early_fire_rate", early);
                    summary(summaryCsv, "unit", condition, beta, "late_fire_rate", late);
                    summary(summaryCsv, "unit", condition, beta, "early_deviation", earlyDev);
                    summary(summaryCsv, "unit", condition, beta, "late_deviation", lateDev);
                    summary(summaryCsv, "unit", condition, beta, "false_alarm_rate", falseAlarm);
                }

                double[] earlyCascade = new double[seeds];
                double[] lateCascade = new double[seeds];
                double[] earlyRecruits = new double[seeds];
                double[] lateRecruits = new double[seeds];
                double[] meanActive = new double[seeds];
                double[] finalActive = new double[seeds];
                double[][] series = new double[seeds][];
                for (int s = 0; s < seeds; s++) {
                    NetworkResult r = runNetwork(generate(condition, s), base, s);
                    earlyCascade[s] = r.earlyCascade();
                    lateCascade[s] = r.lateCascade();
                    earlyRecruits[s] = r.earlyRecruits();
                    lateRecruits[s] = r.lateRecruits();
                    meanActive[s] = r.meanActive();
                    finalActive[s] = r.finalActive();
                    series[s] = toDouble(r.activeSamples());
                    netCsv.row(condition.label(), s, r.earlyCascade(), r.lateCascade(), r.earlyRecruits(),
                        r.lateRecruits(), r.finalActive(), r.meanActive());
                }
                for (int i = 0; i < series[0].length; i++) {
                    double[] column = new double[seeds];
                    for (int s = 0; s < seeds; s++) {
                        column[s] = series[s][i];
                    }
                    seriesCsv.row(condition.label(), i * SAMPLE_EVERY, Statistics.mean(column));
                }
                summary(summaryCsv, "network", condition, base.baselineRate(), "early_cascade", earlyCascade);
                summary(summaryCsv, "network", condition, base.baselineRate(), "late_cascade", lateCascade);
                summary(summaryCsv, "network", condition, base.baselineRate(), "early_recruits", earlyRecruits);
                summary(summaryCsv, "network", condition, base.baselineRate(), "late_recruits", lateRecruits);
                summary(summaryCsv, "network", condition, base.baselineRate(), "mean_active", meanActive);
                summary(summaryCsv, "network", condition, base.baselineRate(), "final_active", finalActive);
            }
        }
    }

    private static void summary(CsvWriter csv, String level, Condition condition, double beta,
                                String metric, double[] values) throws IOException {
        csv.row(level, condition.label(), beta, metric, Statistics.mean(values), Statistics.standardDeviation(values));
    }

    private static double rate(List<Boolean> flags, int from, int to) {
        int count = 0;
        for (int i = from; i < to; i++) {
            if (flags.get(i)) {
                count++;
            }
        }
        return (double) count / (to - from);
    }

    private static double mean(List<Double> values, int from, int to) {
        double sum = 0.0;
        for (int i = from; i < to; i++) {
            sum += values.get(i);
        }
        return sum / (to - from);
    }

    private static double[] toDouble(int[] values) {
        double[] out = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            out[i] = values[i];
        }
        return out;
    }
}
