package greasenbrine.fabric.learning;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import greasenbrine.fabric.core.FabricParameters;
import greasenbrine.fabric.core.SilkFabric;
import greasenbrine.fabric.core.SilkParameters;
import java.util.Random;
import java.util.function.DoubleUnaryOperator;
import org.junit.jupiter.api.Test;

class RegressorTest {

    private static double trainAndTest(OnlineRegressor model, DoubleUnaryOperator target, int steps, long seed) {
        Random rng = new Random(seed);
        for (int i = 0; i < steps; i++) {
            double x = rng.nextDouble() * 2 - 1;
            model.train(x, target.applyAsDouble(x));
        }
        double sum = 0.0;
        int n = 0;
        for (double x = -1.0; x <= 1.0 + 1e-9; x += 0.05) {
            double err = model.predict(x) - target.applyAsDouble(x);
            sum += err * err;
            n++;
        }
        return sum / n;
    }

    @Test
    void linearModelFitsAffineTarget() {
        double mse = trainAndTest(new LinearModel(0.05), x -> 2.0 * x - 0.3, 5000, 1L);
        assertTrue(mse < 1e-5, "mse=" + mse);
        assertEquals(2, new LinearModel(0.05).parameterCount());
    }

    @Test
    void randomFeaturesOutperformLinearOnSine() {
        double linear = trainAndTest(new LinearModel(0.05), x -> Math.sin(Math.PI * x), 10000, 2L);
        double features = trainAndTest(new RandomFeatureModel(32, 2.0, 0.01, 2L), x -> Math.sin(Math.PI * x), 10000, 2L);
        assertTrue(features < linear, "features=" + features + " linear=" + linear);
    }

    @Test
    void perceptronFitsSine() {
        double mse = trainAndTest(new MultilayerPerceptron(16, 0.01, 3L), x -> Math.sin(Math.PI * x), 20000, 3L);
        assertTrue(mse < 0.02, "mse=" + mse);
        assertEquals(49, new MultilayerPerceptron(16, 0.01, 3L).parameterCount());
    }

    @Test
    void perceptronIsDeterministicForSeed() {
        MultilayerPerceptron a = new MultilayerPerceptron(8, 0.01, 7L);
        MultilayerPerceptron b = new MultilayerPerceptron(8, 0.01, 7L);
        for (int i = 0; i < 100; i++) {
            a.train(0.3, 0.5);
            b.train(0.3, 0.5);
        }
        assertEquals(a.predict(0.1), b.predict(0.1));
    }

    @Test
    void fabricRegressorLearnsSomethingBeyondConstant() {
        SilkFabric fabric = new SilkFabric(FabricParameters.defaults(32), SilkParameters.defaults(), 5L);
        FabricRegressor model = new FabricRegressor(fabric, 0.01, "fabric");
        double mse = trainAndTest(model, x -> Math.sin(Math.PI * x), 10000, 5L);
        double constantBaseline = 0.5;
        assertTrue(mse < constantBaseline, "mse=" + mse);
        assertEquals(33, model.parameterCount());
    }

    @Test
    void fabricRegressorIsDeterministicForSeed() {
        FabricRegressor a = new FabricRegressor(
            new SilkFabric(FabricParameters.defaults(16), SilkParameters.defaults(), 9L), 0.01, "fabric");
        FabricRegressor b = new FabricRegressor(
            new SilkFabric(FabricParameters.defaults(16), SilkParameters.defaults(), 9L), 0.01, "fabric");
        Random r1 = new Random(1L);
        Random r2 = new Random(1L);
        for (int i = 0; i < 500; i++) {
            double x1 = r1.nextDouble() * 2 - 1;
            double x2 = r2.nextDouble() * 2 - 1;
            a.train(x1, Math.sin(Math.PI * x1));
            b.train(x2, Math.sin(Math.PI * x2));
        }
        assertEquals(a.predict(0.25), b.predict(0.25));
        assertEquals(a.getFabric().activeCount(), b.getFabric().activeCount());
    }
}
