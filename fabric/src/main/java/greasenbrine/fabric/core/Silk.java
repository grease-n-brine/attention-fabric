package greasenbrine.fabric.core;

import greasenbrine.fabric.structures.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A locally adaptive unit embedded in a three-dimensional state space.
 *
 * <p>Each update follows the sequence: observe input, compute surprise against the
 * expected input, update excitation with gain and leakage, adapt the baseline,
 * decide whether to fire, update the expected input, and emit a transformed output.
 * The emitted output is a coordinate-conditioned nonlinear function of the input;
 * surprise is never emitted.</p>
 */
public class Silk {

    private final int id;
    private final SilkParameters parameters;

    private Point position;
    private double expectedInput;
    private double excitation;
    private double baseline;
    private double lastOutput;
    private long tickCount;

    private final List<Silk> teeth = new ArrayList<>();
    private final List<SilkTick> history = new ArrayList<>();

    public Silk(int id, Point initialPosition, SilkParameters parameters) {
        this.id = id;
        this.position = Objects.requireNonNull(initialPosition, "initialPosition");
        this.parameters = Objects.requireNonNull(parameters, "parameters");
        if (initialPosition.getDimension() != 3) {
            throw new IllegalArgumentException("Silk position must be three-dimensional");
        }
        reset();
    }

    /** Restores the unit to its initial internal state and clears history. */
    public final void reset() {
        expectedInput = 0.0;
        excitation = 0.0;
        baseline = 0.0;
        lastOutput = 0.0;
        tickCount = 0;
        history.clear();
    }

    /** Sets the expected input directly, representing prior exposure to a signal. */
    public void seedExpectation(double value) {
        expectedInput = value;
    }

    /**
     * Processes one scalar input and returns the resulting tick.
     */
    public SilkTick step(double input) {
        double rawSurprise = Math.abs(input - expectedInput);
        double dampedSurprise = rawSurprise * (1.0 - parameters.dampeningFactor());

        double retained = excitation * (1.0 - parameters.leakRate());
        double drive = parameters.inputGain() * Math.abs(input)
            + parameters.surpriseGain() * dampedSurprise;
        excitation = Math.min(parameters.maxExcitation(), retained + drive);

        baseline = baseline * (1.0 - parameters.baselineRate())
            + excitation * parameters.baselineRate();

        boolean fired = (excitation - baseline) >= parameters.firingThreshold();

        double previousExpectation = expectedInput;
        expectedInput = expectedInput * (1.0 - parameters.expectationRate())
            + input * parameters.expectationRate();

        lastOutput = transform(input);
        tickCount++;

        SilkTick tick = new SilkTick(
            id,
            tickCount,
            position,
            input,
            previousExpectation,
            rawSurprise,
            dampedSurprise,
            excitation,
            baseline,
            lastOutput,
            fired
        );
        history.add(tick);
        return tick;
    }

    /**
     * Coordinate-conditioned nonlinear transform of the input:
     * {@code tanh(p_x * input + p_y + p_z * input^2)}.
     */
    public double transform(double input) {
        double px = position.getX();
        double py = position.getY();
        double pz = position.getZ();
        return Math.tanh(px * input + py + pz * input * input);
    }

    public void moveTo(Point newPosition) {
        Objects.requireNonNull(newPosition, "newPosition");
        if (newPosition.getDimension() != 3) {
            throw new IllegalArgumentException("Silk position must be three-dimensional");
        }
        position = newPosition;
    }

    public boolean attachTooth(Silk target) {
        Objects.requireNonNull(target, "target");
        if (target == this || teeth.contains(target)) {
            return false;
        }
        teeth.add(target);
        return true;
    }

    public boolean detachTooth(Silk target) {
        return teeth.remove(target);
    }

    public void clearTeeth() {
        teeth.clear();
    }

    public int getId() {
        return id;
    }

    public SilkParameters getParameters() {
        return parameters;
    }

    public Point getPosition() {
        return position;
    }

    public double getExpectedInput() {
        return expectedInput;
    }

    public double getExcitation() {
        return excitation;
    }

    public double getBaseline() {
        return baseline;
    }

    public double getDeviation() {
        return excitation - baseline;
    }

    public double getLastOutput() {
        return lastOutput;
    }

    public long getTickCount() {
        return tickCount;
    }

    public List<Silk> getTeeth() {
        return Collections.unmodifiableList(teeth);
    }

    public List<SilkTick> getHistory() {
        return Collections.unmodifiableList(history);
    }

    public void clearHistory() {
        history.clear();
    }
}
