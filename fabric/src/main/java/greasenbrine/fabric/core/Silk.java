package greasenbrine.fabric.core;

import greasenbrine.fabric.structures.Point;
import greasenbrine.fabric.core.SilkTick;
import java.util.ArrayList;
import java.util.List;

public class Silk {


    // Unique Silk identifier
    private final int id;

    // Current position of the Silk
    private Point position;

    // Homeostatic sweet spot and per-tick leak rate toward that sweet spot
    private double targetExcitation;
    private double leakRate;

    // Starting excitation and triggering threshold knob
    private double baselineExcitation;
    private double excitationThreshold;

    // Gain scaling drives
    private double baselineGain;
    private double thresholdGain;
    private double surpriseGain;

    // False-alarm dampener in [0.0, 1.0]: higher values choke off spurious activations
    private double dampeningFactor;

    private List<Silk> teethConnections = new ArrayList<>();

    private List<SilkTick> silkActivations = new ArrayList<>();

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
        this.baselineGain = baselineGain;
        this.thresholdGain = excitationGain;
        this.surpriseGain = surpriseGain;
        this.dampeningFactor = dampeningFactor;
    }

    public void initialize() {
        resetHistory();

        SilkTick initialTick = new SilkTick(
            this.id,
            0,
            this.position,
            new Point(new double[]{0, 0, 0}),
            new Point(new double[]{this.baselineExcitation, this.baselineExcitation, this.baselineExcitation}),
            this.targetExcitation,
            this.excitationThreshold,
            0.0,
            0.0,
            this.dampeningFactor,
            false,
            false,
            true
        );
        silkActivations.add(initialTick);
        seedInitialInput(new Point(new double[]{0, 0, 0}));
    }

    public void resetHistory() {
        silkActivations.clear();
    }

    public boolean seedInitialInput(Point input) {
        if (input == null) {
            return false;
        }
        SilkTick tick = this.silkActivations.get(0);
        tick.setReceiverInput(input);
        
        return true;
    }

    public Point calculateSurprise() {
        Point rawSurprise = new Point(new double[]{0, 0, 0});

        
        return rawSurprise;
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

    public List<Silk> getTeethConnections() {
        return teethConnections;
    }
}
