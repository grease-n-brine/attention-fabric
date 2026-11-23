package greasenbrine.fabric.core;

import greasenbrine.fabric.structures.Point;

public record SilkTick(
    long tick,
    Point position,
    Point receiverInput,
    Point excitation,
    double targetExcitation,
    double effectiveThreshold,
    double rawSurprise,
    double dampedSurprise,
    double dampeningFactor,
    boolean triggered,
    boolean surpriseBoosted,
    boolean recruiting
) {}
