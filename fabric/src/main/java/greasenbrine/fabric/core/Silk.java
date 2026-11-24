package greasenbrine.fabric.core;

import greasenbrine.fabric.structures.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * A stateful, homeostatic, surprise-sensitive entity within the fabric network.
 */

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

    // Current 3D excitation vector and local prediction
    private Point excitation;
    private Point expectedSignal;

    // Cached results for downstream signal computations
    private double lastDampedSurprise;
    private boolean lastSweetSpotAlighned;
    private boolean lastTriggered;
    private boolean lastSurpriseBoosted;

    // Co-firing and usefulness tracking
    private int coFiringCount;
    private double usefulnessSignal;
    private boolean alive = true;
    private long tickCount;

    // Connections to downstream Silks (teeth)
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

    public void probeUpstreamTeeth(Silk upstreamSilk) {
        upstreamSilk.teethConnections.add(this);
    }

    public List<Silk> pruneDeadBranches(Silk rootNode) {
        for (int i = 0; i < rootNode.teethConnections.size(); i++) {
            Silk child = rootNode.teethConnections.get(i);
            
            if (!child.alive) {
                rootNode.teethConnections.remove(i);
                i--;
            } else {
                pruneDeadBranches(child);
            }
        }
        return rootNode.teethConnections;
    }
}
