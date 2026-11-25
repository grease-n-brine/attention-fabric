package greasenbrine.fabric.learning;

/**
 * A scalar regressor trained one sample at a time.
 */
public interface OnlineRegressor {

    /** Observes one input and returns the current prediction without learning. */
    double predict(double input);

    /** Observes one input-target pair, learns from it, and returns the prediction made before learning. */
    double train(double input, double target);

    /** Number of adjustable parameters that are updated during training. */
    int parameterCount();

    /** Human-readable identifier used in result tables. */
    String name();
}
