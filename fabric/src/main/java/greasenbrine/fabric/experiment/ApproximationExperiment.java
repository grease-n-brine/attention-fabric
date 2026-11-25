package greasenbrine.fabric.experiment;

import greasenbrine.fabric.core.FabricParameters;
import greasenbrine.fabric.core.SilkFabric;
import greasenbrine.fabric.core.SilkParameters;
import greasenbrine.fabric.learning.FabricRegressor;
import greasenbrine.fabric.learning.LinearModel;
import greasenbrine.fabric.learning.MultilayerPerceptron;
import greasenbrine.fabric.learning.OnlineRegressor;
import greasenbrine.fabric.learning.RandomFeatureModel;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.DoubleUnaryOperator;

/**
 * Experiment 3: online approximation of continuous functions on [-1, 1].
 * Fabric variants are compared against a linear model, fixed random features of
 * the same functional family and capacity, and a backpropagation MLP.
 */
public final class ApproximationExperiment {

    public static final int TRAIN_STEPS = 20000;
    public static final int TEST_POINTS = 201;
    public static final int CAPACITY = 64;
    public static final int MLP_HIDDEN = 16;
    public static final double READOUT_RATE = 0.01;
    public static final double MLP_RATE = 0.01;
    public static final double LINEAR_RATE = 0.05;

    private ApproximationExperiment() {
    }

    public record Target(String name, DoubleUnaryOperator function) {
    }

    public record Result(String target, String model, long seed, double testMse, int activeUnits, int parameters) {
    }

    public static List<Target> targets() {
        return List.of(
            new Target("sine", x -> Math.sin(Math.PI * x)),
            new Target("quadratic", x -> x * x),
            new Target("gaussian", x -> Math.exp(-8.0 * x * x)),
            new Target("absolute", Math::abs)
        );
    }

    /** Model constructors keyed by label; each receives the run seed. */
    public static Map<String, java.util.function.LongFunction<OnlineRegressor>> models(SilkParameters unitParameters) {
        Map<String, java.util.function.LongFunction<OnlineRegressor>> m = new LinkedHashMap<>();
        m.put("linear", seed -> new LinearModel(LINEAR_RATE));
        m.put("random-features", seed -> new RandomFeatureModel(CAPACITY, 2.0, READOUT_RATE, seed));
        m.put("mlp", seed -> new MultilayerPerceptron(MLP_HIDDEN, MLP_RATE, seed));
        m.put("fabric-static", seed -> {
            SilkFabric f = new SilkFabric(
                FabricParameters.defaults(CAPACITY).withRecruitment(false).withPruning(false).withMigration(false),
                unitParameters, seed);
            f.attachAllAsRandomTree();
            return new FabricRegressor(f, READOUT_RATE, "fabric-static");
        });
        m.put("fabric-recruit", seed -> new FabricRegressor(
            new SilkFabric(FabricParameters.defaults(CAPACITY).withPruning(false).withMigration(false), unitParameters, seed),
            READOUT_RATE, "fabric-recruit"));
        m.put("fabric-no-migrate", seed -> new FabricRegressor(
            new SilkFabric(FabricParameters.defaults(CAPACITY).withMigration(false), unitParameters, seed),
            READOUT_RATE, "fabric-no-migrate"));
        m.put("fabric-no-prune", seed -> new FabricRegressor(
            new SilkFabric(FabricParameters.defaults(CAPACITY).withPruning(false), unitParameters, seed),
            READOUT_RATE, "fabric-no-prune"));
        m.put("fabric-full", seed -> new FabricRegressor(
            new SilkFabric(FabricParameters.defaults(CAPACITY), unitParameters, seed),
            READOUT_RATE, "fabric-full"));
        m.put("fabric-full-passthrough", seed -> new FabricRegressor(
            new SilkFabric(FabricParameters.defaults(CAPACITY).withRootPassThrough(true), unitParameters, seed),
            READOUT_RATE, "fabric-full-passthrough"));
        return m;
    }

    public static Result run(Target target, String label, OnlineRegressor model, long seed) {
        Random rng = new Random(seed);
        for (int i = 0; i < TRAIN_STEPS; i++) {
            double x = rng.nextDouble() * 2.0 - 1.0;
            model.train(x, target.function().applyAsDouble(x));
        }
        double sum = 0.0;
        for (int i = 0; i < TEST_POINTS; i++) {
            double x = -1.0 + 2.0 * i / (TEST_POINTS - 1);
            double err = model.predict(x) - target.function().applyAsDouble(x);
            sum += err * err;
        }
        int active = model instanceof FabricRegressor fr ? fr.getFabric().activeCount() : -1;
        return new Result(target.name(), label, seed, sum / TEST_POINTS, active, model.parameterCount());
    }

    public static List<Result> runAll(SilkParameters unitParameters, int seeds) {
        List<Result> results = new ArrayList<>();
        Map<String, java.util.function.LongFunction<OnlineRegressor>> models = models(unitParameters);
        for (Target target : targets()) {
            for (Map.Entry<String, java.util.function.LongFunction<OnlineRegressor>> e : models.entrySet()) {
                for (long seed = 0; seed < seeds; seed++) {
                    results.add(run(target, e.getKey(), e.getValue().apply(seed), seed));
                }
            }
        }
        return results;
    }

    public static void writeAll(Path directory, SilkParameters unitParameters, int seeds) throws IOException {
        List<Result> results = runAll(unitParameters, seeds);
        try (CsvWriter raw = new CsvWriter(directory.resolve("e3_approximation.csv"),
                "target", "model", "seed", "test_mse", "active_units", "parameters");
             CsvWriter summary = new CsvWriter(directory.resolve("e3_summary.csv"),
                "target", "model", "mean_mse", "std_mse", "mean_active", "parameters")) {
            for (Result r : results) {
                raw.row(r.target(), r.model(), r.seed(), r.testMse(), r.activeUnits(), r.parameters());
            }
            for (Target target : targets()) {
                for (String model : models(unitParameters).keySet()) {
                    List<Result> group = results.stream()
                        .filter(r -> r.target().equals(target.name()) && r.model().equals(model))
                        .toList();
                    double[] mse = group.stream().mapToDouble(Result::testMse).toArray();
                    double[] active = group.stream().mapToDouble(Result::activeUnits).toArray();
                    summary.row(target.name(), model, Statistics.mean(mse), Statistics.standardDeviation(mse),
                        Statistics.mean(active), group.get(0).parameters());
                }
            }
        }
    }
}
