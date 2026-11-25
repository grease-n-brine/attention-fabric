package greasenbrine.fabric.learning;

import java.util.Random;

/**
 * Fixed random features of the same functional form as a Silk transform,
 * {@code tanh(a * x + b + c * x^2)}, with parameters drawn once and held fixed.
 * Only the linear readout is trained. This control isolates the effect of
 * structural dynamics from the feature family itself.
 */
public class RandomFeatureModel implements OnlineRegressor {

    private final double[] a;
    private final double[] b;
    private final double[] c;
    private final LinearReadout readout;

    public RandomFeatureModel(int featureCount, double bound, double learningRate, long seed) {
        Random rng = new Random(seed);
        this.a = new double[featureCount];
        this.b = new double[featureCount];
        this.c = new double[featureCount];
        for (int i = 0; i < featureCount; i++) {
            a[i] = uniform(rng, -bound, bound);
            b[i] = uniform(rng, -bound, bound);
            c[i] = uniform(rng, -bound, bound);
        }
        this.readout = new LinearReadout(featureCount, learningRate);
    }

    private double[] features(double x) {
        double[] f = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            f[i] = Math.tanh(a[i] * x + b[i] + c[i] * x * x);
        }
        return f;
    }

    @Override
    public double predict(double input) {
        return readout.predict(features(input));
    }

    @Override
    public double train(double input, double target) {
        return readout.update(features(input), target);
    }

    @Override
    public int parameterCount() {
        return a.length + 1;
    }

    @Override
    public String name() {
        return "random-features";
    }

    private static double uniform(Random rng, double low, double high) {
        return low + (high - low) * rng.nextDouble();
    }
}
