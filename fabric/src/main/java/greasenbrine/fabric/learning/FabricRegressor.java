package greasenbrine.fabric.learning;

import greasenbrine.fabric.core.SilkFabric;

/**
 * Regressor whose features are the outputs of the attached units of a
 * {@link SilkFabric}. The fabric itself adapts only through its local rules;
 * the readout is trained by LMS on the resulting feature vector.
 */
public class FabricRegressor implements OnlineRegressor {

    private final SilkFabric fabric;
    private final LinearReadout readout;
    private final String label;

    public FabricRegressor(SilkFabric fabric, double learningRate, String label) {
        this.fabric = fabric;
        this.readout = new LinearReadout(fabric.getParameters().capacity(), learningRate);
        this.label = label;
    }

    @Override
    public double predict(double input) {
        fabric.step(input);
        return readout.predict(fabric.outputs());
    }

    @Override
    public double train(double input, double target) {
        fabric.step(input);
        return readout.update(fabric.outputs(), target);
    }

    @Override
    public int parameterCount() {
        return readout.getFeatureCount() + 1;
    }

    @Override
    public String name() {
        return label;
    }

    public SilkFabric getFabric() {
        return fabric;
    }
}
