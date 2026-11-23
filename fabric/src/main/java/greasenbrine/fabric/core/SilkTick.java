package greasenbrine.fabric.core;

public record SilkTick(
    long tick,
    double[][][] state,
    double[][][] predictedFutureState,
    double[][][] surprise,
    boolean spiked
) {}
