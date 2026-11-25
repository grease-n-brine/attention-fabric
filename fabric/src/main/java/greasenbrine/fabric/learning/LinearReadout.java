package greasenbrine.fabric.learning;

import java.util.Arrays;

/**
 * Linear readout trained online with the least-mean-squares rule.
 *
 * <p>The update for each weight depends only on the global prediction error and
 * the corresponding local feature value; no error is propagated into the units
 * that produce the features.</p>
 */
public class LinearReadout {

    private final double[] weights;
    private double bias;
    private final double learningRate;

    public LinearReadout(int featureCount, double learningRate) {
        if (featureCount < 1) {
            throw new IllegalArgumentException("featureCount must be at least 1");
        }
        if (learningRate <= 0.0) {
            throw new IllegalArgumentException("learningRate must be positive");
        }
        this.weights = new double[featureCount];
        this.bias = 0.0;
        this.learningRate = learningRate;
    }

    public double predict(double[] features) {
        checkLength(features);
        double sum = bias;
        for (int i = 0; i < weights.length; i++) {
            sum += weights[i] * features[i];
        }
        return sum;
    }

    /** Performs one LMS step and returns the prediction made before the update. */
    public double update(double[] features, double target) {
        double prediction = predict(features);
        double error = target - prediction;
        for (int i = 0; i < weights.length; i++) {
            weights[i] += learningRate * error * features[i];
        }
        bias += learningRate * error;
        return prediction;
    }

    public double[] getWeights() {
        return Arrays.copyOf(weights, weights.length);
    }

    public double getBias() {
        return bias;
    }

    public int getFeatureCount() {
        return weights.length;
    }

    private void checkLength(double[] features) {
        if (features.length != weights.length) {
            throw new IllegalArgumentException(
                "expected " + weights.length + " features but received " + features.length);
        }
    }
}
