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

    public Silk(
        int id, 
        Point initialPosition,
        double targetExcitation,
        double leakRate,
        double baselineExcitation,
        double excitationThreshold,
        double baselineGain,
        double thresholdGain,
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
        this.thresholdGain = thresholdGain;
        this.surpriseGain = surpriseGain;
        this.dampeningFactor = dampeningFactor;
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
}
