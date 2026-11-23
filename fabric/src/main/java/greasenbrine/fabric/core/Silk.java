package greasenbrine.fabric.core;

import greasenbrine.fabric.structures.Point;
import greasenbrine.fabric.core.SilkTick;
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
   private boolean requireSurpriseRecruitment;
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
    double dampeningFactor,
    boolean requireSurpriseRecruitment
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
    this.requireSurpriseRecruitment = requireSurpriseRecruitment;
   }
}
