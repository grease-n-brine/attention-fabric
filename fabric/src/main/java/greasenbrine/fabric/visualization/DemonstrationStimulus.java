package greasenbrine.fabric.visualization;

import java.util.Random;

/**
 * Stimulus used for the demonstration recording: a noisy sinusoid with a single
 * isolated pulse followed later by a train of closely spaced pulses, so that the
 * initial cascade and the subsequent decline in response are both visible.
 */
public final class DemonstrationStimulus {

    public static final int TICKS = 720;
    public static final int PERIOD = 50;
    public static final double NOISE = 0.05;
    public static final double PULSE = 3.0;
    public static final int ISOLATED_PULSE_TICK = 180;
    public static final int TRAIN_START = 400;
    public static final int TRAIN_END = 600;
    public static final int TRAIN_INTERVAL = 10;

    private final double[] values;
    private final boolean[] pulses;

    public DemonstrationStimulus(long seed) {
        Random rng = new Random(seed);
        values = new double[TICKS];
        pulses = new boolean[TICKS];
        for (int t = 0; t < TICKS; t++) {
            double v = Math.sin(2.0 * Math.PI * t / PERIOD) + NOISE * rng.nextGaussian();
            if (isPulse(t)) {
                v += PULSE;
                pulses[t] = true;
            }
            values[t] = v;
        }
    }

    public static boolean isPulse(int t) {
        if (t == ISOLATED_PULSE_TICK) {
            return true;
        }
        return t >= TRAIN_START && t <= TRAIN_END && (t - TRAIN_START) % TRAIN_INTERVAL == 0;
    }

    public int length() {
        return TICKS;
    }

    public double value(int t) {
        return values[t];
    }

    public boolean pulse(int t) {
        return pulses[t];
    }

    /** The most recent {@code size} samples ending at {@code t}, padded with zeros before the start. */
    public FrameRenderer.SignalWindow window(int t, int size) {
        double[] v = new double[size];
        boolean[] p = new boolean[size];
        for (int i = 0; i < size; i++) {
            int idx = t - (size - 1) + i;
            if (idx >= 0) {
                v[i] = values[idx];
                p[i] = pulses[idx];
            }
        }
        return new FrameRenderer.SignalWindow(v, p);
    }
}
