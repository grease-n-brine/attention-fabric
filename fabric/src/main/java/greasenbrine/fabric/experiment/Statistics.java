package greasenbrine.fabric.experiment;

/**
 * Sample statistics over a sequence of doubles.
 */
public final class Statistics {

    private Statistics() {
    }

    public static double mean(double[] values) {
        if (values.length == 0) {
            return Double.NaN;
        }
        double sum = 0.0;
        for (double v : values) {
            sum += v;
        }
        return sum / values.length;
    }

    /** Sample standard deviation with Bessel's correction. */
    public static double standardDeviation(double[] values) {
        if (values.length < 2) {
            return 0.0;
        }
        double mean = mean(values);
        double sum = 0.0;
        for (double v : values) {
            double d = v - mean;
            sum += d * d;
        }
        return Math.sqrt(sum / (values.length - 1));
    }
}
