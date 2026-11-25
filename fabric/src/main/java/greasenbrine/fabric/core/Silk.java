package greasenbrine.fabric.core;

import greasenbrine.fabric.structures.Point;
import java.util.ArrayList;
import java.util.List;

public class Silk {

    private final int id;
    private Point position;

    private double targetExcitation;
    private double leakRate;
    private double baselineExcitation;
    private double excitationThreshold;

    private double baselineGain;
    private double thresholdGain;
    private double surpriseGain;
    private double dampeningFactor;

    private Point expectedInput;
    private Point excitation;
    private long nextTick;

    private final List<Silk> teethConnections = new ArrayList<>();
    private final List<SilkTick> silkActivations = new ArrayList<>();

    public Silk(
        int id,
        Point initialPosition,
        double targetExcitation,
        double leakRate,
        double baselineExcitation,
        double excitationThreshold,
        double baselineGain,
        double excitationGain,
        double surpriseGain,
        double dampeningFactor
    ) {
        this.id = id;
        this.position = initialPosition;
        this.targetExcitation = targetExcitation;
        this.leakRate = leakRate;
        this.baselineExcitation = baselineExcitation;
        this.excitationThreshold = excitationThreshold;
        this.baselineGain = clamp(baselineGain, 0.0, 1.0);
        this.thresholdGain = excitationGain;
        this.surpriseGain = surpriseGain;
        this.dampeningFactor = clamp(dampeningFactor, 0.0, 1.0);
        this.expectedInput = new Point(new double[]{0.0, 0.0, 0.0});
        this.excitation = pointFromScalar(baselineExcitation);
        this.nextTick = 1;
    }

    public void initialize() {
        resetHistory();

        expectedInput = new Point(new double[]{0.0, 0.0, 0.0});
        excitation = pointFromScalar(baselineExcitation);
        nextTick = 1;

        silkActivations.add(new SilkTick(
            id,
            0,
            position,
            new Point(new double[]{0.0, 0.0, 0.0}),
            expectedInput,
            excitation,
            targetExcitation,
            excitationThreshold,
            0.0,
            0.0,
            dampeningFactor,
            true,
            false,
            false
        ));
    }

    public SilkTick step(Point receiverInput) {
        if (receiverInput == null) {
            throw new IllegalArgumentException("receiverInput cannot be null");
        }

        Point previousPrediction = copy(expectedInput);
        Point rawSurprise = absoluteDifference(receiverInput, previousPrediction);
        double rawSurpriseMagnitude = rawSurprise.magnitude();
        double dampedSurpriseMagnitude = rawSurpriseMagnitude * (1.0 - dampeningFactor);

        Point nextExcitation = new Point(new double[]{0.0, 0.0, 0.0});
        for (int i = 0; i < 3; i++) {
            double retained = excitation.get(i) * (1.0 - leakRate);
            double surpriseDrive = rawSurprise.get(i) * surpriseGain * (1.0 - dampeningFactor);
            double value = retained + surpriseDrive;
            nextExcitation = withCoordinate(nextExcitation, i, clamp(value, 0.0, excitationThreshold));
        }

        for (int i = 0; i < 3; i++) {
            double predicted = previousPrediction.get(i) * (1.0 - baselineGain)
                + receiverInput.get(i) * baselineGain;
            expectedInput = withCoordinate(expectedInput, i, predicted);
        }

        double excitationMagnitude = nextExcitation.magnitude();
        baselineExcitation = baselineExcitation * (1.0 - thresholdGain)
            + excitationMagnitude * thresholdGain;

        boolean triggered = excitationMagnitude >= excitationThreshold;
        boolean surpriseBoosted = dampedSurpriseMagnitude > 0.0;

        excitation = nextExcitation;
        SilkTick tick = new SilkTick(
            id,
            nextTick++,
            position,
            copy(receiverInput),
            copy(expectedInput),
            copy(nextExcitation),
            targetExcitation,
            excitationThreshold,
            rawSurpriseMagnitude,
            dampedSurpriseMagnitude,
            dampeningFactor,
            triggered,
            surpriseBoosted,
            triggered
        );
        silkActivations.add(tick);
        return tick;
    }

    public void resetHistory() {
        silkActivations.clear();
    }

    public boolean seedInitialInput(Point input) {
        if (input == null || silkActivations.isEmpty()) {
            return false;
        }

        Point seed = copy(input);
        expectedInput = seed;
        silkActivations.get(0).setReceiverInput(seed);
        silkActivations.get(0).setExpectedInput(copy(seed));
        return true;
    }

    public Point calculateSurprise(int tickIndex) {
        if (tickIndex < 0 || tickIndex >= silkActivations.size()) {
            throw new IndexOutOfBoundsException("tickIndex: " + tickIndex);
        }

        SilkTick tick = silkActivations.get(tickIndex);
        return absoluteDifference(tick.getReceiverInput(), tick.getExpectedInput());
    }

    public int getId() {
        return id;
    }

    public Point getPosition() {
        return position;
    }

    public double getTargetExcitation() {
        return targetExcitation;
    }

    public double getLeakRate() {
        return leakRate;
    }

    public double getBaselineExcitation() {
        return baselineExcitation;
    }

    public double getExcitationThreshold() {
        return excitationThreshold;
    }

    public double getBaselineGain() {
        return baselineGain;
    }

    public double getThresholdGain() {
        return thresholdGain;
    }

    public double getSurpriseGain() {
        return surpriseGain;
    }

    public double getDampeningFactor() {
        return dampeningFactor;
    }

    public Point getExpectedInput() {
        return copy(expectedInput);
    }

    public Point getExcitation() {
        return copy(excitation);
    }

    public List<SilkTick> getSilkActivations() {
        return List.copyOf(silkActivations);
    }

    public List<Silk> getTeethConnections() {
        return teethConnections;
    }

    private static Point absoluteDifference(Point observed, Point expected) {
        double[] values = new double[3];
        for (int i = 0; i < 3; i++) {
            values[i] = Math.abs(observed.get(i) - expected.get(i));
        }
        return new Point(values);
    }

    private static Point copy(Point point) {
        return new Point(point.getCoordinates());
    }

    private static Point pointFromScalar(double value) {
        return new Point(new double[]{value, value, value});
    }

    private static Point withCoordinate(Point point, int index, double value) {
        double[] values = point.getCoordinates();
        values[index] = value;
        return new Point(values);
    }

    private static double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
