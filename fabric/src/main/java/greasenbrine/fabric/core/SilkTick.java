package greasenbrine.fabric.core;

import greasenbrine.fabric.structures.Point;

/**
 * Represents a single tick of the silk simulation.
 * 
 * @silkID The unique identifier of the silk.
 * @tick The current tick number of the simulation.
 * @position The 3D position of the silk at this tick.
 * @receiverInput The input received by the silk at this tick.
 * @excitation The excitation of the silk at this tick.
 * @targetExcitation The target excitation level of the silk.
 * @excitationThreshold The maximum excitation the silk can have.
 * @rawSurprise The raw surprise value of the silk.
 * @dampedSurprise The damped surprise value of the silk.
 * @dampeningFactor The dampening factor applied to the surprise.
 * @triggered True if excitation deviation reached @effectiveThreshold.
 * @surpriseBoosted True if damped surprise actively pushed excitation
 * @recruiting True if unit emits a signal through teeth downstream.
 */

public class SilkTick {
    public int silkID;
    public long tick;
    public Point position;
    public Point receiverInput;
    public Point excitation;
    public double targetExcitation;
    public double excitationThreshold;
    public double rawSurprise;
    public double dampedSurprise;
    public double dampeningFactor;
    public boolean triggered;
    public boolean surpriseBoosted;
    public boolean recruiting;

    public SilkTick(
        int silkID,
        long tick,
        Point position,
        Point receiverInput,
        Point excitation,
        double targetExcitation,
        double excitationThreshold,
        double rawSurprise,
        double dampedSurprise,
        double dampeningFactor,
        boolean triggered,
        boolean surpriseBoosted,
        boolean recruiting
    ) {
        this.silkID = silkID;
        this.tick = tick;
        this.position = position;
        this.receiverInput = receiverInput;
        this.excitation = excitation;
        this.targetExcitation = targetExcitation;
        this.excitationThreshold = excitationThreshold;
        this.rawSurprise = rawSurprise;
        this.dampedSurprise = dampedSurprise;
        this.dampeningFactor = dampeningFactor;
        this.triggered = triggered;
        this.surpriseBoosted = surpriseBoosted;
        this.recruiting = recruiting;
    }

    public void setReceiverInput(Point input) {
        this.receiverInput = input;
    }
}
    