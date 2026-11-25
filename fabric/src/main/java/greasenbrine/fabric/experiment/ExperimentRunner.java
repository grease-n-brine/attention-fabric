package greasenbrine.fabric.experiment;

import greasenbrine.fabric.core.SilkParameters;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Runs every experiment and writes CSV results to the given directory.
 *
 * <p>Usage: {@code ExperimentRunner <output-directory> [seeds]}</p>
 */
public final class ExperimentRunner {

    public static final int DEFAULT_SEEDS = 10;
    public static final double[] BASELINE_RATES = {0.01, 0.05, 0.2};

    private ExperimentRunner() {
    }

    public static void main(String[] args) throws IOException {
        Path output = Path.of(args.length > 0 ? args[0] : "results");
        int seeds = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_SEEDS;
        Files.createDirectories(output);
        SilkParameters unit = SilkParameters.defaults();

        long start = System.nanoTime();

        SingleUnitExperiment.Summary e1 = SingleUnitExperiment.writeTrace(output.resolve("e1_single_unit_trace.csv"), unit);
        System.out.println(String.format(Locale.ROOT,
            "E1 settled_surprise=%.4f spike_surprise=%.4f recovery_ticks=%d peak_excitation=%.4f final_baseline=%.4f",
            e1.settledSurprise(), e1.spikeSurprise(), e1.ticksToRecoverTenPercent(), e1.peakExcitation(), e1.finalBaseline()));

        HabituationExperiment.writeAll(output, unit, BASELINE_RATES, seeds);
        System.out.println("E2 written");

        ApproximationExperiment.writeAll(output, unit, seeds);
        System.out.println("E3 written");

        writeEnvironment(output.resolve("environment.txt"), seeds, unit);
        double seconds = (System.nanoTime() - start) / 1e9;
        System.out.println(String.format(Locale.ROOT, "completed in %.1f s; results in %s", seconds, output.toAbsolutePath()));
    }

    private static void writeEnvironment(Path path, int seeds, SilkParameters unit) throws IOException {
        String text = String.join("\n",
            "java.version=" + System.getProperty("java.version"),
            "java.vendor=" + System.getProperty("java.vendor"),
            "os.name=" + System.getProperty("os.name"),
            "os.arch=" + System.getProperty("os.arch"),
            "seeds=" + seeds,
            "unit.parameters=" + unit,
            "e2.baseline_rates=" + java.util.Arrays.toString(BASELINE_RATES),
            "e3.capacity=" + ApproximationExperiment.CAPACITY,
            "e3.train_steps=" + ApproximationExperiment.TRAIN_STEPS,
            "e3.test_points=" + ApproximationExperiment.TEST_POINTS,
            "e3.mlp_hidden=" + ApproximationExperiment.MLP_HIDDEN,
            "");
        Files.writeString(path, text);
    }
}
