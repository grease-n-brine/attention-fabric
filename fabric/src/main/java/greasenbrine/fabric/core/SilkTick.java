package greasenbrine.fabric.core;

import greasenbrine.fabric.structures.Point;

/**
 * Immutable record of the state of one Silk after a single update.
 *
 * @param silkId         identifier of the Silk
 * @param tick           update index, starting at 1 for the first processed input
 * @param position       spatial position after the update
 * @param input          scalar signal received through the receiver
 * @param expectedInput  expected input prior to observing {@code input}
 * @param rawSurprise    absolute residual between {@code input} and {@code expectedInput}
 * @param dampedSurprise raw surprise after dampening
 * @param excitation     excitation after gain and leakage
 * @param baseline       adaptive baseline after the update
 * @param output         transformed signal emitted through the teeth
 * @param fired          whether excitation exceeded the baseline by the firing threshold
 */
public record SilkTick(
    int silkId,
    long tick,
    Point position,
    double input,
    double expectedInput,
    double rawSurprise,
    double dampedSurprise,
    double excitation,
    double baseline,
    double output,
    boolean fired
) {
    /** Excitation relative to the adaptive baseline. */
    public double deviation() {
        return excitation - baseline;
    }
}
