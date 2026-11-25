package greasenbrine.fabric.learning;

import java.util.Random;

/**
 * Single-hidden-layer perceptron with hyperbolic tangent units, trained by
 * stochastic gradient descent with backpropagation. Serves as the global
 * optimization reference against which local adaptation is compared.
 */
public class MultilayerPerceptron implements OnlineRegressor {

    private final int hidden;
    private final double[] w1;
    private final double[] b1;
    private final double[] w2;
    private double b2;
    private final double learningRate;

    public MultilayerPerceptron(int hidden, double learningRate, long seed) {
        if (hidden < 1) {
            throw new IllegalArgumentException("hidden must be at least 1");
        }
        Random rng = new Random(seed);
        this.hidden = hidden;
        this.w1 = new double[hidden];
        this.b1 = new double[hidden];
        this.w2 = new double[hidden];
        double scale = 1.0 / Math.sqrt(hidden);
        for (int i = 0; i < hidden; i++) {
            w1[i] = rng.nextGaussian();
            b1[i] = rng.nextGaussian() * 0.5;
            w2[i] = rng.nextGaussian() * scale;
        }
        this.b2 = 0.0;
        this.learningRate = learningRate;
    }

    @Override
    public double predict(double input) {
        double sum = b2;
        for (int i = 0; i < hidden; i++) {
            sum += w2[i] * Math.tanh(w1[i] * input + b1[i]);
        }
        return sum;
    }

    @Override
    public double train(double input, double target) {
        double[] h = new double[hidden];
        double out = b2;
        for (int i = 0; i < hidden; i++) {
            h[i] = Math.tanh(w1[i] * input + b1[i]);
            out += w2[i] * h[i];
        }
        double error = out - target;
        for (int i = 0; i < hidden; i++) {
            double gradHidden = error * w2[i] * (1.0 - h[i] * h[i]);
            w2[i] -= learningRate * error * h[i];
            w1[i] -= learningRate * gradHidden * input;
            b1[i] -= learningRate * gradHidden;
        }
        b2 -= learningRate * error;
        return out;
    }

    @Override
    public int parameterCount() {
        return 3 * hidden + 1;
    }

    @Override
    public String name() {
        return "mlp";
    }
}
