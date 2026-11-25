package greasenbrine.fabric.learning;

/**
 * Affine model {@code y = w * x + b} trained by LMS.
 */
public class LinearModel implements OnlineRegressor {

    private final LinearReadout readout;

    public LinearModel(double learningRate) {
        this.readout = new LinearReadout(1, learningRate);
    }

    @Override
    public double predict(double input) {
        return readout.predict(new double[]{input});
    }

    @Override
    public double train(double input, double target) {
        return readout.update(new double[]{input}, target);
    }

    @Override
    public int parameterCount() {
        return 2;
    }

    @Override
    public String name() {
        return "linear";
    }
}
