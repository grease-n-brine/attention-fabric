package greasenbrine.fabric.core;

/**
 * Immutable parameter set governing the local dynamics of a single Silk.
 *
 * <ul>
 *   <li>{@code expectationRate} (alpha): rate at which the expected input tracks the observed input.</li>
 *   <li>{@code leakRate} (lambda): fraction of excitation removed per tick.</li>
 *   <li>{@code inputGain} (g_x): excitation added per unit of absolute input.</li>
 *   <li>{@code surpriseGain} (g_s): excitation added per unit of damped surprise.</li>
 *   <li>{@code dampeningFactor} (d): fraction of raw surprise suppressed before it drives excitation.</li>
 *   <li>{@code baselineRate} (beta): rate at which the adaptive baseline tracks excitation.</li>
 *   <li>{@code firingThreshold} (theta): excitation deviation above baseline required to fire.</li>
 *   <li>{@code maxExcitation} (e_max): upper clamp on excitation.</li>
 * </ul>
 */
public record SilkParameters(
    double expectationRate,
    double leakRate,
    double inputGain,
    double surpriseGain,
    double dampeningFactor,
    double baselineRate,
    double firingThreshold,
    double maxExcitation
) {
    public SilkParameters {
        requireUnit(expectationRate, "expectationRate");
        requireUnit(leakRate, "leakRate");
        requireUnit(dampeningFactor, "dampeningFactor");
        requireUnit(baselineRate, "baselineRate");
        requireNonNegative(inputGain, "inputGain");
        requireNonNegative(surpriseGain, "surpriseGain");
        requireNonNegative(firingThreshold, "firingThreshold");
        if (maxExcitation <= 0.0) {
            throw new IllegalArgumentException("maxExcitation must be positive");
        }
    }

    public static SilkParameters defaults() {
        return new SilkParameters(0.2, 0.1, 0.0, 0.5, 0.1, 0.05, 0.5, 10.0);
    }

    public SilkParameters withExpectationRate(double value) {
        return new SilkParameters(value, leakRate, inputGain, surpriseGain, dampeningFactor, baselineRate, firingThreshold, maxExcitation);
    }

    public SilkParameters withLeakRate(double value) {
        return new SilkParameters(expectationRate, value, inputGain, surpriseGain, dampeningFactor, baselineRate, firingThreshold, maxExcitation);
    }

    public SilkParameters withSurpriseGain(double value) {
        return new SilkParameters(expectationRate, leakRate, inputGain, value, dampeningFactor, baselineRate, firingThreshold, maxExcitation);
    }

    public SilkParameters withBaselineRate(double value) {
        return new SilkParameters(expectationRate, leakRate, inputGain, surpriseGain, dampeningFactor, value, firingThreshold, maxExcitation);
    }

    public SilkParameters withFiringThreshold(double value) {
        return new SilkParameters(expectationRate, leakRate, inputGain, surpriseGain, dampeningFactor, baselineRate, value, maxExcitation);
    }

    private static void requireUnit(double value, String name) {
        if (value < 0.0 || value > 1.0) {
            throw new IllegalArgumentException(name + " must lie in [0, 1]");
        }
    }

    private static void requireNonNegative(double value, String name) {
        if (value < 0.0) {
            throw new IllegalArgumentException(name + " must be non-negative");
        }
    }
}
